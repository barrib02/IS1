
package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.SearchResultDTO;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.service.ObraService;
import com.example.demo.service.BusquedaUnificadaService;

@ExtendWith(MockitoExtension.class)
class ObraControllerBusquedaTest {

	// Simulamos el servicio para no conectarnos a Neon.
	@Mock
	private ObraService obraService;
	@Mock
	private BusquedaUnificadaService busquedaUnificadaService;

	// Creamos el controlador utilizando el servicio simulado.
	@InjectMocks
	private ObraController obraController;

	@Test
	void debeDevolverResultadosDeBusquedaUnificada() {

		// Simulamos una película que ya existe en Neon.
		SearchResultDTO pelicula = new SearchResultDTO(62L, "Oppenheimer", "PELICULA");

		pelicula.setFuenteApi("TMDB");
		pelicula.setIdExterno("872585");

		// El servicio unificado devuelve la película.
		when(busquedaUnificadaService.buscar("Oppenheimer", TipoObra.PELICULA)).thenReturn(List.of(pelicula));

		// Ejecutamos el endpoint del controlador.
		List<SearchResultDTO> resultados = obraController.buscar("Oppenheimer", TipoObra.PELICULA);

		// Comprobamos que conserva los identificadores.
		assertEquals(1, resultados.size());
		assertEquals(62L, resultados.get(0).getId());
		assertEquals("TMDB", resultados.get(0).getFuenteApi());
		assertEquals("872585", resultados.get(0).getIdExterno());

		// Comprobamos que utiliza el nuevo servicio.
		verify(busquedaUnificadaService).buscar("Oppenheimer", TipoObra.PELICULA);

		// El controlador ya no consulta directamente Neon en /search.
		verify(obraService, never()).buscar(any(), any());
	}

	@Test
	void debeMostrarSugerenciasDeNeonYTMDB() {

		// Simulamos una película procedente de TMDB.
		SearchResultDTO pelicula = new SearchResultDTO(null, "Harry Potter y la piedra filosofal", "PELICULA");

		pelicula.setFuenteApi("TMDB");
		pelicula.setIdExterno("671");

		// El servicio unificado devuelve la película.
		when(busquedaUnificadaService.buscar("Harry Pot", null)).thenReturn(List.of(pelicula));

		// Ejecutamos el método de sugerencias.
		List<SearchResultDTO> sugerencias = obraController.obtenerSugerencias("Harry Pot");

		// Comprobamos que la sugerencia conserva sus datos.
		assertEquals(1, sugerencias.size());
		assertEquals("Harry Potter y la piedra filosofal", sugerencias.get(0).getTitulo());
		assertEquals("TMDB", sugerencias.get(0).getFuenteApi());
		assertEquals("671", sugerencias.get(0).getIdExterno());

		// Comprobamos que se utiliza el buscador unificado.
		verify(busquedaUnificadaService).buscar("Harry Pot", null);
	}

	@Test
	void noDebeBuscarSugerenciasConMenosDeDosCaracteres() {

		// No consultamos las APIs con búsquedas demasiado cortas.
		List<SearchResultDTO> sugerencias = obraController.obtenerSugerencias("H");

		assertTrue(sugerencias.isEmpty());

		verifyNoInteractions(busquedaUnificadaService);
	}

	@Test
	void debeLimitarLasSugerenciasAOchoResultados() {

		// Simulamos diez resultados procedentes del buscador.
		List<SearchResultDTO> resultados = new java.util.ArrayList<>();

		for (int i = 0; i < 10; i++) {
			resultados.add(new SearchResultDTO(null, "Película " + i, "PELICULA"));
		}

		when(busquedaUnificadaService.buscar("Harry", null)).thenReturn(resultados);

		List<SearchResultDTO> sugerencias = obraController.obtenerSugerencias("Harry");

		// El desplegable solo debe mostrar ocho resultados.
		assertEquals(8, sugerencias.size());
	}

}
