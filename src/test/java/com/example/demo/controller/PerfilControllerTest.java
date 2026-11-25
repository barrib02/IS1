package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.example.demo.controller.PerfilController;
import com.example.demo.dto.PerfilResponse;
import com.example.demo.dto.PrivacidadRequest;
import com.example.demo.entity.PrivacidadPerfil;
import com.example.demo.service.PerfilService;

class PerfilControllerTest {

	@Test
	void cambiaPrivacidadDelUsuarioAutenticado() {

		PerfilService service = mock(PerfilService.class);
		PerfilController controller = new PerfilController(service);

		Authentication auth = mock(Authentication.class);

		when(auth.getName()).thenReturn("prueba@test.com");

		PrivacidadRequest request = new PrivacidadRequest();
		request.setPrivacidad(PrivacidadPerfil.PRIVADO);

		PerfilResponse respuesta = new PerfilResponse(1L, "Usuario", "Biografía", null, null);

		respuesta.setPrivacidadPerfil(PrivacidadPerfil.PRIVADO);

		when(service.actualizarPrivacidad("prueba@test.com", PrivacidadPerfil.PRIVADO)).thenReturn(respuesta);

		PerfilResponse resultado = controller.cambiarPrivacidad(request, auth);

		assertEquals(PrivacidadPerfil.PRIVADO, resultado.getPrivacidadPerfil());

		verify(service).actualizarPrivacidad("prueba@test.com", PrivacidadPerfil.PRIVADO);

		verifyNoMoreInteractions(service);
	}

	@Test
	void consultaPerfilIdentificandoAlSolicitante() {

		PerfilService service = mock(PerfilService.class);
		PerfilController controller = new PerfilController(service);

		Authentication auth = mock(Authentication.class);

		when(auth.getName()).thenReturn("visitante@test.com");

		PerfilResponse respuesta = new PerfilResponse(2L, "Otro usuario", "Biografía", null, null);

		when(service.obtenerPerfilPorId(2L, "visitante@test.com")).thenReturn(respuesta);

		PerfilResponse resultado = controller.perfilDeUsuario(2L, auth);

		assertEquals(2L, resultado.getId());

		verify(service).obtenerPerfilPorId(2L, "visitante@test.com");
	}

	@Test
	void obtienePerfilPropioDesdeLaSesion() {

		PerfilService service = mock(PerfilService.class);
		PerfilController controller = new PerfilController(service);

		Authentication auth = mock(Authentication.class);

		when(auth.getName()).thenReturn("prueba@test.com");

		PerfilResponse respuesta = new PerfilResponse(1L, "Usuario", "Biografía", null, null);

		when(service.obtenerMiPerfil("prueba@test.com")).thenReturn(respuesta);

		PerfilResponse resultado = controller.miPerfil(auth);

		assertEquals(1L, resultado.getId());

		verify(service).obtenerMiPerfil("prueba@test.com");
	}
}