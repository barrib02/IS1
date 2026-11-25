package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestClientException;

import com.example.demo.dto.EstanteResponse;
import com.example.demo.dto.SearchResultDTO;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;

class EstanteServiceTest {

	private ObraRepository obraRepository;
	private GoogleBooksService googleBooksService;
	private TmdbService tmdbService;
	private RawgService rawgService;
	private EstanteService estanteService;

	private static final String LIBROS_JSON = """
			{
			    "items": [
			        {
			            "id": "libro123",
			            "volumeInfo": {
			                "title": "Harry Potter",
			                "imageLinks": {
			                    "thumbnail": "http://example.com/libro.jpg"
			                }
			            }
			        }
			    ]
			}
			""";

	private static final String PELICULAS_JSON = """
			{
			    "results": [
			        {
			            "id": 597,
			            "title": "Titanic",
			            "poster_path": "/titanic.jpg"
			        }
			    ]
			}
			""";

	private static final String SERIES_JSON = """
			{
			    "results": [
			        {
			            "id": 1399,
			            "name": "Juego de tronos",
			            "poster_path": "/serie.jpg"
			        }
			    ]
			}
			""";

	private static final String VIDEOJUEGOS_JSON = """
			{
			    "results": [
			        {
			            "id": 12345,
			            "name": "Minecraft",
			            "background_image": "https://example.com/minecraft.jpg"
			        }
			    ]
			}
			""";

	@BeforeEach
	void preparar() {

		obraRepository = mock(ObraRepository.class);
		googleBooksService = mock(GoogleBooksService.class);
		tmdbService = mock(TmdbService.class);
		rawgService = mock(RawgService.class);

		estanteService = new EstanteService(obraRepository, googleBooksService, tmdbService, rawgService);

		// Respuestas simuladas de Google Books.
		when(googleBooksService.obtenerLibrosRecomendados()).thenReturn(LIBROS_JSON);

		when(googleBooksService.buscarLibrosPorTematica(anyString())).thenReturn(LIBROS_JSON);

		// Respuestas simuladas de TMDB.
		when(tmdbService.obtenerPeliculasPopulares()).thenReturn(PELICULAS_JSON);

		when(tmdbService.obtenerSeriesPopulares()).thenReturn(SERIES_JSON);

		when(tmdbService.obtenerPeliculasPorGenero(anyString())).thenReturn(PELICULAS_JSON);

		when(tmdbService.obtenerSeriesPorGenero(anyString())).thenReturn(SERIES_JSON);

		// Respuestas simuladas de RAWG.
		when(rawgService.obtenerVideojuegosPopulares()).thenReturn(VIDEOJUEGOS_JSON);

		when(rawgService.obtenerVideojuegosPorGenero(anyString())).thenReturn(VIDEOJUEGOS_JSON);

		// Por defecto, Neon no contiene obras para los estantes.
		when(obraRepository.buscarObrasConResenas(nullable(TipoObra.class), any())).thenReturn(List.of());

		when(obraRepository.buscarObrasOriginalesValidadas(nullable(TipoObra.class), any())).thenReturn(List.of());
	}

	// Busca un estante concreto por su título.
	private EstanteResponse buscarEstante(List<EstanteResponse> estantes, String titulo) {

		return estantes.stream().filter(e -> titulo.equals(e.getTitulo())).findFirst().orElseThrow();
	}

	@Test
	void debeGenerarEstantesGenerales() {

	    List<EstanteResponse> resultado =
	            estanteService.obtenerEstantes(null);

	    assertFalse(resultado.isEmpty());

	    assertNotNull(buscarEstante(resultado, "Libros para perderse"));

	    assertNotNull(buscarEstante(resultado, "Noches de cine"));

	    assertNotNull(buscarEstante(resultado, "Maratones de fin de semana"));

	    assertNotNull(buscarEstante(resultado, "Una partida más"));
	}

	@Test
	void debeAdaptarLibrosDeGoogleBooks() {

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.LIBRO);

		SearchResultDTO libro = buscarEstante(resultado, "Recomendados").getObras().get(0);

		assertEquals("Harry Potter", libro.getTitulo());
		assertEquals("LIBRO", libro.getTipo());
		assertEquals("GOOGLE_BOOKS", libro.getFuenteApi());
		assertEquals("libro123", libro.getIdExterno());

