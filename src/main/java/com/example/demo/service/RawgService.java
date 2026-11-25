package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RawgService {

	private final RestClient restClient;
	private final String apiKey;

	@Autowired
	public RawgService(@Value("${rawg.base-url}") String baseUrl, @Value("${rawg.api-key}") String apiKey) {

		this.restClient = RestClient.builder().baseUrl(baseUrl).build();

		this.apiKey = apiKey;
	}

	// Constructor para las pruebas unitarias.
	RawgService(String apiKey, RestClient restClient) {
		this.apiKey = apiKey;
		this.restClient = restClient;
	}

	// Busca videojuegos por título.
	public String buscarVideojuegos(String titulo) {

		return restClient
				.get().uri(uriBuilder -> uriBuilder.path("/games").queryParam("key", apiKey)
						.queryParam("search", titulo).queryParam("page_size", 20).build())
				.retrieve().body(String.class);
	}

	// Obtiene la ficha completa de un videojuego.
	public String obtenerVideojuego(String idExterno) {

		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/games/{id}").queryParam("key", apiKey).build(idExterno)).retrieve()
				.body(String.class);
	}

	// Obtiene videojuegos populares para Discover.
	public String obtenerVideojuegosPopulares() {

		return restClient
				.get().uri(uriBuilder -> uriBuilder.path("/games").queryParam("key", apiKey)
						.queryParam("ordering", "-added").queryParam("page_size", 10).build())
				.retrieve().body(String.class);
	}

	// Obtiene videojuegos por género.
	public String obtenerVideojuegosPorGenero(String genero) {

		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/games").queryParam("key", apiKey).queryParam("genres", genero)
						.queryParam("ordering", "-added").queryParam("page_size", 10).build())
				.retrieve().body(String.class);
	}

	// Obtiene las tiendas asociadas al videojuego.
	public String obtenerTiendasVideojuego(String idExterno) {

		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/games/{id}/stores").queryParam("key", apiKey).build(idExterno))
				.retrieve().body(String.class);
	}
}