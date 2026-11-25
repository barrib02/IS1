
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.function.Function;
import java.net.URI;
import org.mockito.ArgumentCaptor;
import org.springframework.web.util.UriComponentsBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

class TmdbServiceTest {

	private RestClient restClient;
	private RestClient.RequestHeadersUriSpec<?> peticion;
	private RestClient.RequestHeadersSpec<?> respuestaPeticion;
	private RestClient.ResponseSpec respuestaHttp;
	private TmdbService tmdbService;

	@BeforeEach
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void preparar() {

		// Simulamos las dependencias HTTP de TMDB.
		restClient = mock(RestClient.class);
		peticion = mock(RestClient.RequestHeadersUriSpec.class);
		respuestaPeticion = mock(RestClient.RequestHeadersSpec.class);
		respuestaHttp = mock(RestClient.ResponseSpec.class);

		when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) peticion);

		when(peticion.uri(any(Function.class))).thenReturn((RestClient.RequestHeadersSpec) respuestaPeticion);

		when(respuestaPeticion.retrieve()).thenReturn(respuestaHttp);

		// Creamos el servicio real con dependencias simuladas.
		tmdbService = new TmdbService("clave-prueba", restClient);
	}

	@Test
	void debeObtenerDetallesDePelicula() {

		// Preparamos una respuesta simulada.
		String json = "{\"id\":671,\"title\":\"Harry Potter\"}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		// Ejecutamos el método real de TmdbService.
		String resultado = tmdbService.obtenerPelicula("671");

		// Verificamos el resultado.
		assertEquals(json, resultado);

		verify(restClient).get();
		verify(respuestaHttp).body(String.class);
	}

	@Test
	void debeObtenerDetallesDeSerie() {

		String json = "{\"id\":1399,\"name\":\"Juego de tronos\"}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		// Ejecutamos el método real de TmdbService.
		String resultado = tmdbService.obtenerSerie("1399");

		assertEquals(json, resultado);

		verify(restClient).get();
		verify(respuestaHttp).body(String.class);
	}

	@Test
	void obtenerPlataformasPelicula_devuelveRespuestaTmdb() {

		// Simulamos la respuesta de TMDB para una película.
		String respuestaJson = """
				{
				    "results": {
				        "ES": {
				            "flatrate": [
				                {"provider_name": "Netflix"}
				            ]
				        }
				    }
				}
				""";

		when(respuestaHttp.body(String.class)).thenReturn(respuestaJson);

		// Consultamos las plataformas de la película.
		String resultado = tmdbService.obtenerPlataformasPelicula("671");

		// Comprobamos que devuelve la respuesta esperada.
		assertEquals(respuestaJson, resultado);
	}

	@Test
	void obtenerPlataformasSerie_devuelveRespuestaTmdb() {

		// Simulamos la respuesta de TMDB para una serie.
		String respuestaJson = """
				{
				    "results": {
				        "ES": {
				            "rent": [
				                {"provider_name": "Amazon Video"}
				            ]
				        }
				    }
				}
				""";

		when(respuestaHttp.body(String.class)).thenReturn(respuestaJson);

		// Consultamos las plataformas de la serie.
		String resultado = tmdbService.obtenerPlataformasSerie("1399");

		// Comprobamos que devuelve la respuesta esperada.
		assertEquals(respuestaJson, resultado);
	}

	@Test
	void debeBuscarPeliculas() {

		String json = "{\"results\":[{\"id\":671}]}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		assertEquals(json, tmdbService.buscarPeliculas("Harry Potter"));

		verify(restClient).get();
	}

	@Test
	void debeBuscarSeries() {

		String json = "{\"results\":[{\"id\":1399}]}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		assertEquals(json, tmdbService.buscarSeries("Juego de tronos"));

		verify(restClient).get();
	}

	@Test
	void debeObtenerPeliculasPopulares() {

		String json = "{\"results\":[{\"id\":597}]}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		assertEquals(json, tmdbService.obtenerPeliculasPopulares());

		verify(restClient).get();
	}

	@Test
	void debeObtenerSeriesPopulares() {

		String json = "{\"results\":[{\"id\":1399}]}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		assertEquals(json, tmdbService.obtenerSeriesPopulares());

		verify(restClient).get();
	}

	@Test
	void debeObtenerPeliculasPorGenero() {

		String json = "{\"results\":[{\"id\":671}]}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		assertEquals(json, tmdbService.obtenerPeliculasPorGenero("14"));

		verify(restClient).get();
	}

	@Test
	void debeObtenerSeriesPorGenero() {

		String json = "{\"results\":[{\"id\":1399}]}";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		assertEquals(json, tmdbService.obtenerSeriesPorGenero("18"));

		verify(restClient).get();
	}

	@Test
	void debePropagarErrorDeLaApi() {

		when(respuestaHttp.body(String.class))
				.thenThrow(new org.springframework.web.client.RestClientException("TMDB no disponible"));

		assertThrows(org.springframework.web.client.RestClientException.class,
				() -> tmdbService.obtenerPeliculasPopulares());
	}

	@Test
	void debeObtenerEpisodiosDeUnaTemporada() {

		String json = """
				{
				    "season_number": 2,
				    "episodes": [
				        {"episode_number": 1, "name": "Episodio 1"},
				        {"episode_number": 2, "name": "Episodio 2"}
				    ]
				}
				""";

		when(respuestaHttp.body(String.class)).thenReturn(json);

		String resultado = tmdbService.obtenerTemporada("1399", 2);

		assertEquals(json, resultado);

		verify(restClient).get();
		verify(respuestaHttp).body(String.class);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void debeConstruirUriCorrectaParaTemporada() {

		when(respuestaHttp.body(String.class)).thenReturn("{}");

		tmdbService.obtenerTemporada("1399", 2);

		ArgumentCaptor<Function<UriBuilder, URI>> captor = ArgumentCaptor.forClass((Class) Function.class);

		verify(peticion).uri(captor.capture());

		URI uri = captor.getValue().apply(UriComponentsBuilder.fromUriString("https://api.themoviedb.org/3"));

		assertEquals("/3/tv/1399/season/2", uri.getPath());

		assertTrue(uri.getQuery().contains("api_key=clave-prueba"));
		assertTrue(uri.getQuery().contains("language=es-ES"));
	}

	@Test
	void debePropagarErrorAlConsultarTemporada() {

		when(respuestaHttp.body(String.class))
				.thenThrow(new org.springframework.web.client.RestClientException("TMDB no disponible"));

		assertThrows(org.springframework.web.client.RestClientException.class,
				() -> tmdbService.obtenerTemporada("1399", 2));
	}

}
