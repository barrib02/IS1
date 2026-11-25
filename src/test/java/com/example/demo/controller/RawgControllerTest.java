package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.service.RawgService;

class RawgControllerTest {

	private RawgService rawgService;
	private RawgController rawgController;

	@BeforeEach
	void preparar() {

		rawgService = mock(RawgService.class);
		rawgController = new RawgController(rawgService);
	}

	@Test
	void debeBuscarVideojuegos() {

		String json = """
				{"results":[{"id":123,"name":"Minecraft"}]}
				""";

		when(rawgService.buscarVideojuegos("Minecraft")).thenReturn(json);

		assertEquals(json, rawgController.buscarVideojuegos("Minecraft"));

		verify(rawgService).buscarVideojuegos("Minecraft");
	}

	@Test
	void debeObtenerFichaVideojuego() {

		String json = """
				{"id":123,"name":"Minecraft"}
				""";

		when(rawgService.obtenerVideojuego("123")).thenReturn(json);

		assertEquals(json, rawgController.obtenerVideojuego("123"));

		verify(rawgService).obtenerVideojuego("123");
	}

	@Test
	void debeObtenerTiendasVideojuego() {

		String json = """
				{"results":[{"id":1,"store_id":1}]}
				""";

		when(rawgService.obtenerTiendasVideojuego("123")).thenReturn(json);

		assertEquals(json, rawgController.obtenerTiendasVideojuego("123"));

		verify(rawgService).obtenerTiendasVideojuego("123");
	}
}