
package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class GoogleBooksService {

	private final RestClient restClient;
	private final String apiKey;

	@Autowired
	public GoogleBooksService(@Value("${googlebooks.base-url}") String baseUrl,
			@Value("${GOOGLE_BOOKS_API_KEY}") String apiKey) {

		this.restClient = RestClient.builder().baseUrl(baseUrl).build();

		this.apiKey = apiKey;
	}

	// Constructor utilizado en las pruebas unitarias.
	GoogleBooksService(String apiKey, RestClient restClient) {
		this.apiKey = apiKey;
		this.restClient = restClient;
	}

	public String buscarLibros(String titulo) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/volumes").queryParam("q", "intitle:" + titulo)
						.queryParam("printType", "books").queryParam("key", apiKey).build())
				.retrieve().body(String.class);
	}

	public String obtenerLibro(String idExterno) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/volumes/{id}").queryParam("key", apiKey).build(idExterno))
				.retrieve().body(String.class);
	}

	// Obtiene recomendaciones de libros en español.
	public String obtenerLibrosRecomendados() {
		return buscarLibrosPorTematica("fiction");
	}

	// Obtiene libros de una temática determinada.
	public String buscarLibrosPorTematica(String tematica) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/volumes").queryParam("q", "subject:" + tematica)
						.queryParam("printType", "books").queryParam("langRestrict", "es").queryParam("maxResults", 10)
						.queryParam("key", apiKey).build())
				.retrieve().body(String.class);
	}
}
