package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.Valoracion;
import com.example.demo.repository.ValoracionRepository;
import com.example.demo.service.ValoracionService;
import com.example.demo.service.ActividadService;
import java.util.Optional;

class ValoracionServiceTest {

	@Test
	void noPermiteMasDeCincoEstrellas() {

		ValoracionRepository repositorio = mock(ValoracionRepository.class);
		ActividadService actividadService = mock(ActividadService.class);
		ValoracionService servicio = new ValoracionService(repositorio, actividadService);

		Valoracion valoracion = new Valoracion();
		valoracion.setObraId(1L);
		valoracion.setPuntuacion(6);

		assertThrows(IllegalArgumentException.class, () -> {
			servicio.crearValoracion(valoracion);
		});
	}

	@Test
	void noPermitePuntuacionesNegativas() {

		ValoracionRepository repositorio = mock(ValoracionRepository.class);
		ActividadService actividadService = mock(ActividadService.class);
		ValoracionService servicio = new ValoracionService(repositorio, actividadService);

		Valoracion valoracion = new Valoracion();
		valoracion.setObraId(1L);
		valoracion.setPuntuacion(-1);

		assertThrows(IllegalArgumentException.class, () -> {
			servicio.crearValoracion(valoracion);
		});
	}

	@Test
	void permiteUnaPuntuacionValida() {

		ValoracionRepository repositorio = mock(ValoracionRepository.class);
		ActividadService actividadService = mock(ActividadService.class);
		ValoracionService servicio = new ValoracionService(repositorio, actividadService);

		Valoracion valoracion = new Valoracion();
		valoracion.setObraId(1L);
		valoracion.setPuntuacion(4);

		when(repositorio.save(valoracion)).thenReturn(valoracion);

		Valoracion resultado = servicio.crearValoracion(valoracion);

		assertEquals(4, resultado.getPuntuacion());
	}

	@Test
	void registraActividadAlCrearValoracion() {

		ValoracionRepository repositorio = mock(ValoracionRepository.class);
		ActividadService actividadService = mock(ActividadService.class);

		ValoracionService servicio = new ValoracionService(repositorio, actividadService);

		Valoracion valoracion = new Valoracion();
		valoracion.setUsuarioId(1L);
		valoracion.setObraId(10L);
		valoracion.setPuntuacion(4);

		when(repositorio.save(any(Valoracion.class))).thenReturn(valoracion);

		servicio.crearValoracion(valoracion);

		verify(actividadService).registrarActividad(1L, 10L, "VALORACION", "Ha valorado una obra con 4 estrellas");
	}

	@Test
	void editarValoracionRegistraNuevaActividad() {

		ValoracionRepository repositorio = mock(ValoracionRepository.class);

		ActividadService actividadService = mock(ActividadService.class);

		ValoracionService servicio = new ValoracionService(repositorio, actividadService);

		Valoracion valoracion = new Valoracion();

		valoracion.setId(1L);
		valoracion.setUsuarioId(1L);
		valoracion.setObraId(10L);
		valoracion.setPuntuacion(4);

		when(repositorio.findById(1L)).thenReturn(Optional.of(valoracion));

		when(repositorio.save(any(Valoracion.class))).thenReturn(valoracion);

		Valoracion resultado = servicio.editarValoracion(1L, 3);

		assertEquals(3, resultado.getPuntuacion());

		verify(actividadService).registrarActividad(1L, 10L, "VALORACION",
				"Ha actualizado su valoración a 3 estrellas");
	}

}