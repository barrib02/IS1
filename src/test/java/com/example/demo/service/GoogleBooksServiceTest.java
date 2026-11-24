package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

class GoogleBooksServiceTest {

	private RestClient restClient;
	private RestClient.RequestHeadersUriSpec<?> peticion;
	private RestClient.RequestHeadersSpec<?> respuestaPeticion;
	private RestClient.ResponseSpec respuestaHttp;
	private GoogleBooksService googleBooksService;

	private String uriConsultada;

	@BeforeEach
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void preparar() {

		restClient = mock(RestClient.class);
		peticion = mock(RestClient.RequestHeadersUriSpec.class);
		respuestaPeticion = mock(RestClient.RequestHeadersSpec.class);
		respuestaHttp = mock(RestClient.ResponseSpec.class);

		when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) peticion);

		when(peticion.uri(any(Function.class))).thenAnswer(invocacion -> {

			Function<UriBuilder, java.net.URI> funcion = invocacion.getArgument(0);

			uriConsultada = funcion.apply(UriComponentsBuilder.fromUriString("https://www.googleapis.com/books/v1"))
					.toString();

			return respuestaPeticion;
		});

		when(respuestaPeticion.retrieve()).thenReturn(respuestaHttp);

		googleBooksService = new GoogleBooksService("clave-prueba", restClient);
	}

	@Test
	void debeBuscarLibrosPorTitulo() {

		String json = """
				{
				    "totalItems": 1,
				    "items": [
				        {
				            "id": "libro123",
				            "volumeInfo": {
				                "title": "Harry Potter"
				            }
				        }
				    ]
				}
				""";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		String resultado = googleBooksService.buscarLibros("Harry Potter");

		assertEquals(json, resultado);
		assertTrue(uriConsultada.contains("/volumes"));
		assertTrue(uriConsultada.contains("q=intitle:Harry%20Potter"));
		assertTrue(uriConsultada.contains("printType=books"));
		assertTrue(uriConsultada.contains("key=clave-prueba"));

		verify(restClient).get();
	}

	@Test
	void debeObtenerDetallesDeLibro() {

		String json = """
				{
				    "id": "libro123",
				    "volumeInfo": {
				        "title": "Harry Potter",
				        "pageCount": 256
				    }
				}
				""";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		String resultado = googleBooksService.obtenerLibro("libro123");

		assertEquals(json, resultado);
		assertTrue(uriConsultada.contains("/volumes/libro123"));

		verify(restClient).get();
	}

	@Test
	void debeBuscarLibrosPorTematica() {

		String json = "{\"totalItems\":1,\"items\":[]}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		String resultado = googleBooksService.buscarLibrosPorTematica("fantasy");

		assertEquals(json, resultado);
		assertTrue(uriConsultada.contains("q=subject:fantasy"));
		assertTrue(uriConsultada.contains("langRestrict=es"));
		assertTrue(uriConsultada.contains("maxResults=10"));
	}

	@Test
	void debeObtenerLibrosParaEstantes() {

		String json = "{\"items\":[]}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		String resultado = googleBooksService.obtenerLibrosRecomendados();

		assertEquals(json, resultado);
		assertTrue(uriConsultada.contains("q=subject:fiction"));
	}

	@Test
	void debeDevolverBusquedaSinResultados() {

		String json = "{\"totalItems\":0}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		String resultado = googleBooksService.buscarLibros("TituloInexistente");

		assertEquals(json, resultado);
	}

	@Test
	void debePropagarErrorDeLaApi() {

		when(respuestaHttp.body(String.class)).thenThrow(new RestClientException("API no disponible"));

		assertThrows(RestClientException.class, () -> googleBooksService.buscarLibros("Harry Potter"));
	}
}