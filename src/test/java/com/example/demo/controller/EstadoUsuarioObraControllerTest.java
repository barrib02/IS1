package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.Usuario;
import com.example.demo.service.EstadoUsuarioObraService;
import com.example.demo.service.PerfilService;

class EstadoUsuarioObraControllerTest {

	private EstadoUsuarioObraService service;
	private PerfilService perfilService;
	private EstadoUsuarioObraController controller;
	private Authentication auth;

	@BeforeEach
	void preparar() {

		service = mock(EstadoUsuarioObraService.class);
		perfilService = mock(PerfilService.class);
		auth = mock(Authentication.class);

		controller = new EstadoUsuarioObraController(service, perfilService);

		Usuario usuario = mock(Usuario.class);

		when(usuario.getId()).thenReturn(1L);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getName()).thenReturn("prueba@test.com");

		when(perfilService.obtenerUsuarioPorEmail("prueba@test.com")).thenReturn(usuario);
	}

	@Test
	void permiteConsultarEstadoPropio() {

		when(service.obtenerEstado(1L, 10L)).thenReturn(EstadoObra.EN_CURSO);

		EstadoObra resultado = controller.obtenerEstado(1L, 10L, auth);

		assertEquals(EstadoObra.EN_CURSO, resultado);

		verify(service).obtenerEstado(1L, 10L);
	}

	@Test
	void impideConsultarEstadoDeOtroUsuario() {

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> controller.obtenerEstado(2L, 10L, auth));

		assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());

		verifyNoInteractions(service);
	}

	@Test
	void permiteCambiarEstadoPropio() {

		String resultado = controller.cambiarEstado(1L, 10L, "EN_CURSO", null, auth);

		assertEquals("Estado actualizado", resultado);

		verify(service).cambiarEstado(1L, 10L, EstadoObra.EN_CURSO, null);
	}

	@Test
	void impideCambiarEstadoDeOtroUsuario() {

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> controller.cambiarEstado(2L, 10L, "EN_CURSO", null, auth));

		assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());

		verifyNoInteractions(service);
	}

	@Test
	void permiteGuardarFechaDeFinalizacion() {

		LocalDate fecha = LocalDate.of(2026, 11, 25);

		controller.cambiarEstado(1L, 10L, "COMPLETADA", fecha, auth);

		verify(service).cambiarEstado(1L, 10L, EstadoObra.COMPLETADA, fecha);
	}

	@Test
	void impideConsultarEstadoSinSesion() {

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> controller.obtenerEstado(1L, 10L, null));

		assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());

		verifyNoInteractions(service);
		verifyNoInteractions(perfilService);
	}

	@Test
	void impideCambiarEstadoSinSesion() {

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> controller.cambiarEstado(1L, 10L, "EN_CURSO", null, null));

		assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());

		verifyNoInteractions(service);
		verifyNoInteractions(perfilService);
	}

	@Test
	void rechazaEstadoIncorrecto() {

		assertThrows(IllegalArgumentException.class, () -> controller.cambiarEstado(1L, 10L, "INVENTADO", null, auth));

		verifyNoInteractions(service);
	}
}