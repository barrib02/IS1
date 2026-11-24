package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.service.GoogleBooksService;

class GoogleBooksControllerTest {

	private GoogleBooksService googleBooksService;
	private GoogleBooksController googleBooksController;

	@BeforeEach
	void preparar() {

		googleBooksService = mock(GoogleBooksService.class);

		googleBooksController = new GoogleBooksController(googleBooksService);
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

		when(googleBooksService.buscarLibros("Harry Potter")).thenReturn(json);

		String resultado = googleBooksController.buscarLibros("Harry Potter");

		assertEquals(json, resultado);

		verify(googleBooksService).buscarLibros("Harry Potter");
		verifyNoMoreInteractions(googleBooksService);
	}

	@Test
	void debeDevolverDetallesDeLibro() {

		String json = """
				{
				    "id": "libro123",
				    "volumeInfo": {
				        "title": "Harry Potter",
				        "pageCount": 256
				    }
				}
				""";

		when(googleBooksService.obtenerLibro("libro123")).thenReturn(json);

		String resultado = googleBooksController.obtenerLibro("libro123");

		assertEquals(json, resultado);

		verify(googleBooksService).obtenerLibro("libro123");
		verifyNoMoreInteractions(googleBooksService);
	}

	@Test
	void debeDevolverBusquedaSinResultados() {

		String json = "{\"totalItems\":0}";

		when(googleBooksService.buscarLibros("Inexistente")).thenReturn(json);

		String resultado = googleBooksController.buscarLibros("Inexistente");

		assertEquals(json, resultado);

		verify(googleBooksService).buscarLibros("Inexistente");
	}
}