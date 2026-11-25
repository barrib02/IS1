package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.net.URI;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

class RawgServiceTest {

	private RestClient restClient;
	private RestClient.RequestHeadersUriSpec<?> uriSpec;
	private RestClient.RequestHeadersSpec<?> headersSpec;
	private RestClient.ResponseSpec responseSpec;

	private RawgService rawgService;

	private static final String JSON = """
			{"results":[{"id":123,"name":"Minecraft"}]}
			""";

	@BeforeEach
	void preparar() {

		restClient = mock(RestClient.class);
		uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
		headersSpec = mock(RestClient.RequestHeadersSpec.class);
		responseSpec = mock(RestClient.ResponseSpec.class);

		doReturn(uriSpec).when(restClient).get();

		doReturn(headersSpec).when(uriSpec).uri(any(Function.class));

		when(headersSpec.retrieve()).thenReturn(responseSpec);

		when(responseSpec.body(String.class)).thenReturn(JSON);

		rawgService = new RawgService("clave-de-prueba", restClient);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private URI obtenerUri() {

		ArgumentCaptor<Function<UriBuilder, URI>> captor = ArgumentCaptor.forClass((Class) Function.class);

		verify(uriSpec).uri(captor.capture());

		return captor.getValue().apply(UriComponentsBuilder.fromUriString("https://api.rawg.io/api"));
	}

	@Test
	void debeBuscarVideojuegos() {

		assertEquals(JSON, rawgService.buscarVideojuegos("Minecraft"));

		URI uri = obtenerUri();

		assertEquals("/api/games", uri.getPath());
		assertTrue(uri.getQuery().contains("search=Minecraft"));
		assertTrue(uri.getQuery().contains("key=clave-de-prueba"));
		assertTrue(uri.getQuery().contains("page_size=20"));
	}

	@Test
	void debeObtenerFichaVideojuego() {

		assertEquals(JSON, rawgService.obtenerVideojuego("123"));

		URI uri = obtenerUri();

		assertEquals("/api/games/123", uri.getPath());
		assertTrue(uri.getQuery().contains("key=clave-de-prueba"));
	}

	@Test
	void debeObtenerVideojuegosPopulares() {

		assertEquals(JSON, rawgService.obtenerVideojuegosPopulares());

		URI uri = obtenerUri();

		assertEquals("/api/games", uri.getPath());
		assertTrue(uri.getQuery().contains("ordering=-added"));
		assertTrue(uri.getQuery().contains("page_size=10"));
	}

	@Test
	void debeObtenerVideojuegosPorGenero() {

		assertEquals(JSON, rawgService.obtenerVideojuegosPorGenero("action"));

		URI uri = obtenerUri();

		assertEquals("/api/games", uri.getPath());
		assertTrue(uri.getQuery().contains("genres=action"));
		assertTrue(uri.getQuery().contains("page_size=10"));
	}

	@Test
	void debeObtenerTiendasVideojuego() {

		assertEquals(JSON, rawgService.obtenerTiendasVideojuego("123"));

		URI uri = obtenerUri();

		assertEquals("/api/games/123/stores", uri.getPath());
	}

	@Test
	void debePropagarErrorDeLaApi() {

		when(responseSpec.body(String.class)).thenThrow(new RestClientException("RAWG no disponible"));

		assertThrows(RestClientException.class, () -> rawgService.buscarVideojuegos("Minecraft"));
	}
}