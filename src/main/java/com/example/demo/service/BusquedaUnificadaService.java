
package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.SearchResultDTO;
import com.example.demo.dto.TmdbObraDTO;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import org.springframework.web.client.RestClientException;

import java.util.HashSet;
import java.util.Set;

@Service
public class BusquedaUnificadaService {

	private final ObraService obraService;
	private final TmdbService tmdbService;
	private final TmdbObraMapper tmdbObraMapper;
	private final TmdbResultadoMapper tmdbResultadoMapper;

	public BusquedaUnificadaService(ObraService obraService, TmdbService tmdbService, TmdbObraMapper tmdbObraMapper,
			TmdbResultadoMapper tmdbResultadoMapper) {

		this.obraService = obraService;
		this.tmdbService = tmdbService;
		this.tmdbObraMapper = tmdbObraMapper;
		this.tmdbResultadoMapper = tmdbResultadoMapper;
	}

	// Combina las obras de Neon con los resultados de TMDB.
	public List<SearchResultDTO> buscar(String titulo, TipoObra tipo) {

		List<SearchResultDTO> resultados = new ArrayList<>();
		// Identificadores externos de las obras ya incluidas.
		Set<String> referenciasEncontradas = new HashSet<>();

		// Primero recuperamos las obras existentes en Neon.
		for (Obra obra : obraService.buscar(titulo, tipo)) {

			SearchResultDTO resultado = new SearchResultDTO(obra.getId(), obra.getTitulo(),
					obra.getTipo() == null ? "" : obra.getTipo().name());

			resultado.setFuenteApi(obra.getFuenteApi());
			resultado.setIdExterno(obra.getIdExterno());
			resultado.setImagenUrl(obra.getImagenUrl());

			resultados.add(resultado);
			// Registramos la referencia para evitar duplicados de TMDB.
			if (obra.getFuenteApi() != null && obra.getIdExterno() != null) {
				referenciasEncontradas.add(obra.getFuenteApi() + ":" + obra.getTipo() + ":" + obra.getIdExterno());
			}
		}
		// Sin título, mostramos únicamente las obras de Neon.
		// Evitamos realizar consultas innecesarias a TMDB.
		if (titulo == null || titulo.isBlank()) {
			return resultados;
		}

		if (tipo == null || tipo == TipoObra.PELICULA) {

			try {
				String json = tmdbService.buscarPeliculas(titulo);

				List<TmdbObraDTO> peliculas = tmdbObraMapper.convertirResultados(json, "PELICULA");

				for (TmdbObraDTO pelicula : peliculas) {

					SearchResultDTO resultado = tmdbResultadoMapper.convertir(pelicula);

					String referencia = "TMDB:PELICULA:" + resultado.getIdExterno();

					// Evitamos mostrar obras duplicadas.
					if (referenciasEncontradas.add(referencia)) {
						resultados.add(resultado);
					}
				}

			} catch (RestClientException | IllegalStateException e) {

				// Si TMDB falla, conservamos los resultados de Neon.
				System.err.println("Error al consultar películas de TMDB: " + e.getMessage());
			}
		}

		if (tipo == null || tipo == TipoObra.SERIE) {

			try {
				String json = tmdbService.buscarSeries(titulo);

				List<TmdbObraDTO> series = tmdbObraMapper.convertirResultados(json, "SERIE");

				for (TmdbObraDTO serie : series) {

					SearchResultDTO resultado = tmdbResultadoMapper.convertir(serie);

					String referencia = "TMDB:SERIE:" + resultado.getIdExterno();

					// Evitamos mostrar obras duplicadas.
					if (referenciasEncontradas.add(referencia)) {
						resultados.add(resultado);
					}
				}

			} catch (RestClientException | IllegalStateException e) {

				// Si TMDB falla, conservamos los resultados de Neon.
				System.err.println("Error al consultar series de TMDB: " + e.getMessage());
			}
		}

		return resultados;
	}
}
