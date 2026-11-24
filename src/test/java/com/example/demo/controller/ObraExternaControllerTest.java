
package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.service.ObraExternaService;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ObraExternaControllerTest {

	private ObraExternaService obraExternaService;
	private ObraExternaController controlador;
	private Authentication usuario;

	@BeforeEach
	void preparar() {
		obraExternaService = mock(ObraExternaService.class);
		controlador = new ObraExternaController(obraExternaService);

		usuario = new UsernamePasswordAuthenticationToken("usuario", null, java.util.Collections.emptyList());

	}

	@Test
	void creaReferenciaDePeliculaTmdb() {
		Obra obra = new Obra();
		obra.setId(62L);

		when(obraExternaService.obtenerOCrearReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal")).thenReturn(obra);

		var peticion = new ObraExternaController.ReferenciaExternaRequest("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal");

		var respuesta = controlador.obtenerOCrearReferencia(peticion, usuario);

		assertEquals(62L, respuesta.id());

		verify(obraExternaService).obtenerOCrearReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal");
	}

	@Test
	void permiteCrearReferenciaDeSerieTmdb() {
		Obra obra = new Obra();
		obra.setId(80L);

		when(obraExternaService.obtenerOCrearReferencia("TMDB", "1399", TipoObra.SERIE, "Juego de tronos"))
				.thenReturn(obra);

		var peticion = new ObraExternaController.ReferenciaExternaRequest("TMDB", "1399", TipoObra.SERIE,
				"Juego de tronos");

		var respuesta = controlador.obtenerOCrearReferencia(peticion, usuario);

		assertEquals(80L, respuesta.id());
	}

	@Test
	void rechazaUsuariosSinAutenticacion() {
		var peticion = new ObraExternaController.ReferenciaExternaRequest("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal");

		assertThrows(SecurityException.class, () -> controlador.obtenerOCrearReferencia(peticion, null));

		verifyNoInteractions(obraExternaService);
	}

	@Test
	void rechazaUsuariosAnonimos() {
		Authentication anonimo = new UsernamePasswordAuthenticationToken("anonymousUser", null);

		var peticion = new ObraExternaController.ReferenciaExternaRequest("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal");

		assertThrows(SecurityException.class, () -> controlador.obtenerOCrearReferencia(peticion, anonimo));

		verifyNoInteractions(obraExternaService);
	}

	@Test
	void buscarReferenciaExistenteDevuelveId() {

		Obra obra = new Obra();
		obra.setId(25L);

		when(obraExternaService.buscarReferencia("TMDB", "597", TipoObra.PELICULA)).thenReturn(Optional.of(obra));

		var respuesta = controlador.buscarReferencia("TMDB", "597", TipoObra.PELICULA);

		assertEquals(25L, respuesta.id());

		verify(obraExternaService).buscarReferencia("TMDB", "597", TipoObra.PELICULA);
	}

	@Test
	void buscarReferenciaInexistenteDevuelve404() {

		when(obraExternaService.buscarReferencia("TMDB", "597", TipoObra.PELICULA)).thenReturn(Optional.empty());

		ResponseStatusException excepcion = assertThrows(ResponseStatusException.class,
				() -> controlador.buscarReferencia("TMDB", "597", TipoObra.PELICULA));

		assertEquals(HttpStatus.NOT_FOUND, excepcion.getStatusCode());

		verify(obraExternaService, never()).obtenerOCrearReferencia(any(), any(), any(), any());
	}

}
