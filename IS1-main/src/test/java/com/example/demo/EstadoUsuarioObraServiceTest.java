package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.EstadoUsuarioObra;
import com.example.demo.repository.EstadoUsuarioObraRepository;
import com.example.demo.service.EstadoUsuarioObraService;
import com.example.demo.service.ActividadService;

class EstadoUsuarioObraServiceTest {

	@Test
	void obtieneEstadoExistente() {
		/* Se crea un repositorio falso */
		EstadoUsuarioObraRepository repo = mock(EstadoUsuarioObraRepository.class);

		/* Se crea el Service con el repositorio falso */
		ActividadService actividadService = mock(ActividadService.class);
		EstadoUsuarioObraService service = new EstadoUsuarioObraService(repo, actividadService);

		/*
		 * Simular un estado existente (creamos el objeto que queremos que encuentre el
		 * repositorio)
		 */
		EstadoUsuarioObra estado = new EstadoUsuarioObra(1L, 1L, EstadoObra.EN_CURSO);

		/*
		 * Cuando alguien llame a la funcion findByUsuarioAndObra(1L,1L), devuelve el
		 * objeto creado justo antes
		 */
		when(repo.findByUsuarioIdAndObraId(1L, 1L)).thenReturn(Optional.of(estado));

		/* Comprueba el resultado, tiene que devolver EN_CURSO */
		assertEquals(EstadoObra.EN_CURSO, service.obtenerEstado(1L, 1L));
	}

	@Test
	void devuelveNullSiNoExiste() { // Comprobar que devuelve null si esa obra no tiene un estado asignado
		/* Se crea un repositorio falso */
		EstadoUsuarioObraRepository repo = mock(EstadoUsuarioObraRepository.class);

		/* Se crea el Service con el repositorio falso */
		ActividadService actividadService = mock(ActividadService.class);
		EstadoUsuarioObraService service = new EstadoUsuarioObraService(repo, actividadService);

		/*
		 * Cuando alguien llame a findByUsuarioIdAndObraId(1L,1L), devuelve
		 * Optional.empty()
		 */
		when(repo.findByUsuarioIdAndObraId(1L, 1L)).thenReturn(Optional.empty());

		/* Comprueba que el resultado sea null */
		assertNull(service.obtenerEstado(1L, 1L));
	}

	@Test
	void registraActividadAlCambiarEstado() {

		EstadoUsuarioObraRepository repo = mock(EstadoUsuarioObraRepository.class);

		ActividadService actividadService = mock(ActividadService.class);

		EstadoUsuarioObraService service = new EstadoUsuarioObraService(repo, actividadService);

		when(repo.findByUsuarioIdAndObraId(1L, 10L)).thenReturn(Optional.empty());

		service.cambiarEstado(1L, 10L, EstadoObra.EN_CURSO);

		verify(actividadService).registrarActividad(1L, 10L, "ESTADO", "Ha cambiado el estado de una obra a EN_CURSO");
	}

	@Test
	void mismoEstadoNoRegistraActividadDuplicada() {

		EstadoUsuarioObraRepository repo = mock(EstadoUsuarioObraRepository.class);

		ActividadService actividadService = mock(ActividadService.class);

		EstadoUsuarioObraService service = new EstadoUsuarioObraService(repo, actividadService);

		EstadoUsuarioObra estado = new EstadoUsuarioObra(1L, 10L, EstadoObra.EN_CURSO);

		when(repo.findByUsuarioIdAndObraId(1L, 10L)).thenReturn(Optional.of(estado));

		service.cambiarEstado(1L, 10L, EstadoObra.EN_CURSO);

		verify(actividadService, never()).registrarActividad(anyLong(), anyLong(), anyString(), anyString());

		verify(repo, never()).save(any(EstadoUsuarioObra.class));
	}

}