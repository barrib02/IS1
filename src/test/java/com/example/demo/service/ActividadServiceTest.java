
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.Actividad;
import com.example.demo.repository.ActividadRepository;
import com.example.demo.service.ActividadService;
import com.example.demo.entity.Valoracion;
import com.example.demo.entity.Resena;
import com.example.demo.repository.ValoracionRepository;
import com.example.demo.repository.ResenaRepository;
import com.example.demo.service.ValoracionService;
import com.example.demo.service.ResenaService;
import com.example.demo.repository.ObraRepository;
import com.example.demo.dto.ActividadResponse;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;

class ActividadServiceTest {

	@Test
	void registraActividadCorrectamente() {

		// Creamos el repositorio falso.
		ActividadRepository repo = mock(ActividadRepository.class);

		// Creamos el servicio.
		ActividadService service = new ActividadService(repo, mock(ObraRepository.class));

		// Simulamos una actividad.
		Actividad actividad = new Actividad(1L, 10L, "VALORACION", "Ha valorado una obra");

		when(repo.save(any(Actividad.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		// Ejecutamos el método.
		Actividad resultado = service.registrarActividad(1L, 10L, "VALORACION", "Ha valorado una obra");

		// Comprobamos el resultado.
		assertNotNull(resultado);
		assertNotNull(resultado.getFecha());
		assertEquals(1L, resultado.getUsuarioId());
		assertEquals(10L, resultado.getObraId());
		assertEquals("VALORACION", resultado.getTipo());
		assertEquals("Ha valorado una obra", resultado.getDescripcion());

		verify(repo).save(any(Actividad.class));
	}

	@Test
	void obtieneActividadesDeUnUsuario() {

		// Creamos el repositorio falso.
		ActividadRepository repo = mock(ActividadRepository.class);

		// Creamos el servicio.
		ActividadService service = new ActividadService(repo, mock(ObraRepository.class));

		// Creamos actividades de prueba.
		Actividad actividad1 = new Actividad(1L, 10L, "RESENA", "Ha publicado una reseña");

		Actividad actividad2 = new Actividad(1L, 20L, "VALORACION", "Ha valorado una obra");

		when(repo.findByUsuarioIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of(actividad2, actividad1));

		// Ejecutamos la consulta.
		List<Actividad> resultado = service.obtenerActividadReciente(1L);

		// Comprobamos los resultados.
		assertEquals(2, resultado.size());
		assertEquals("VALORACION", resultado.get(0).getTipo());
		assertEquals("RESENA", resultado.get(1).getTipo());

		verify(repo).findByUsuarioIdOrderByFechaDescIdDesc(1L);
	}

	@Test
	void devuelveListaVaciaSiNoHayActividades() {

		ActividadRepository repo = mock(ActividadRepository.class);
		ActividadService service = new ActividadService(repo, mock(ObraRepository.class));

		when(repo.findByUsuarioIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of());

		List<Actividad> resultado = service.obtenerActividadReciente(1L);

		assertNotNull(resultado);
		assertTrue(resultado.isEmpty());
	}

	@Test
	void registraActividadAlModificarValoracion() {

		ValoracionRepository repo = mock(ValoracionRepository.class);
		ActividadService actividad = mock(ActividadService.class);

		ValoracionService service = new ValoracionService(repo, actividad);

		Valoracion valoracion = new Valoracion();

		valoracion.setId(1L);
		valoracion.setUsuarioId(2L);
		valoracion.setObraId(10L);
		valoracion.setPuntuacion(3);

		when(repo.findById(1L)).thenReturn(java.util.Optional.of(valoracion));

		when(repo.save(any(Valoracion.class))).thenAnswer(i -> i.getArgument(0));

		Valoracion resultado = service.editarValoracion(1L, 5);

		assertEquals(5, resultado.getPuntuacion());

		verify(actividad).registrarActividad(2L, 10L, "VALORACION", "Ha actualizado su valoración a 5 estrellas");
	}

	@Test
	void noRegistraActividadSiValoracionNoCambia() {

		ValoracionRepository repo = mock(ValoracionRepository.class);
		ActividadService actividad = mock(ActividadService.class);

		ValoracionService service = new ValoracionService(repo, actividad);

		Valoracion valoracion = new Valoracion();
		valoracion.setPuntuacion(4);

		when(repo.findById(1L)).thenReturn(java.util.Optional.of(valoracion));

		service.editarValoracion(1L, 4);

		verify(repo, never()).save(any(Valoracion.class));

		verifyNoInteractions(actividad);
	}

	@Test
	void registraActividadAlModificarResena() {

		ResenaRepository repo = mock(ResenaRepository.class);
		ActividadService actividad = mock(ActividadService.class);

		ResenaService service = new ResenaService(repo, actividad);

		Resena resena = new Resena();

		resena.setId(1L);
		resena.setUsuarioId(2L);
		resena.setObraId(10L);
		resena.setTexto("Reseña inicial");

		when(repo.findById(1L)).thenReturn(java.util.Optional.of(resena));

		when(repo.save(any(Resena.class))).thenAnswer(i -> i.getArgument(0));

		Resena resultado = service.editarResena(1L, "Reseña actualizada");

		assertEquals("Reseña actualizada", resultado.getTexto());

		verify(actividad).registrarActividad(2L, 10L, "RESENA", "Ha actualizado su reseña");
	}

	@Test
	void noRegistraActividadSiResenaNoCambia() {

		ResenaRepository repo = mock(ResenaRepository.class);
		ActividadService actividad = mock(ActividadService.class);

		ResenaService service = new ResenaService(repo, actividad);

		Resena resena = new Resena();
		resena.setTexto("Me ha gustado mucho");

		when(repo.findById(1L)).thenReturn(java.util.Optional.of(resena));

		service.editarResena(1L, "Me ha gustado mucho");

		verify(repo, never()).save(any(Resena.class));

		verifyNoInteractions(actividad);
	}

	@Test
	void historialIncluyeInformacionDeLaObra() {

		ActividadRepository repo = mock(ActividadRepository.class);
		ObraRepository obras = mock(ObraRepository.class);

		ActividadService service = new ActividadService(repo, obras);

		Actividad actividad = new Actividad(1L, 10L, "VALORACION", "Ha valorado una obra");

		Obra obra = mock(Obra.class);

		when(obra.getId()).thenReturn(10L);
		when(obra.getTitulo()).thenReturn("Oppenheimer");
		when(obra.getTipo()).thenReturn(TipoObra.PELICULA);

		when(repo.findByUsuarioIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of(actividad));

		when(obras.findAllById(List.of(10L))).thenReturn(List.of(obra));

		List<ActividadResponse> resultado = service.obtenerHistorial(1L);

		assertEquals(1, resultado.size());
		assertEquals("Oppenheimer", resultado.get(0).tituloObra());
		assertEquals("PELICULA", resultado.get(0).tipoObra());
		assertEquals("VALORACION", resultado.get(0).tipo());
		assertEquals(10L, resultado.get(0).obraId());
		assertEquals(actividad.getFecha(), resultado.get(0).fecha());
	}

	@Test
	void historialConservaOrdenCronologico() {

		ActividadRepository repo = mock(ActividadRepository.class);
		ObraRepository obras = mock(ObraRepository.class);

		ActividadService service = new ActividadService(repo, obras);

		Actividad antigua = new Actividad(1L, 10L, "RESENA", "Ha publicado una reseña");

		Actividad reciente = new Actividad(1L, 20L, "VALORACION", "Ha valorado una obra");

		when(repo.findByUsuarioIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of(reciente, antigua));

		when(obras.findAllById(any())).thenReturn(List.of());

		List<ActividadResponse> resultado = service.obtenerHistorial(1L);

		assertEquals(2, resultado.size());
		assertEquals("VALORACION", resultado.get(0).tipo());
		assertEquals("RESENA", resultado.get(1).tipo());

		verify(repo).findByUsuarioIdOrderByFechaDescIdDesc(1L);
	}

	@Test
	void historialConservaActividadSiNoExisteObra() {

		ActividadRepository repo = mock(ActividadRepository.class);
		ObraRepository obras = mock(ObraRepository.class);

		ActividadService service = new ActividadService(repo, obras);

		Actividad actividad = new Actividad(1L, 10L, "RESENA", "Ha publicado una reseña");

		when(repo.findByUsuarioIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of(actividad));

		when(obras.findAllById(any())).thenReturn(List.of());

		List<ActividadResponse> resultado = service.obtenerHistorial(1L);

		assertEquals(1, resultado.size());
		assertNull(resultado.get(0).tituloObra());
		assertNull(resultado.get(0).tipoObra());
		assertEquals("RESENA", resultado.get(0).tipo());
		assertEquals(10L, resultado.get(0).obraId());
	}

	@Test
	void historialLimitaActividadesADiez() {

		ActividadRepository repo = mock(ActividadRepository.class);
		ObraRepository obras = mock(ObraRepository.class);

		ActividadService service = new ActividadService(repo, obras);

		List<Actividad> actividades = new java.util.ArrayList<>();

		for (int i = 0; i < 15; i++) {
			actividades.add(new Actividad(1L, 10L, "ESTADO", "Cambio de estado " + i));
		}

		when(repo.findByUsuarioIdOrderByFechaDescIdDesc(1L)).thenReturn(actividades);

		when(obras.findAllById(any())).thenReturn(List.of());

		List<ActividadResponse> resultado = service.obtenerHistorial(1L);

		assertEquals(10, resultado.size());
		assertEquals("Cambio de estado 0", resultado.get(0).descripcion());
		assertEquals("Cambio de estado 9", resultado.get(9).descripcion());
	}

	@Test
	void historialNoDuplicaConsultaDeObras() {

		ActividadRepository repo = mock(ActividadRepository.class);
		ObraRepository obras = mock(ObraRepository.class);

		ActividadService service = new ActividadService(repo, obras);

		Actividad primera = new Actividad(1L, 10L, "RESENA", "Ha publicado una reseña");

		Actividad segunda = new Actividad(1L, 10L, "VALORACION", "Ha valorado una obra");

		when(repo.findByUsuarioIdOrderByFechaDescIdDesc(1L)).thenReturn(List.of(segunda, primera));

		when(obras.findAllById(List.of(10L))).thenReturn(List.of());

		List<ActividadResponse> resultado = service.obtenerHistorial(1L);

		assertEquals(2, resultado.size());

		verify(obras, times(1)).findAllById(List.of(10L));
		verifyNoMoreInteractions(obras);
	}
}
