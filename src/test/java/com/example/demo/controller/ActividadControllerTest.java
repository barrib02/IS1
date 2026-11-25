package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.controller.ActividadController;
import com.example.demo.dto.ActividadResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.service.ActividadService;
import com.example.demo.service.PerfilService;

class ActividadControllerTest {

    @Test
    void obtieneActividadDelUsuarioAutenticado() {

        ActividadService actividadService = mock(ActividadService.class);
        PerfilService perfilService = mock(PerfilService.class);

        ActividadController controller =
                new ActividadController(actividadService, perfilService);

        Principal principal = () -> "prueba@test.com";

        Usuario usuario = mock(Usuario.class);

        when(perfilService.obtenerUsuarioPorEmail("prueba@test.com"))
                .thenReturn(usuario);

        when(usuario.getId()).thenReturn(1L);

        ActividadResponse actividad = new ActividadResponse(
                1L,
                1L,
                10L,
                "Oppenheimer",
                "PELICULA",
                "VALORACION",
                "Ha valorado una obra",
                LocalDateTime.now()
        );

        when(actividadService.obtenerHistorial(1L))
                .thenReturn(List.of(actividad));

        List<ActividadResponse> resultado =
                controller.obtenerMiActividad(principal);

        assertEquals(1, resultado.size());
        assertEquals("VALORACION", resultado.get(0).tipo());
        assertEquals("Oppenheimer", resultado.get(0).tituloObra());
        assertEquals("PELICULA", resultado.get(0).tipoObra());
        assertEquals(10L, resultado.get(0).obraId());
        assertNotNull(resultado.get(0).fecha());

        verify(perfilService).obtenerUsuarioPorEmail("prueba@test.com");
        verify(actividadService).obtenerHistorial(1L);
    }

    @Test
    void devuelveListaVaciaSiNoHayActividad() {

        ActividadService actividadService = mock(ActividadService.class);
        PerfilService perfilService = mock(PerfilService.class);

        ActividadController controller =
                new ActividadController(actividadService, perfilService);

        Principal principal = () -> "prueba@test.com";

        Usuario usuario = mock(Usuario.class);

        when(perfilService.obtenerUsuarioPorEmail("prueba@test.com"))
                .thenReturn(usuario);

        when(usuario.getId()).thenReturn(1L);

        when(actividadService.obtenerHistorial(1L))
                .thenReturn(List.of());

        List<ActividadResponse> resultado =
                controller.obtenerMiActividad(principal);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(actividadService).obtenerHistorial(1L);
    }

    @Test
    void consultaActividadSoloDelUsuarioAutenticado() {

        ActividadService actividadService = mock(ActividadService.class);
        PerfilService perfilService = mock(PerfilService.class);

        ActividadController controller =
                new ActividadController(actividadService, perfilService);

        Principal principal = () -> "usuario1@test.com";

        Usuario usuario = mock(Usuario.class);

        when(perfilService.obtenerUsuarioPorEmail("usuario1@test.com"))
                .thenReturn(usuario);

        when(usuario.getId()).thenReturn(1L);

        when(actividadService.obtenerHistorial(1L))
                .thenReturn(List.of());

        controller.obtenerMiActividad(principal);

        verify(perfilService).obtenerUsuarioPorEmail("usuario1@test.com");
        verify(actividadService).obtenerHistorial(1L);

        verify(actividadService, never()).obtenerHistorial(2L);
        verifyNoMoreInteractions(actividadService);
    }
}