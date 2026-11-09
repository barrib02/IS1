package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.Resena;
import com.example.demo.repository.ResenaRepository;
import com.example.demo.service.ResenaService;
import com.example.demo.service.ActividadService;
import java.util.Optional;

class ResenaServiceTest {

	@Test
	void noPermiteResenaVacia() {

		/* Creamos un repositorio falso para no usar la base de datos real. */
		ResenaRepository repo = mock(ResenaRepository.class);

		// Creamos el servicio que queremos probar.
		ActividadService actividadService = mock(ActividadService.class);
		ResenaService service = new ResenaService(repo, actividadService);

		// Creamos una resena vacia.
		Resena resena = new Resena();
		resena.setTexto("");

		// Comprobamos que el servicio lanza una excepcion
		assertThrows(IllegalArgumentException.class, () -> service.crearResena(resena));
	}

	@Test
	void permiteResenaValida() {

		// Creamos un repositorio falso
		ResenaRepository repo = mock(ResenaRepository.class);

		// Creamos el servicio que vamos a probar
		ActividadService actividadService = mock(ActividadService.class);
		ResenaService service = new ResenaService(repo, actividadService);

		// Creamos una resena valida.
		Resena resena = new Resena();
		resena.setTexto("Muy buena obra");

		// Simulamos que al guardar en la base de datos
		// se devuelve exactamente la misma reseña.
		when(repo.save(any())).thenReturn(resena);

		// Ejecutamos el método que queremos probar.
		Resena resultado = service.crearResena(resena);

		// Verificamos que el texto guardado es correcto.
		assertEquals("Muy buena obra", resultado.getTexto());
	}

	@Test
	void noPermiteMasDe200Palabras() {
		// Creo el repositorio falso
		ResenaRepository repo = mock(ResenaRepository.class);

		// Creo el servicio que voy a probar
		ActividadService actividadService = mock(ActividadService.class);
		ResenaService service = new ResenaService(repo, actividadService);

		// Creo un resena con mas de 200 palabras
		Resena resena = new Resena();

		String texto = "palabra ".repeat(201);

		resena.setTexto(texto);

		// Compruebo que lanza excepcion
		assertThrows(IllegalArgumentException.class, () -> service.crearResena(resena));
	}

	@Test
	void registraActividadAlCrearResena() {

		ResenaRepository repo = mock(ResenaRepository.class);
		ActividadService actividadService = mock(ActividadService.class);

		ResenaService service = new ResenaService(repo, actividadService);

		Resena resena = new Resena();
		resena.setUsuarioId(1L);
		resena.setObraId(10L);
		resena.setTexto("Muy buena obra");

		when(repo.save(any(Resena.class))).thenReturn(resena);

		service.crearResena(resena);

		verify(actividadService).registrarActividad(1L, 10L, "RESENA", "Ha publicado una reseña");
	}

	@Test
	void editarResenaNoRegistraNuevaActividad() {

		ResenaRepository repo = mock(ResenaRepository.class);
		ActividadService actividadService = mock(ActividadService.class);

		ResenaService service = new ResenaService(repo, actividadService);

		Resena resena = new Resena();
		resena.setId(1L);
		resena.setUsuarioId(1L);
		resena.setObraId(10L);
		resena.setTexto("Reseña original");

		when(repo.findById(1L)).thenReturn(Optional.of(resena));
		when(repo.save(any(Resena.class))).thenReturn(resena);

		service.editarResena(1L, "Reseña modificada");

		verify(actividadService, never()).registrarActividad(anyLong(), anyLong(), anyString(), anyString());
	}

}