		assertEquals("https://example.com/libro.jpg", libro.getImagenUrl());

		// No debe existir un ID interno de Neon.
		assertNull(libro.getId());
	}

	@Test
	void debeAdaptarPeliculasDeTmdb() {

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.PELICULA);

		SearchResultDTO pelicula = buscarEstante(resultado, "Recomendados").getObras().get(0);

		assertEquals("Titanic", pelicula.getTitulo());
		assertEquals("PELICULA", pelicula.getTipo());
		assertEquals("TMDB", pelicula.getFuenteApi());
		assertEquals("597", pelicula.getIdExterno());

		assertEquals("https://image.tmdb.org/t/p/w500/titanic.jpg", pelicula.getImagenUrl());

		assertNull(pelicula.getId());
	}

	@Test
	void debeAdaptarSeriesDeTmdb() {

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.SERIE);

		SearchResultDTO serie = buscarEstante(resultado, "Recomendados").getObras().get(0);

		assertEquals("Juego de tronos", serie.getTitulo());
		assertEquals("SERIE", serie.getTipo());
		assertEquals("TMDB", serie.getFuenteApi());
		assertEquals("1399", serie.getIdExterno());

		assertEquals("https://image.tmdb.org/t/p/w500/serie.jpg", serie.getImagenUrl());

		assertNull(serie.getId());
	}

	@Test
	void debeRespetarFiltroDeLibros() {

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.LIBRO);

		assertFalse(resultado.isEmpty());

		assertTrue(resultado.stream().flatMap(e -> e.getObras().stream()).allMatch(o -> "LIBRO".equals(o.getTipo())));

		verifyNoInteractions(tmdbService);
	}

	@Test
	void debeRespetarFiltroDePeliculas() {

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.PELICULA);

		assertFalse(resultado.isEmpty());

		assertTrue(
				resultado.stream().flatMap(e -> e.getObras().stream()).allMatch(o -> "PELICULA".equals(o.getTipo())));

		verifyNoInteractions(googleBooksService);

		verify(tmdbService, never()).obtenerSeriesPopulares();

		verify(tmdbService, never()).obtenerSeriesPorGenero(anyString());
	}

	@Test
	void debeRespetarFiltroDeSeries() {

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.SERIE);

		assertFalse(resultado.isEmpty());

		assertTrue(resultado.stream().flatMap(e -> e.getObras().stream()).allMatch(o -> "SERIE".equals(o.getTipo())));

		verifyNoInteractions(googleBooksService);

		verify(tmdbService, never()).obtenerPeliculasPopulares();

		verify(tmdbService, never()).obtenerPeliculasPorGenero(anyString());
	}

	@Test
	void debeMezclarTiposEnEstantesTematicos() {

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(null);

		EstanteResponse estante = buscarEstante(resultado, "Mundos que no existen");

		List<SearchResultDTO> obras = estante.getObras();

		assertEquals(4, obras.size());

		assertTrue(obras.stream().anyMatch(o -> "LIBRO".equals(o.getTipo())));

		assertTrue(obras.stream().anyMatch(o -> "PELICULA".equals(o.getTipo())));

		assertTrue(obras.stream().anyMatch(o -> "SERIE".equals(o.getTipo())));

		assertTrue(obras.stream().anyMatch(o -> "VIDEOJUEGO".equals(o.getTipo())));
	}

	@Test
	void debeLimitarEstantesADiezObras() {

		StringBuilder json = new StringBuilder("{\"results\":[");

		for (int i = 1; i <= 15; i++) {

			if (i > 1) {
				json.append(",");
			}

			json.append("{\"id\":").append(i).append(",\"title\":\"Pelicula ").append(i).append("\"}");
		}

		json.append("]}");

		when(tmdbService.obtenerPeliculasPopulares()).thenReturn(json.toString());

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.PELICULA);

		assertEquals(10, buscarEstante(resultado, "Recomendados").getObras().size());

		assertTrue(resultado.stream().allMatch(e -> e.getObras().size() <= 10));
	}

	@Test
	void debeMostrarObrasResenadas() {

		Obra obra = mock(Obra.class);

		when(obra.getId()).thenReturn(95L);
		when(obra.getTitulo()).thenReturn("Harry Potter");
		when(obra.getTipo()).thenReturn(TipoObra.LIBRO);
		when(obra.getFuenteApi()).thenReturn("GOOGLE_BOOKS");
		when(obra.getIdExterno()).thenReturn("libro123");

		when(obraRepository.buscarObrasConResenas(eq(TipoObra.LIBRO), any())).thenReturn(List.of(obra));

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.LIBRO);

		SearchResultDTO dto = buscarEstante(resultado, "Reseñadas por nuestra comunidad").getObras().get(0);

		assertEquals(95L, dto.getId());
		assertEquals("Harry Potter", dto.getTitulo());
		assertEquals("GOOGLE_BOOKS", dto.getFuenteApi());
		assertEquals("libro123", dto.getIdExterno());

		verify(obraRepository).buscarObrasConResenas(TipoObra.LIBRO, PageRequest.of(0, 10));
	}

	@Test
	void debeMostrarObrasOriginalesValidadas() {

		Obra obra = mock(Obra.class);

		when(obra.getId()).thenReturn(100L);
		when(obra.getTitulo()).thenReturn("Mi novela original");
		when(obra.getTipo()).thenReturn(TipoObra.LIBRO);

		when(obraRepository.buscarObrasOriginalesValidadas(eq(TipoObra.LIBRO), any())).thenReturn(List.of(obra));

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.LIBRO);

		SearchResultDTO dto = buscarEstante(resultado, "Creaciones de nuestra comunidad").getObras().get(0);

		assertEquals(100L, dto.getId());
		assertEquals("Mi novela original", dto.getTitulo());
		assertNull(dto.getFuenteApi());
		assertNull(dto.getIdExterno());

		verify(obraRepository).buscarObrasOriginalesValidadas(TipoObra.LIBRO, PageRequest.of(0, 10));
	}

	@Test
	void debeReutilizarCache() {

		estanteService.obtenerEstantes(TipoObra.LIBRO);
		estanteService.obtenerEstantes(TipoObra.LIBRO);

		// La segunda consulta reutiliza los datos de memoria.
		verify(googleBooksService, times(1)).obtenerLibrosRecomendados();

		verify(googleBooksService, times(1)).buscarLibrosPorTematica("fantasy");
	}

	@Test
	void debeSeguirFuncionandoSiFallaGoogleBooks() {

		when(googleBooksService.obtenerLibrosRecomendados())
				.thenThrow(new RestClientException("Google Books no disponible"));

		when(googleBooksService.buscarLibrosPorTematica(anyString()))
				.thenThrow(new RestClientException("Google Books no disponible"));

		List<EstanteResponse> resultado = assertDoesNotThrow(() -> estanteService.obtenerEstantes(null));

		assertNotNull(buscarEstante(resultado, "Noches de cine"));

		assertNotNull(buscarEstante(resultado, "Maratones de fin de semana"));

		assertFalse(resultado.stream().flatMap(e -> e.getObras().stream()).anyMatch(o -> "LIBRO".equals(o.getTipo())));
	}

	@Test
	void debeSeguirFuncionandoSiFallaTmdb() {

		when(tmdbService.obtenerPeliculasPopulares()).thenThrow(new RestClientException("TMDB no disponible"));

		when(tmdbService.obtenerSeriesPopulares()).thenThrow(new RestClientException("TMDB no disponible"));

		when(tmdbService.obtenerPeliculasPorGenero(anyString()))
				.thenThrow(new RestClientException("TMDB no disponible"));

		when(tmdbService.obtenerSeriesPorGenero(anyString())).thenThrow(new RestClientException("TMDB no disponible"));

		List<EstanteResponse> resultado = assertDoesNotThrow(() -> estanteService.obtenerEstantes(null));

		assertNotNull(buscarEstante(resultado, "Libros para perderse"));

		assertNotNull(buscarEstante(resultado, "Una partida más"));

		assertTrue(resultado.stream().flatMap(e -> e.getObras().stream())
				.allMatch(o -> "LIBRO".equals(o.getTipo()) || "VIDEOJUEGO".equals(o.getTipo())));
	}

	@Test
	void noDebeMostrarEstantesVacios() {

		when(googleBooksService.obtenerLibrosRecomendados()).thenReturn("{\"items\":[]}");

		when(googleBooksService.buscarLibrosPorTematica(anyString())).thenReturn("{\"items\":[]}");

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.LIBRO);

		assertTrue(resultado.isEmpty());
	}

	@Test
	void debeIgnorarObrasExternasSinIdentificadorOTitulo() {

		when(googleBooksService.obtenerLibrosRecomendados()).thenReturn("""
				{
				    "items": [
				        {
				            "volumeInfo": {
				                "title": "Libro sin ID"
				            }
				        },
				        {
				            "id": "sinTitulo",
				            "volumeInfo": {}
				        },
				        {
				            "id": "valido",
				            "volumeInfo": {
				                "title": "Libro valido"
				            }
				        }
				    ]
				}
				""");

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.LIBRO);

		List<SearchResultDTO> obras = buscarEstante(resultado, "Recomendados").getObras();

		assertEquals(1, obras.size());
		assertEquals("valido", obras.get(0).getIdExterno());
	}

	@Test
	void noDebeGuardarObrasEnNeonAlExplorar() {

		estanteService.obtenerEstantes(null);

		// Solo se permiten las dos consultas de lectura.
		verify(obraRepository).buscarObrasConResenas(isNull(), eq(PageRequest.of(0, 10)));

		verify(obraRepository).buscarObrasOriginalesValidadas(isNull(), eq(PageRequest.of(0, 10)));

		verifyNoMoreInteractions(obraRepository);
	}

	@Test
	void debeAdaptarVideojuegosDeRawg() {

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.VIDEOJUEGO);

		SearchResultDTO juego = buscarEstante(resultado, "Recomendados").getObras().get(0);

		assertEquals("Minecraft", juego.getTitulo());
		assertEquals("VIDEOJUEGO", juego.getTipo());
		assertEquals("RAWG", juego.getFuenteApi());
		assertEquals("12345", juego.getIdExterno());

		assertEquals("https://example.com/minecraft.jpg", juego.getImagenUrl());

		assertNull(juego.getId());
	}

	@Test
	void debeRespetarFiltroDeVideojuegos() {

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.VIDEOJUEGO);

		assertFalse(resultado.isEmpty());

		assertTrue(
				resultado.stream().flatMap(e -> e.getObras().stream()).allMatch(o -> "VIDEOJUEGO".equals(o.getTipo())));

		verifyNoInteractions(googleBooksService, tmdbService);

		verify(rawgService).obtenerVideojuegosPopulares();

		verify(rawgService).obtenerVideojuegosPorGenero("action");
	}

	@Test
	void debeReutilizarCacheDeRawg() {

		estanteService.obtenerEstantes(TipoObra.VIDEOJUEGO);
		estanteService.obtenerEstantes(TipoObra.VIDEOJUEGO);

		verify(rawgService, times(1)).obtenerVideojuegosPopulares();

		verify(rawgService, times(1)).obtenerVideojuegosPorGenero("action");
	}

	@Test
	void debeSeguirFuncionandoSiFallaRawg() {

		when(rawgService.obtenerVideojuegosPopulares()).thenThrow(new RestClientException("RAWG no disponible"));

		when(rawgService.obtenerVideojuegosPorGenero(anyString()))
				.thenThrow(new RestClientException("RAWG no disponible"));

		List<EstanteResponse> resultado = assertDoesNotThrow(() -> estanteService.obtenerEstantes(null));

		assertNotNull(buscarEstante(resultado, "Libros para perderse"));

		assertNotNull(buscarEstante(resultado, "Noches de cine"));

		assertNotNull(buscarEstante(resultado, "Maratones de fin de semana"));

		assertFalse(
				resultado.stream().flatMap(e -> e.getObras().stream()).anyMatch(o -> "VIDEOJUEGO".equals(o.getTipo())));
	}

	@Test
	void debeIgnorarVideojuegosSinIdentificadorOTitulo() {

		when(rawgService.obtenerVideojuegosPopulares()).thenReturn("""
				{
				    "results": [
				        {
				            "name": "Juego sin ID"
				        },
				        {
				            "id": 111
				        },
				        {
				            "id": 222,
				            "name": "Juego valido"
				        }
				    ]
				}
				""");

		List<EstanteResponse> resultado = estanteService.obtenerEstantes(TipoObra.VIDEOJUEGO);

		List<SearchResultDTO> obras = buscarEstante(resultado, "Recomendados").getObras();

		assertEquals(1, obras.size());

		assertEquals("222", obras.get(0).getIdExterno());

		assertEquals("Juego valido", obras.get(0).getTitulo());
	}
}