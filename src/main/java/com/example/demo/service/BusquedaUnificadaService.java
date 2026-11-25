package com.example.demo.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.example.demo.dto.SearchResultDTO;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class BusquedaUnificadaService {

	private final ObraService obraService;
	private final TmdbService tmdbService;
	private final GoogleBooksService googleBooksService;
	private final RawgService rawgService;
	private final TmdbObraMapper tmdbObraMapper;
	private final TmdbResultadoMapper tmdbResultadoMapper;

	private final JsonMapper jsonMapper = new JsonMapper();

	public BusquedaUnificadaService(ObraService obraService, TmdbService tmdbService, TmdbObraMapper tmdbObraMapper,
			TmdbResultadoMapper tmdbResultadoMapper, GoogleBooksService googleBooksService, RawgService rawgService) {

		this.obraService = obraService;
		this.tmdbService = tmdbService;
		this.tmdbObraMapper = tmdbObraMapper;
		this.tmdbResultadoMapper = tmdbResultadoMapper;
		this.googleBooksService = googleBooksService;
		this.rawgService = rawgService;
	}

	// Combina los resultados de Neon y las tres APIs.
	public List<SearchResultDTO> buscar(String titulo, TipoObra tipo) {
		return buscarInternamente(titulo, tipo, false);
	}

	// Búsqueda previa al registro manual.
	public List<SearchResultDTO> comprobarAntesDeRegistrar(String titulo, TipoObra tipo) {

		if (titulo == null || titulo.isBlank()) {
			throw new IllegalArgumentException("El título es obligatorio");
		}

		if (tipo == null) {
			throw new IllegalArgumentException("El tipo de obra es obligatorio");
		}

		return buscarInternamente(titulo.trim(), tipo, true);
	}

	private List<SearchResultDTO> buscarInternamente(String titulo, TipoObra tipo, boolean comprobacionEstricta) {

		Map<String, SearchResultDTO> resultados = new LinkedHashMap<>();

		// Recuperamos primero las obras existentes en Neon.
		for (Obra obra : obraService.buscar(titulo, tipo)) {

			// Las obras originales deben estar validadas.
			if (obra.getFuenteApi() == null && !obra.isValidada()) {
				continue;
			}

			SearchResultDTO dto = new SearchResultDTO(obra.getId(), obra.getTitulo(),
					obra.getTipo() == null ? "" : obra.getTipo().name());

			dto.setFuenteApi(obra.getFuenteApi());
			dto.setIdExterno(obra.getIdExterno());
			dto.setImagenUrl(obra.getImagenUrl());

			incorporarResultado(resultados, dto);
		}

		// Sin texto no consultamos las APIs externas.
		if (titulo == null || titulo.isBlank()) {
			return new ArrayList<>(resultados.values());
		}

		// Google Books.
		if (tipo == null || tipo == TipoObra.LIBRO) {

			consultarApi(resultados, "Google Books", () -> convertirLibros(googleBooksService.buscarLibros(titulo)),
					comprobacionEstricta);
		}

		// Películas de TMDB.
		if (tipo == null || tipo == TipoObra.PELICULA) {

			consultarApi(resultados, "TMDB películas",
					() -> convertirTmdb(tmdbService.buscarPeliculas(titulo), "PELICULA"), comprobacionEstricta);
		}

		// Series de TMDB.
		if (tipo == null || tipo == TipoObra.SERIE) {

			consultarApi(resultados, "TMDB series", () -> convertirTmdb(tmdbService.buscarSeries(titulo), "SERIE"),
					comprobacionEstricta);
		}

		// Videojuegos de RAWG.
		if (tipo == null || tipo == TipoObra.VIDEOJUEGO) {

			consultarApi(resultados, "RAWG", () -> convertirVideojuegos(rawgService.buscarVideojuegos(titulo)),
					comprobacionEstricta);
		}

		return new ArrayList<>(resultados.values());
	}

	// Gestiona las consultas a las APIs externas.
	private void consultarApi(Map<String, SearchResultDTO> resultados, String nombreApi,
			Supplier<List<SearchResultDTO>> consulta, boolean comprobacionEstricta) {

		try {

			for (SearchResultDTO dto : consulta.get()) {
				incorporarResultado(resultados, dto);
			}

		} catch (RuntimeException e) {

			System.err.println("Error al consultar " + nombreApi + ": " + e.getClass().getSimpleName());

			if (comprobacionEstricta) {

				throw new org.springframework.web.server.ResponseStatusException(
						org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
						"No se ha podido consultar " + nombreApi + ". Inténtalo de nuevo antes de registrar la obra.");
			}
		}
	}

	// Evita duplicados utilizando fuente, tipo e identificador.
	private void incorporarResultado(Map<String, SearchResultDTO> resultados, SearchResultDTO dto) {

		if (dto == null || dto.getTitulo() == null || dto.getTitulo().isBlank()) {
			return;
		}

		String referencia;

		if (dto.getFuenteApi() != null && dto.getIdExterno() != null && !dto.getIdExterno().isBlank()) {

			referencia = dto.getFuenteApi() + ":" + dto.getTipo() + ":" + dto.getIdExterno();

		} else if (dto.getId() != null) {

			referencia = "NEON:" + dto.getId();

		} else {
			return;
		}

		SearchResultDTO existente = resultados.get(referencia);

		if (existente == null) {
			resultados.put(referencia, dto);
			return;
		}

		// Conservamos el ID interno de Neon, pero completamos
		// la portada con la información de la API.
		if (existente.getImagenUrl() == null || existente.getImagenUrl().isBlank()) {

			existente.setImagenUrl(dto.getImagenUrl());
		}
	}

	// Comprueba que la API ha devuelto una respuesta válida.
	private JsonNode validarRespuestaApi(String json) {

		if (json == null || json.isBlank()) {

			throw new IllegalStateException("La API ha devuelto una respuesta vacía");
		}

		JsonNode raiz = jsonMapper.readTree(json);

		if (raiz == null || !raiz.isObject()) {

			throw new IllegalStateException("La respuesta de la API no es válida");
		}

		return raiz;
	}

	private List<SearchResultDTO> convertirTmdb(String json, String tipo) {

		JsonNode raiz = validarRespuestaApi(json);

		if (!raiz.path("results").isArray()) {

			throw new IllegalStateException("Respuesta de TMDB incorrecta");
		}

		return tmdbObraMapper.convertirResultados(json, tipo).stream().map(tmdbResultadoMapper::convertir).toList();
	}

	// Adapta Google Books al formato común.
	private List<SearchResultDTO> convertirLibros(String json) {

		List<SearchResultDTO> resultados = new ArrayList<>();

		JsonNode raiz = validarRespuestaApi(json);

		// Google Books puede omitir items cuando no hay resultados.
		if (!raiz.path("items").isArray()) {

			if (raiz.path("totalItems").isIntegralNumber() && raiz.path("totalItems").asInt() == 0) {

				return resultados;
			}

			throw new IllegalStateException("Respuesta de Google Books incorrecta");
		}

		for (JsonNode libro : raiz.path("items")) {

			JsonNode info = libro.path("volumeInfo");

			String id = libro.path("id").asText("");
			String titulo = info.path("title").asText("");

			if (id.isBlank() || titulo.isBlank()) {
				continue;
			}

			String imagen = info.path("imageLinks").path("thumbnail").asText("");

			resultados.add(crearResultadoExterno("GOOGLE_BOOKS", TipoObra.LIBRO, id, titulo, imagen));
		}

		return resultados;
	}

	// Adapta RAWG al mismo formato común.
	private List<SearchResultDTO> convertirVideojuegos(String json) {

		List<SearchResultDTO> resultados = new ArrayList<>();

		JsonNode raiz = validarRespuestaApi(json);

		if (!raiz.path("results").isArray()) {

			throw new IllegalStateException("Respuesta de RAWG incorrecta");
		}

		for (JsonNode juego : raiz.path("results")) {

			String id = juego.path("id").asText("");
			String titulo = juego.path("name").asText("");

			if (id.isBlank() || titulo.isBlank()) {
				continue;
			}

			String imagen = juego.path("background_image").asText("");

			resultados.add(crearResultadoExterno("RAWG", TipoObra.VIDEOJUEGO, id, titulo, imagen));
		}

		return resultados;
	}

	// Construye los resultados externos de manera uniforme.
	private SearchResultDTO crearResultadoExterno(String fuente, TipoObra tipo, String idExterno, String titulo,
			String imagen) {

		SearchResultDTO dto = new SearchResultDTO(null, titulo, tipo.name());

		dto.setFuenteApi(fuente);
		dto.setIdExterno(idExterno);

		if (imagen != null && !imagen.isBlank()) {
			dto.setImagenUrl(imagen.replaceFirst("^http:", "https:"));
		}

		return dto;
	}
}