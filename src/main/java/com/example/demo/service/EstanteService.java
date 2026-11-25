package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dto.EstanteResponse;
import com.example.demo.dto.SearchResultDTO;
import com.example.demo.entity.GeneroObra;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class EstanteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EstanteService.class);

	private static final int POR_ESTANTE = 10;

	// Las recomendaciones externas se guardan en memoria durante una hora.
	private static final long DURACION_CACHE = 60 * 60 * 1000L;

	private record PorTipo(TipoObra tipo, String titulo, String subtitulo) {
	}

	private record PorGenero(String titulo, String subtitulo, List<GeneroObra> generos, String tematicaLibros,
			String generosPeliculas, String generosSeries, String generosVideojuegos) {
	}

	private record EntradaCache(List<SearchResultDTO> obras, long caducidad) {
	}

	private static final List<PorTipo> TIPOS = List.of(
			new PorTipo(TipoObra.LIBRO, "Libros para perderse", "Historias para devorar entre páginas"),

			new PorTipo(TipoObra.PELICULA, "Noches de cine", "Películas para poner en la pantalla grande"),

			new PorTipo(TipoObra.SERIE, "Maratones de fin de semana", "Series para decir \"un capítulo más\""),

			new PorTipo(TipoObra.VIDEOJUEGO, "Una partida más", "Videojuegos para no soltar el mando"));

	private static final List<PorGenero> GENEROS = List.of(

			new PorGenero("Para no dormir", "Terror y suspense para dejar la luz encendida",
					List.of(GeneroObra.TERROR, GeneroObra.THRILLER), "horror", "27|53", null, null),

			new PorGenero("Para ver en familia", "Planes para todas las edades",
					List.of(GeneroObra.INFANTIL, GeneroObra.ANIMACION, GeneroObra.JUVENIL), "juvenile fiction",
					"10751|16", "10751|16", "family"),

			new PorGenero("Mundos que no existen", "Fantasía y ciencia ficción para escapar de la realidad",
					List.of(GeneroObra.FANTASIA, GeneroObra.CIENCIA_FICCION), "fantasy", "14|878", "10765",
					"role-playing-games-rpg"),

			new PorGenero("Para llorar a gusto", "Dramas y romances que te dejan el corazón blandito",
					List.of(GeneroObra.DRAMA, GeneroObra.ROMANCE), "romance", "18|10749", "18", null),

			new PorGenero("¿Quién fue el culpable?", "Misterio y crimen para los que no pueden parar",
					List.of(GeneroObra.MISTERIO, GeneroObra.CRIMEN), "mystery", "9648|80", "9648|80", null),

			new PorGenero("Descarga de adrenalina", "Acción y aventura sin un respiro",
					List.of(GeneroObra.ACCION, GeneroObra.AVENTURA), "adventure", "28|12", "10759", "action"),

			new PorGenero("Risas aseguradas", "Comedias para desconectar", List.of(GeneroObra.COMEDIA), "humor", "35",
					"35", null),

			new PorGenero("Para jugar con amigos", "Multijugador, deportes y carreras",
					List.of(GeneroObra.MULTIJUGADOR, GeneroObra.DEPORTES, GeneroObra.CARRERAS), null, null, null,
					"sports"),

			new PorGenero("Basado en hechos reales", "Documentales y relatos históricos",
					List.of(GeneroObra.DOCUMENTAL, GeneroObra.HISTORICO), "history", "99|36", "99", null));

	private final ObraRepository obraRepository;
	private final GoogleBooksService googleBooksService;
	private final TmdbService tmdbService;
	private final RawgService rawgService;

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	private final Map<String, EntradaCache> cache = new ConcurrentHashMap<>();

	public EstanteService(ObraRepository obraRepository, GoogleBooksService googleBooksService, TmdbService tmdbService,
			RawgService rawgService) {

		this.obraRepository = obraRepository;
		this.googleBooksService = googleBooksService;
		this.tmdbService = tmdbService;
		this.rawgService = rawgService;
	}

	// Genera los estantes generales o los de un tipo concreto.
	public List<EstanteResponse> obtenerEstantes(TipoObra tipo) {

		List<EstanteResponse> estantes = new ArrayList<>();

		if (tipo == null) {

			for (PorTipo p : TIPOS) {

				anadir(estantes, p.titulo(), p.subtitulo(), obtenerRecomendaciones(p.tipo()));
			}

		} else {

			anadir(estantes, "Recomendados", "Descubre nuevas obras", obtenerRecomendaciones(tipo));
		}

		// Estantes temáticos.
		for (PorGenero p : GENEROS) {

			List<SearchResultDTO> obras = obtenerRecomendacionesPorGenero(p, tipo);

			anadir(estantes, p.titulo(), p.subtitulo(), obras);
		}

		// Obras que han recibido reseñas de nuestros usuarios.
		anadir(estantes, "Reseñadas por nuestra comunidad", "Descubre las opiniones de otros usuarios",
				obtenerObrasResenadas(tipo));

		// Obras originales registradas en Shelfie.
		anadir(estantes, "Creaciones de nuestra comunidad", "Descubre las obras de nuestros usuarios",
				obtenerObrasOriginales(tipo));

		return estantes;
	}

	// Recomendaciones generales de cada API.
	private List<SearchResultDTO> obtenerRecomendaciones(TipoObra tipo) {

		return switch (tipo) {

		case LIBRO ->
			consultarConCache("libros-populares", () -> adaptarLibros(googleBooksService.obtenerLibrosRecomendados()));

		case PELICULA -> consultarConCache("peliculas-populares",
				() -> adaptarTmdb(tmdbService.obtenerPeliculasPopulares(), TipoObra.PELICULA));

		case SERIE -> consultarConCache("series-populares",
				() -> adaptarTmdb(tmdbService.obtenerSeriesPopulares(), TipoObra.SERIE));

		case VIDEOJUEGO ->
			consultarConCache("videojuegos-populares", () -> adaptarRawg(rawgService.obtenerVideojuegosPopulares()));
		};
	}

	// Recomendaciones por temática y tipo.
	private List<SearchResultDTO> obtenerRecomendacionesPorGenero(PorGenero genero, TipoObra tipo) {

		List<SearchResultDTO> obras = new ArrayList<>();

		if ((tipo == null || tipo == TipoObra.LIBRO) && genero.tematicaLibros() != null) {

			obras.addAll(consultarConCache("libros-" + genero.tematicaLibros(),
					() -> adaptarLibros(googleBooksService.buscarLibrosPorTematica(genero.tematicaLibros()))));
		}

		if ((tipo == null || tipo == TipoObra.PELICULA) && genero.generosPeliculas() != null) {

			obras.addAll(consultarConCache("peliculas-" + genero.generosPeliculas(),
					() -> adaptarTmdb(tmdbService.obtenerPeliculasPorGenero(genero.generosPeliculas()),
							TipoObra.PELICULA)));
		}

		if ((tipo == null || tipo == TipoObra.SERIE) && genero.generosSeries() != null) {

			obras.addAll(consultarConCache("series-" + genero.generosSeries(),
					() -> adaptarTmdb(tmdbService.obtenerSeriesPorGenero(genero.generosSeries()), TipoObra.SERIE)));
		}
		if ((tipo == null || tipo == TipoObra.VIDEOJUEGO) && genero.generosVideojuegos() != null) {

			obras.addAll(consultarConCache("videojuegos-" + genero.generosVideojuegos(),
					() -> adaptarRawg(rawgService.obtenerVideojuegosPorGenero(genero.generosVideojuegos()))));
		}

		// Alterna los tipos para que los libros no ocupen
		// siempre todas las posiciones del estante general.
		if (tipo == null) {
			return mezclarTipos(obras);
		}

		return obras.stream().limit(POR_ESTANTE).toList();
	}

	// Convierte la respuesta JSON de Google Books en DTO.
	private List<SearchResultDTO> adaptarLibros(String json) {

		List<SearchResultDTO> obras = new ArrayList<>();

		JsonNode raiz = jsonMapper.readTree(json);

		for (JsonNode libro : raiz.path("items")) {

			JsonNode info = libro.path("volumeInfo");

			String id = libro.path("id").asText("");
			String titulo = info.path("title").asText("");

			if (id.isBlank() || titulo.isBlank()) {
				continue;
			}

			SearchResultDTO dto = new SearchResultDTO(null, titulo, TipoObra.LIBRO.name());

			dto.setFuenteApi("GOOGLE_BOOKS");
			dto.setIdExterno(id);

			String portada = info.path("imageLinks").path("thumbnail").asText("");

			if (!portada.isBlank()) {
				dto.setImagenUrl(portada.replace("http://", "https://"));
			}

			obras.add(dto);
		}

		return obras.stream().limit(POR_ESTANTE).toList();
	}

	// Convierte películas y series de TMDB en DTO.
	private List<SearchResultDTO> adaptarTmdb(String json, TipoObra tipo) {

		List<SearchResultDTO> obras = new ArrayList<>();

		JsonNode raiz = jsonMapper.readTree(json);

		for (JsonNode resultado : raiz.path("results")) {

			String id = resultado.path("id").asText("");

			String titulo = resultado.path(tipo == TipoObra.PELICULA ? "title" : "name").asText("");

			if (id.isBlank() || titulo.isBlank()) {
				continue;
			}

			SearchResultDTO dto = new SearchResultDTO(null, titulo, tipo.name());

			dto.setFuenteApi("TMDB");
			dto.setIdExterno(id);

			String poster = resultado.path("poster_path").asText("");

			if (!poster.isBlank() && !"null".equals(poster)) {

				dto.setImagenUrl("https://image.tmdb.org/t/p/w500" + poster);
			}

			obras.add(dto);
		}

		return obras.stream().limit(POR_ESTANTE).toList();
	}

	// Convierte los videojuegos de RAWG en el formato común.
	private List<SearchResultDTO> adaptarRawg(String json) {

		List<SearchResultDTO> obras = new ArrayList<>();

		if (json == null || json.isBlank()) {
			return obras;
		}

		JsonNode raiz = jsonMapper.readTree(json);

		for (JsonNode juego : raiz.path("results")) {

			String id = juego.path("id").asText("");
			String titulo = juego.path("name").asText("");

			if (id.isBlank() || titulo.isBlank()) {
				continue;
			}

			SearchResultDTO dto = new SearchResultDTO(null, titulo, TipoObra.VIDEOJUEGO.name());

			dto.setFuenteApi("RAWG");
			dto.setIdExterno(id);

			String portada = juego.path("background_image").asText("");

			if (!portada.isBlank() && !"null".equals(portada)) {

				dto.setImagenUrl(portada.replaceFirst("^http:", "https:"));
			}

			obras.add(dto);
		}

		return obras.stream().limit(POR_ESTANTE).toList();
	}

	// Alterna libros, películas, series y videojuegos.
	private List<SearchResultDTO> mezclarTipos(List<SearchResultDTO> obras) {

		List<SearchResultDTO> resultado = new ArrayList<>();

		List<List<SearchResultDTO>> grupos = new ArrayList<>();

		for (TipoObra tipo : TipoObra.values()) {

			grupos.add(obras.stream().filter(o -> tipo.name().equals(o.getTipo())).toList());
		}

		for (int i = 0; i < POR_ESTANTE; i++) {

			for (List<SearchResultDTO> grupo : grupos) {

				if (i < grupo.size()) {
					resultado.add(grupo.get(i));
				}

				if (resultado.size() == POR_ESTANTE) {
					return resultado;
				}
			}
		}

		return resultado;
	}

	// Obtiene las obras manuales validadas de Neon.
	private List<SearchResultDTO> obtenerObrasOriginales(TipoObra tipo) {

		Pageable pagina = PageRequest.of(0, POR_ESTANTE);

		return obraRepository.buscarObrasOriginalesValidadas(tipo, pagina).stream().map(this::convertirObraNeon)
				.toList();
	}

	// Evita consultar las APIs cada vez que se recarga Discover.
	private List<SearchResultDTO> consultarConCache(String clave, Supplier<List<SearchResultDTO>> consulta) {

		EntradaCache entrada = cache.compute(clave, (k, anterior) -> {

			long ahora = System.currentTimeMillis();

			if (anterior != null && anterior.caducidad() > ahora) {
				return anterior;
			}

			try {

				return new EntradaCache(List.copyOf(consulta.get()), ahora + DURACION_CACHE);

			} catch (Exception e) {

				LOGGER.warn("No se pudieron cargar las recomendaciones: {}", clave);

				// Si falla una API, conserva temporalmente
				// las recomendaciones anteriores, si existen.
				return new EntradaCache(anterior == null ? List.of() : anterior.obras(), ahora + 60_000L);
			}
		});

		return entrada.obras();
	}

	// Evita mostrar estantes vacíos.
	private void anadir(List<EstanteResponse> estantes, String titulo, String subtitulo, List<SearchResultDTO> obras) {

		if (!obras.isEmpty()) {

			estantes.add(new EstanteResponse(titulo, subtitulo, obras));
		}
	}

	private List<SearchResultDTO> obtenerObrasResenadas(TipoObra tipo) {

		Pageable pagina = PageRequest.of(0, POR_ESTANTE);

		return obraRepository.buscarObrasConResenas(tipo, pagina).stream().map(this::convertirObraNeon).toList();
	}

	private SearchResultDTO convertirObraNeon(Obra obra) {

		SearchResultDTO dto = new SearchResultDTO(obra.getId(), obra.getTitulo(), obra.getTipo().name());

		dto.setFuenteApi(obra.getFuenteApi());
		dto.setIdExterno(obra.getIdExterno());
		dto.setImagenUrl(obra.getImagenUrl());

		return dto;
	}
}