
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.SearchResultDTO;
import com.example.demo.dto.TmdbObraDTO;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BusquedaUnificadaServiceTest {
	@Mock
	private ObraService obraService;

	@Mock
	private TmdbService tmdbService;

	@Mock
	private TmdbObraMapper tmdbObraMapper;

	@Mock
	private TmdbResultadoMapper tmdbResultadoMapper;

	@InjectMocks
	private BusquedaUnificadaService busquedaService;

	@Mock
	private GoogleBooksService googleBooksService;

	@Mock
	private RawgService rawgService;

	@Test
	void debeCombinarNeonYTMDBSinDuplicados() {

		// Oppenheimer ya está registrada en Neon.
		Obra oppenheimer = new Obra();
		oppenheimer.setId(62L);
		oppenheimer.setTitulo("Oppenheimer");
		oppenheimer.setTipo(TipoObra.PELICULA);
		oppenheimer.setFuenteApi("TMDB");
		oppenheimer.setIdExterno("872585");

		when(obraService.buscar("Oppenheimer", TipoObra.PELICULA)).thenReturn(List.of(oppenheimer));

		// TMDB devuelve la misma película y otra diferente.
		TmdbObraDTO repetida = new TmdbObraDTO();
		TmdbObraDTO nueva = new TmdbObraDTO();

		String jsonSimulado = """
				{
				    "results": [
				        {"id": 872585, "title": "Oppenheimer"},
				        {"id": 999999, "title": "Otra película"}
				    ]
				}
				""";

		when(tmdbService.buscarPeliculas("Oppenheimer")).thenReturn(jsonSimulado);

		when(tmdbObraMapper.convertirResultados(jsonSimulado, "PELICULA")).thenReturn(List.of(repetida, nueva));
		SearchResultDTO resultadoRepetido = new SearchResultDTO(null, "Oppenheimer", "PELICULA");
		resultadoRepetido.setFuenteApi("TMDB");
		resultadoRepetido.setIdExterno("872585");

		SearchResultDTO resultadoNuevo = new SearchResultDTO(null, "Otra película", "PELICULA");
		resultadoNuevo.setFuenteApi("TMDB");
		resultadoNuevo.setIdExterno("999999");

		when(tmdbResultadoMapper.convertir(repetida)).thenReturn(resultadoRepetido);

		when(tmdbResultadoMapper.convertir(nueva)).thenReturn(resultadoNuevo);

		// Ejecutamos la búsqueda unificada.
		List<SearchResultDTO> resultados = busquedaService.buscar("Oppenheimer", TipoObra.PELICULA);

		// Debe conservar la obra de Neon y añadir solo la nueva.
		assertEquals(2, resultados.size());
		assertEquals(62L, resultados.get(0).getId());
		assertEquals("999999", resultados.get(1).getIdExterno());

		// No debe consultar series si solo buscamos películas.
		verify(tmdbService, never()).buscarSeries(anyString());
	}

	@Test
	void noDebeConsultarTMDBSinTitulo() {

		// Simulamos una búsqueda sin texto.
		when(obraService.buscar(null, null)).thenReturn(List.of());

		List<SearchResultDTO> resultados = busquedaService.buscar(null, null);

		assertTrue(resultados.isEmpty());

		// Sin título, no realizamos llamadas externas.
		verifyNoInteractions(tmdbService, tmdbObraMapper, tmdbResultadoMapper);
	}

	@Test
	void debeBuscarPeliculasYSeriesSinFiltro() {

		when(obraService.buscar("Harry Potter", null)).thenReturn(List.of());

		when(tmdbService.buscarPeliculas("Harry Potter")).thenReturn("{\"results\":[]}");

		when(tmdbService.buscarSeries("Harry Potter")).thenReturn("{\"results\":[]}");

		when(tmdbObraMapper.convertirResultados("{\"results\":[]}", "PELICULA")).thenReturn(List.of());

		when(tmdbObraMapper.convertirResultados("{\"results\":[]}", "SERIE")).thenReturn(List.of());

		// Buscamos sin especificar tipo.
		busquedaService.buscar("Harry Potter", null);

		// Debe consultar ambas categorías.
		verify(tmdbService).buscarPeliculas("Harry Potter");
		verify(tmdbService).buscarSeries("Harry Potter");
	}

	@Test
	void noDebeConsultarTMDBParaLibros() {

		when(obraService.buscar("Harry Potter", TipoObra.LIBRO)).thenReturn(List.of());

		busquedaService.buscar("Harry Potter", TipoObra.LIBRO);

		// Google Books se integrará más adelante.
		verifyNoInteractions(tmdbService);
	}

	@Test
	void debeBuscarLibrosEnGoogleBooks() {

		when(obraService.buscar("Harry Potter", TipoObra.LIBRO)).thenReturn(List.of());

		when(googleBooksService.buscarLibros("Harry Potter")).thenReturn("""
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
				""");

		List<SearchResultDTO> resultados = busquedaService.buscar("Harry Potter", TipoObra.LIBRO);

		assertEquals(1, resultados.size());

		SearchResultDTO libro = resultados.get(0);

		assertEquals("GOOGLE_BOOKS", libro.getFuenteApi());
		assertEquals("libro123", libro.getIdExterno());
		assertEquals("LIBRO", libro.getTipo());
		assertEquals("Harry Potter", libro.getTitulo());
		assertEquals("https://example.com/libro.jpg", libro.getImagenUrl());

		assertNull(libro.getId());

		verifyNoInteractions(tmdbService, rawgService);
	}

	@Test
	void debeBuscarVideojuegosEnRawg() {

		when(obraService.buscar("Minecraft", TipoObra.VIDEOJUEGO)).thenReturn(List.of());

		when(rawgService.buscarVideojuegos("Minecraft")).thenReturn("""
				{
				    "results": [
				        {
				            "id": 12345,
				            "name": "Minecraft",
				            "background_image": "https://example.com/juego.jpg"
				        }
				    ]
				}
				""");

		List<SearchResultDTO> resultados = busquedaService.buscar("Minecraft", TipoObra.VIDEOJUEGO);

		assertEquals(1, resultados.size());

		SearchResultDTO juego = resultados.get(0);

		assertEquals("RAWG", juego.getFuenteApi());
		assertEquals("12345", juego.getIdExterno());
		assertEquals("VIDEOJUEGO", juego.getTipo());
		assertEquals("Minecraft", juego.getTitulo());
		assertEquals("https://example.com/juego.jpg", juego.getImagenUrl());

		assertNull(juego.getId());

		verifyNoInteractions(tmdbService, googleBooksService);
	}

	@Test
	void debeConsultarTodasLasApisSinFiltro() {

		when(obraService.buscar("Minecraft", null)).thenReturn(List.of());

		when(googleBooksService.buscarLibros("Minecraft")).thenReturn("{\"items\":[]}");

		when(rawgService.buscarVideojuegos("Minecraft")).thenReturn("{\"results\":[]}");

		when(tmdbService.buscarPeliculas("Minecraft")).thenReturn("{\"results\":[]}");

		when(tmdbService.buscarSeries("Minecraft")).thenReturn("{\"results\":[]}");

		when(tmdbObraMapper.convertirResultados("{\"results\":[]}", "PELICULA")).thenReturn(List.of());

		when(tmdbObraMapper.convertirResultados("{\"results\":[]}", "SERIE")).thenReturn(List.of());

		busquedaService.buscar("Minecraft", null);

		verify(googleBooksService).buscarLibros("Minecraft");
		verify(rawgService).buscarVideojuegos("Minecraft");
		verify(tmdbService).buscarPeliculas("Minecraft");
		verify(tmdbService).buscarSeries("Minecraft");
	}

	@Test
	void debeEvitarDuplicadosDeRawg() {

		Obra obra = new Obra();

		obra.setId(100L);
		obra.setTitulo("Minecraft");
		obra.setTipo(TipoObra.VIDEOJUEGO);
		obra.setFuenteApi("RAWG");
		obra.setIdExterno("12345");

		when(obraService.buscar("Minecraft", TipoObra.VIDEOJUEGO)).thenReturn(List.of(obra));

		when(rawgService.buscarVideojuegos("Minecraft")).thenReturn("""
				{
				    "results": [
				        {
				            "id": 12345,
				            "name": "Minecraft",
				            "background_image": "https://example.com/juego.jpg"
				        }
				    ]
				}
				""");

		List<SearchResultDTO> resultados = busquedaService.buscar("Minecraft", TipoObra.VIDEOJUEGO);

		assertEquals(1, resultados.size());
		assertEquals(100L, resultados.get(0).getId());
		assertEquals("12345", resultados.get(0).getIdExterno());

		assertEquals("https://example.com/juego.jpg", resultados.get(0).getImagenUrl());
	}

	@Test
	void debeConservarNeonSiFallaRawg() {

		Obra obra = new Obra();

		obra.setId(100L);
		obra.setTitulo("Minecraft");
		obra.setTipo(TipoObra.VIDEOJUEGO);
		obra.setFuenteApi("RAWG");
		obra.setIdExterno("12345");

		when(obraService.buscar("Minecraft", TipoObra.VIDEOJUEGO)).thenReturn(List.of(obra));

		when(rawgService.buscarVideojuegos("Minecraft"))
				.thenThrow(new org.springframework.web.client.RestClientException("RAWG no disponible"));

		List<SearchResultDTO> resultados = assertDoesNotThrow(
				() -> busquedaService.buscar("Minecraft", TipoObra.VIDEOJUEGO));

		assertEquals(1, resultados.size());
		assertEquals(100L, resultados.get(0).getId());
	}

	@Test
	void noDebeConsultarApisSinTitulo() {

		when(obraService.buscar(null, null)).thenReturn(List.of());

		busquedaService.buscar(null, null);

		verifyNoInteractions(tmdbService, googleBooksService, rawgService);
	}

	@Test
	void comprobacionRechazaTituloVacio() {

		assertThrows(IllegalArgumentException.class,
				() -> busquedaService.comprobarAntesDeRegistrar("", TipoObra.LIBRO));
	}

	@Test
	void comprobacionRechazaTipoVacio() {

		assertThrows(IllegalArgumentException.class,
				() -> busquedaService.comprobarAntesDeRegistrar("Harry Potter", null));
	}

	@Test
	void comprobacionPermiteContinuarSinCoincidencias() {

		when(obraService.buscar("Obra original de prueba", TipoObra.LIBRO)).thenReturn(List.of());

		when(googleBooksService.buscarLibros("Obra original de prueba")).thenReturn("{\"totalItems\":0,\"items\":[]}");

		List<SearchResultDTO> resultado = busquedaService.comprobarAntesDeRegistrar("Obra original de prueba",
				TipoObra.LIBRO);

		assertTrue(resultado.isEmpty());
	}

	@Test
	void comprobacionSeDetieneSiFallaLaApi() {

		when(obraService.buscar("Minecraft", TipoObra.VIDEOJUEGO)).thenReturn(List.of());

		when(rawgService.buscarVideojuegos("Minecraft")).thenThrow(new RuntimeException("API no disponible"));

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> busquedaService.comprobarAntesDeRegistrar("Minecraft", TipoObra.VIDEOJUEGO));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
	}

	@Test
	void comprobacionRechazaRespuestaVaciaTmdb() {

		when(tmdbService.buscarPeliculas("Prueba")).thenReturn("");

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> busquedaService.comprobarAntesDeRegistrar("Prueba", TipoObra.PELICULA));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
	}

	@Test
	void comprobacionRechazaRespuestaIncorrectaTmdb() {

		when(tmdbService.buscarPeliculas("Prueba")).thenReturn("{}");

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> busquedaService.comprobarAntesDeRegistrar("Prueba", TipoObra.PELICULA));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
	}

	@Test
	void comprobacionAceptaGoogleBooksSinResultados() {

		when(googleBooksService.buscarLibros("Prueba")).thenReturn("{\"totalItems\":0}");

		assertTrue(busquedaService.comprobarAntesDeRegistrar("Prueba", TipoObra.LIBRO).isEmpty());
	}

	@Test
	void comprobacionRechazaRespuestaIncorrectaGoogleBooks() {

		when(googleBooksService.buscarLibros("Prueba")).thenReturn("{\"totalItems\":5}");

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> busquedaService.comprobarAntesDeRegistrar("Prueba", TipoObra.LIBRO));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
	}

	@Test
	void comprobacionRechazaRespuestaIncorrectaRawg() {

		when(rawgService.buscarVideojuegos("Prueba")).thenReturn("{}");

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> busquedaService.comprobarAntesDeRegistrar("Prueba", TipoObra.VIDEOJUEGO));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
	}
}
