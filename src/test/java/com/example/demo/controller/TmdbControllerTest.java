
package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.service.TmdbService;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TmdbControllerTest {

	private TmdbService tmdbService;
	private TmdbController tmdbController;

	@BeforeEach
	void preparar() {

		// Simulamos el servicio externo con Mockito.
		tmdbService = mock(TmdbService.class);

		// Creamos el controlador real que queremos probar.
		tmdbController = new TmdbController(tmdbService);
	}

	@Test
	void debeDevolverDetallesDePelicula() {

		// Preparamos la respuesta simulada del servicio.
		String json = "{\"id\":671,\"title\":\"Harry Potter\"}";

		when(tmdbService.obtenerPelicula("671")).thenReturn(json);

		// Ejecutamos el método real del controlador.
		String resultado = tmdbController.obtenerPelicula("671");

		// Comprobamos que devuelve la información esperada.
		assertEquals(json, resultado);

		// Verificamos que llama al servicio con el ID correcto.
		verify(tmdbService).obtenerPelicula("671");
		verifyNoMoreInteractions(tmdbService);
	}

	@Test
	void debeDevolverDetallesDeSerie() {

		String json = "{\"id\":1399,\"name\":\"Juego de tronos\"}";

		when(tmdbService.obtenerSerie("1399")).thenReturn(json);

		// Ejecutamos el método real del controlador.
		String resultado = tmdbController.obtenerSerie("1399");

		assertEquals(json, resultado);

		verify(tmdbService).obtenerSerie("1399");
		verifyNoMoreInteractions(tmdbService);
	}

	@Test
	void debeObtenerPlataformasDePelicula() {

		// Simulamos la respuesta de TMDB para una película.
		String json = """
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

		// Simulamos la respuesta del servicio.
		when(tmdbService.obtenerPlataformasPelicula("671")).thenReturn(json);

		// Ejecutamos el método real del controlador.
		String resultado = tmdbController.obtenerPlataformasPelicula("671");

		// Comprobamos que devuelve los datos correctamente.
		assertEquals(json, resultado);

		// Verificamos que utiliza el servicio correspondiente.
		verify(tmdbService).obtenerPlataformasPelicula("671");
	}

	@Test
	void debeObtenerPlataformasDeSerie() {

		// Simulamos la respuesta de TMDB para una serie.
		String json = """
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

		// Simulamos la respuesta del servicio.
		when(tmdbService.obtenerPlataformasSerie("1399")).thenReturn(json);

		// Ejecutamos el método real del controlador.
		String resultado = tmdbController.obtenerPlataformasSerie("1399");

		// Comprobamos que devuelve los datos correctamente.
		assertEquals(json, resultado);

		// Verificamos que utiliza el servicio correspondiente.
		verify(tmdbService).obtenerPlataformasSerie("1399");
	}

	@Test
	void debeDevolverEpisodiosDeUnaTemporada() {

		String json = """
				{
				    "season_number": 2,
				    "episodes": [
				        {"episode_number": 1, "name": "Episodio 1"},
				        {"episode_number": 2, "name": "Episodio 2"}
				    ]
				}
				""";

		when(tmdbService.obtenerTemporada("1399", 2)).thenReturn(json);

		String resultado = tmdbController.obtenerTemporada("1399", 2);

		assertEquals(json, resultado);

		verify(tmdbService).obtenerTemporada("1399", 2);
		verifyNoMoreInteractions(tmdbService);
	}

	@Test
	void debePropagarErrorAlConsultarTemporada() {

		when(tmdbService.obtenerTemporada("1399", 2))
				.thenThrow(new org.springframework.web.client.RestClientException("TMDB no disponible"));

		assertThrows(org.springframework.web.client.RestClientException.class,
				() -> tmdbController.obtenerTemporada("1399", 2));

		verify(tmdbService).obtenerTemporada("1399", 2);
		verifyNoMoreInteractions(tmdbService);
	}

}
