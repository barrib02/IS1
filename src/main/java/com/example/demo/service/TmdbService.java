
package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class TmdbService {

	private final RestClient restClient;
	private final String apiKey;

	@Autowired
	public TmdbService(@Value("${tmdb.api-key}") String apiKey, @Value("${tmdb.base-url}") String baseUrl) {

		this.apiKey = apiKey;

		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
	}

	// Constructor para pruebas unitarias con JUnit y Mockito.
	TmdbService(String apiKey, RestClient restClient) {
		this.apiKey = apiKey;
		this.restClient = restClient;
	}

	// Busca peliculas en TMDB por titulo.
	public String buscarPeliculas(String titulo) {

		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/search/movie").queryParam("api_key", apiKey)
						.queryParam("query", titulo).queryParam("language", "es-ES").build())
				.retrieve().body(String.class);
	}

	// Busca series en TMDB a partir del titulo introducido.
	// Devuelve los resultados en formato JSON y en español.
	public String buscarSeries(String titulo) {

		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/search/tv").queryParam("api_key", apiKey)
						.queryParam("query", titulo).queryParam("language", "es-ES").build())
				.retrieve().body(String.class);
	}

	/**
	 * Obtiene los detalles de una película desde TMDB.
	 */
	public String obtenerPelicula(String idExterno) {

		return restClient.get().uri(uriBuilder -> uriBuilder.path("/movie/{id}").queryParam("api_key", apiKey)
				.queryParam("language", "es-ES").build(idExterno)).retrieve().body(String.class);
	}

	/**
	 * Obtiene los detalles de una serie desde TMDB.
	 */
	public String obtenerSerie(String idExterno) {

		return restClient.get().uri(uriBuilder -> uriBuilder.path("/tv/{id}").queryParam("api_key", apiKey)
				.queryParam("language", "es-ES").build(idExterno)).retrieve().body(String.class);
	}

	/**
	 * Consulta las plataformas donde se puede ver una película. TMDB devuelve la
	 * disponibilidad organizada por países.
	 */
	public String obtenerPlataformasPelicula(String idExterno) {

		return restClient.get().uri(uriBuilder -> uriBuilder.path("/movie/{id}/watch/providers")
				.queryParam("api_key", apiKey).build(idExterno)).retrieve().body(String.class);
	}

	/**
	 * Consulta las plataformas donde se puede ver una serie. TMDB devuelve la
	 * disponibilidad organizada por países.
	 */
	public String obtenerPlataformasSerie(String idExterno) {

		return restClient.get().uri(uriBuilder -> uriBuilder.path("/tv/{id}/watch/providers")
				.queryParam("api_key", apiKey).build(idExterno)).retrieve().body(String.class);
	}

	// Obtiene películas populares.
	public String obtenerPeliculasPopulares() {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/movie/popular").queryParam("api_key", apiKey)
						.queryParam("language", "es-ES").queryParam("region", "ES").build())
				.retrieve().body(String.class);
	}

	// Obtiene series populares.
	public String obtenerSeriesPopulares() {
		return restClient.get().uri(uriBuilder -> uriBuilder.path("/tv/popular").queryParam("api_key", apiKey)
				.queryParam("language", "es-ES").build()).retrieve().body(String.class);
	}

	// Obtiene películas de un género determinado.
	public String obtenerPeliculasPorGenero(String genero) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/discover/movie").queryParam("api_key", apiKey)
						.queryParam("language", "es-ES").queryParam("region", "ES").queryParam("with_genres", genero)
						.queryParam("sort_by", "popularity.desc").build())
				.retrieve().body(String.class);
	}

	// Obtiene series de un género determinado.
	public String obtenerSeriesPorGenero(String genero) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/discover/tv").queryParam("api_key", apiKey)
						.queryParam("language", "es-ES").queryParam("with_genres", genero)
						.queryParam("sort_by", "popularity.desc").build())
				.retrieve().body(String.class);
	}
}
