
package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.TmdbService;

@RestController
@RequestMapping("/api/tmdb")
public class TmdbController {

	private final TmdbService tmdbService;

	public TmdbController(TmdbService tmdbService) {
		this.tmdbService = tmdbService;
	}

	// Devuelve los detalles de una película de TMDB.
	@GetMapping(value = "/pelicula/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String obtenerPelicula(@PathVariable String id) {
		return tmdbService.obtenerPelicula(id);
	}

	// Devuelve los detalles de una serie de TMDB.
	@GetMapping(value = "/serie/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String obtenerSerie(@PathVariable String id) {
		return tmdbService.obtenerSerie(id);
	}

	// Devuelve la información y los episodios de una temporada.
	@GetMapping(value = "/serie/{id}/temporada/{temporada}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String obtenerTemporada(@PathVariable String id, @PathVariable int temporada) {

		return tmdbService.obtenerTemporada(id, temporada);
	}

	/**
	 * Devuelve las plataformas disponibles para una película.
	 */
	@GetMapping(value = "/pelicula/{id}/plataformas", produces = MediaType.APPLICATION_JSON_VALUE)
	public String obtenerPlataformasPelicula(@PathVariable String id) {
		return tmdbService.obtenerPlataformasPelicula(id);
	}

	/**
	 * Devuelve las plataformas disponibles para una serie.
	 */
	@GetMapping(value = "/serie/{id}/plataformas", produces = MediaType.APPLICATION_JSON_VALUE)
	public String obtenerPlataformasSerie(@PathVariable String id) {
		return tmdbService.obtenerPlataformasSerie(id);
	}

}
