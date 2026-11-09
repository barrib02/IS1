
package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.controller.ActividadController;
import com.example.demo.entity.Actividad;
import com.example.demo.entity.Usuario;
import com.example.demo.service.ActividadService;
import com.example.demo.service.PerfilService;

class ActividadControllerTest {

	@Test
	void obtieneActividadDelUsuarioAutenticado() {

		ActividadService actividadService = mock(ActividadService.class);
		PerfilService perfilService = mock(PerfilService.class);

		ActividadController controller = new ActividadController(actividadService, perfilService);

		Principal principal = () -> "prueba@test.com";

		Usuario usuario = mock(Usuario.class);

		when(perfilService.obtenerUsuarioPorEmail("prueba@test.com")).thenReturn(usuario);

		when(usuario.getId()).thenReturn(1L);

		Actividad actividad = new Actividad(1L, 10L, "VALORACION", "Ha valorado una obra");

		when(actividadService.obtenerActividadReciente(1L)).thenReturn(List.of(actividad));

		List<Actividad> resultado = controller.obtenerMiActividad(principal);

		assertEquals(1, resultado.size());
		assertEquals("VALORACION", resultado.get(0).getTipo());

		verify(perfilService).obtenerUsuarioPorEmail("prueba@test.com");
		verify(actividadService).obtenerActividadReciente(1L);
	}

	@Test
	void devuelveListaVaciaSiNoHayActividad() {

		ActividadService actividadService = mock(ActividadService.class);
		PerfilService perfilService = mock(PerfilService.class);

		ActividadController controller = new ActividadController(actividadService, perfilService);

		Principal principal = () -> "prueba@test.com";

		Usuario usuario = mock(Usuario.class);

		when(perfilService.obtenerUsuarioPorEmail("prueba@test.com")).thenReturn(usuario);

		when(usuario.getId()).thenReturn(1L);

		when(actividadService.obtenerActividadReciente(1L)).thenReturn(List.of());

		List<Actividad> resultado = controller.obtenerMiActividad(principal);

		assertNotNull(resultado);
		assertTrue(resultado.isEmpty());

		verify(actividadService).obtenerActividadReciente(1L);
	}

	@Test
	void consultaActividadSoloDelUsuarioAutenticado() {

		ActividadService actividadService = mock(ActividadService.class);
		PerfilService perfilService = mock(PerfilService.class);

		ActividadController controller = new ActividadController(actividadService, perfilService);

		Principal principal = () -> "usuario1@test.com";

		Usuario usuario = mock(Usuario.class);

		when(perfilService.obtenerUsuarioPorEmail("usuario1@test.com")).thenReturn(usuario);

		when(usuario.getId()).thenReturn(1L);

		when(actividadService.obtenerActividadReciente(1L)).thenReturn(List.of());

		controller.obtenerMiActividad(principal);

		verify(perfilService).obtenerUsuarioPorEmail("usuario1@test.com");
		verify(actividadService).obtenerActividadReciente(1L);

		verify(actividadService, never()).obtenerActividadReciente(2L);
	}

}
