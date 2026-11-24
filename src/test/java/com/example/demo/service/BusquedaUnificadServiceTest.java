
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import com.example.demo.dto.SearchResultDTO;
import com.example.demo.dto.TmdbObraDTO;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;

@ExtendWith(MockitoExtension.class)
class BusquedaUnificadaServiceTest {

	@Mock
	private ObraService obraService;

	@Mock
	private TmdbService tmdbService;

	@Mock
	private TmdbObraMapper tmdbObraMapper;

	@Mock
	private TmdbResultadoMapper tmdbResultadoMapper;

	@InjectMocks
	private BusquedaUnificadaService busquedaService;

	@Test
	void debeCombinarNeonYTMDBSinDuplicados() {

		// Oppenheimer ya está registrada en Neon.
		Obra oppenheimer = new Obra();
		oppenheimer.setId(62L);
		oppenheimer.setTitulo("Oppenheimer");
		oppenheimer.setTipo(TipoObra.PELICULA);
		oppenheimer.setFuenteApi("TMDB");
		oppenheimer.setIdExterno("872585");

		when(obraService.buscar("Oppenheimer", TipoObra.PELICULA)).thenReturn(List.of(oppenheimer));

		// TMDB devuelve la misma película y otra diferente.
		TmdbObraDTO repetida = new TmdbObraDTO();
		TmdbObraDTO nueva = new TmdbObraDTO();

		when(tmdbService.buscarPeliculas("Oppenheimer")).thenReturn("JSON_SIMULADO");

		when(tmdbObraMapper.convertirResultados("JSON_SIMULADO", "PELICULA")).thenReturn(List.of(repetida, nueva));

		SearchResultDTO resultadoRepetido = new SearchResultDTO(null, "Oppenheimer", "PELICULA");
		resultadoRepetido.setFuenteApi("TMDB");
		resultadoRepetido.setIdExterno("872585");

		SearchResultDTO resultadoNuevo = new SearchResultDTO(null, "Otra película", "PELICULA");
		resultadoNuevo.setFuenteApi("TMDB");
		resultadoNuevo.setIdExterno("999999");

		when(tmdbResultadoMapper.convertir(repetida)).thenReturn(resultadoRepetido);

		when(tmdbResultadoMapper.convertir(nueva)).thenReturn(resultadoNuevo);

		// Ejecutamos la búsqueda unificada.
		List<SearchResultDTO> resultados = busquedaService.buscar("Oppenheimer", TipoObra.PELICULA);

		// Debe conservar la obra de Neon y añadir solo la nueva.
		assertEquals(2, resultados.size());
		assertEquals(62L, resultados.get(0).getId());
		assertEquals("999999", resultados.get(1).getIdExterno());

		// No debe consultar series si solo buscamos películas.
		verify(tmdbService, never()).buscarSeries(anyString());
	}

	@Test
	void noDebeConsultarTMDBSinTitulo() {

		// Simulamos una búsqueda sin texto.
		when(obraService.buscar(null, null)).thenReturn(List.of());

		List<SearchResultDTO> resultados = busquedaService.buscar(null, null);

		assertTrue(resultados.isEmpty());

		// Sin título, no realizamos llamadas externas.
		verifyNoInteractions(tmdbService, tmdbObraMapper, tmdbResultadoMapper);
	}

	@Test
	void debeBuscarPeliculasYSeriesSinFiltro() {

		when(obraService.buscar("Harry Potter", null)).thenReturn(List.of());

		when(tmdbService.buscarPeliculas("Harry Potter")).thenReturn("{\"results\":[]}");

		when(tmdbService.buscarSeries("Harry Potter")).thenReturn("{\"results\":[]}");

		when(tmdbObraMapper.convertirResultados("{\"results\":[]}", "PELICULA")).thenReturn(List.of());

		when(tmdbObraMapper.convertirResultados("{\"results\":[]}", "SERIE")).thenReturn(List.of());

		// Buscamos sin especificar tipo.
		busquedaService.buscar("Harry Potter", null);

		// Debe consultar ambas categorías.
		verify(tmdbService).buscarPeliculas("Harry Potter");
		verify(tmdbService).buscarSeries("Harry Potter");
	}

	@Test
	void noDebeConsultarTMDBParaLibros() {

		when(obraService.buscar("Harry Potter", TipoObra.LIBRO)).thenReturn(List.of());

		busquedaService.buscar("Harry Potter", TipoObra.LIBRO);

		// Google Books se integrará más adelante.
		verifyNoInteractions(tmdbService);
	}
}
