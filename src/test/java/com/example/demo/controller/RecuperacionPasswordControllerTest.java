
package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.controller.RecuperacionPasswordController;
import com.example.demo.dto.RecuperacionPasswordRequest;
import com.example.demo.dto.RestablecerPasswordRequest;
import com.example.demo.service.RecuperacionPasswordService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

class RecuperacionPasswordControllerTest {

	private RecuperacionPasswordService service;
	private RecuperacionPasswordController controller;
	private HttpServletRequest httpRequest;
	private HttpSession sesion;

	private static final String EMAIL = "usuario@example.com";
	private static final String TOKEN = "A".repeat(43);
	private static final String PASSWORD = "NuevaPassword123";

	@BeforeEach
	void setUp() {

		service = mock(RecuperacionPasswordService.class);
		httpRequest = mock(HttpServletRequest.class);
		sesion = mock(HttpSession.class);

		controller = new RecuperacionPasswordController(service);

		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void limpiarContexto() {

		SecurityContextHolder.clearContext();
	}

	private RecuperacionPasswordRequest crearSolicitud() {

		RecuperacionPasswordRequest request = new RecuperacionPasswordRequest();

		request.setEmail(EMAIL);

		return request;
	}

	private RestablecerPasswordRequest crearRestablecimiento() {

		RestablecerPasswordRequest request = new RestablecerPasswordRequest();

		request.setToken(TOKEN);
		request.setPassword(PASSWORD);

		return request;
	}

	@Test
	void debeAceptarSolicitudDeRecuperacion() {

		ResponseEntity<String> respuesta = controller.solicitarRecuperacion(crearSolicitud());

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());

		assertEquals("Si existe una cuenta verificada asociada a ese correo, "
				+ "recibirás un enlace para restablecer tu contraseña.", respuesta.getBody());

		verify(service).solicitarRecuperacion(EMAIL);
	}

	@Test
	void debeMantenerRespuestaGenericaSiFallaCorreo() {

		doThrow(new MailSendException("Error SMTP")).when(service).solicitarRecuperacion(EMAIL);

		ResponseEntity<String> respuesta = controller.solicitarRecuperacion(crearSolicitud());

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());

		assertEquals("Si existe una cuenta verificada asociada a ese correo, "
				+ "recibirás un enlace para restablecer tu contraseña.", respuesta.getBody());
	}

	@Test
	void debeRestablecerPasswordYCerrarSesion() {

		when(httpRequest.getSession(false)).thenReturn(sesion);

		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null, java.util.List.of()));

		ResponseEntity<String> respuesta = controller.restablecerPassword(crearRestablecimiento(), httpRequest);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());

		assertEquals("Contraseña actualizada correctamente. " + "Ya puedes iniciar sesión.", respuesta.getBody());

		verify(service).restablecerPassword(TOKEN, PASSWORD);
		verify(sesion).invalidate();

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void debeRestablecerPasswordAunqueNoExistaSesion() {

		when(httpRequest.getSession(false)).thenReturn(null);

		ResponseEntity<String> respuesta = controller.restablecerPassword(crearRestablecimiento(), httpRequest);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());

		verify(service).restablecerPassword(TOKEN, PASSWORD);
		verifyNoInteractions(sesion);
	}

	@Test
	void debeDevolverErrorSiTokenNoEsValido() {

		doThrow(new IllegalArgumentException("El enlace no es válido o ha caducado")).when(service)
				.restablecerPassword(TOKEN, PASSWORD);

		ResponseEntity<String> respuesta = controller.restablecerPassword(crearRestablecimiento(), httpRequest);

		assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());

		assertEquals("El enlace no es válido o ha caducado", respuesta.getBody());

		verifyNoInteractions(httpRequest, sesion);
	}

	@Test
	void debeDevolverErrorSiPasswordNoEsValida() {

		doThrow(new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres")).when(service)
				.restablecerPassword(TOKEN, PASSWORD);

		ResponseEntity<String> respuesta = controller.restablecerPassword(crearRestablecimiento(), httpRequest);

		assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());

		assertEquals("La contraseña debe tener al menos 8 caracteres", respuesta.getBody());

		verifyNoInteractions(httpRequest, sesion);
	}
}
