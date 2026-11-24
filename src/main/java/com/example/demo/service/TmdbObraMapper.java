
package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.dto.TmdbObraDTO;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// Transforma las respuestas JSON de TMDB en objetos
// que Shelfie puede utilizar para mostrar peliculas y series.
@Component
public class TmdbObraMapper {

	private final ObjectMapper objectMapper;
	private final TmdbGeneroMapper generoMapper;

	// Recibe las herramientas necesarias para interpretar
	// el JSON y convertir los generos de TMDB.
	public TmdbObraMapper(ObjectMapper objectMapper, TmdbGeneroMapper generoMapper) {

		this.objectMapper = objectMapper;
		this.generoMapper = generoMapper;
	}

	// Convierte los resultados de una busqueda de TMDB
	// en una lista de obras con el formato de Shelfie.
	public List<TmdbObraDTO> convertirResultados(String json, String tipo) {

		List<TmdbObraDTO> obras = new ArrayList<>();

		// Comprueba que el tipo de obra sea valido.
		if (!"PELICULA".equals(tipo) && !"SERIE".equals(tipo)) {
			throw new IllegalArgumentException("Tipo de obra no valido: " + tipo);
		}

		try {
			// Lee la respuesta JSON de TMDB.
			JsonNode respuesta = objectMapper.readTree(json);
			JsonNode resultados = respuesta.path("results");

			// Recorre las peliculas o series encontradas.
			for (JsonNode resultado : resultados) {

				TmdbObraDTO obra = new TmdbObraDTO();

				// Guarda el identificador externo de TMDB.
				obra.setTmdbId(resultado.path("id").asLong());

				// TMDB utiliza nombres distintos para
				// los titulos de peliculas y series.
				String campoTitulo = "PELICULA".equals(tipo) ? "title" : "name";

				obra.setTitulo(resultado.path(campoTitulo).asText());

				obra.setTipo(tipo);

				// Obtiene la descripcion de la obra.
				obra.setDescripcion(resultado.path("overview").asText());

				// Obtiene la ruta del poster si existe.
				JsonNode poster = resultado.path("poster_path");

				if (!poster.isNull() && !poster.isMissingNode()) {
					obra.setPosterPath(poster.asText());
				}

				// Extrae el año de la fecha de estreno.
				String campoFecha = "PELICULA".equals(tipo) ? "release_date" : "first_air_date";

				String fecha = resultado.path(campoFecha).asText();

				if (fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
					obra.setAnio(Integer.parseInt(fecha.substring(0, 4)));
				}

				// Recoge los identificadores de generos de TMDB.
				List<Integer> idsGeneros = new ArrayList<>();

				for (JsonNode genero : resultado.path("genre_ids")) {
					idsGeneros.add(genero.asInt());
				}

				// Convierte los generos al formato de Shelfie.
				obra.setGeneros(generoMapper.convertirGeneros(idsGeneros));

				obras.add(obra);
			}

		} catch (tools.jackson.core.JacksonException e) {

			// Informa de que la respuesta recibida no
			// se ha podido interpretar correctamente.
			throw new IllegalStateException("Error al procesar la respuesta de TMDB", e);
		}

		return obras;
	}
}
