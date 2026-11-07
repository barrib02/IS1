package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.EstadoUsuarioObra;
import com.example.demo.repository.EstadoUsuarioObraRepository;
import com.example.demo.service.EstadoUsuarioObraService;

class EstadoUsuarioObraServiceTest {

	@Test
	void obtieneEstadoExistente() {
		/* Se crea un repositorio falso */
		EstadoUsuarioObraRepository repo = mock(EstadoUsuarioObraRepository.class);

		/* Se crea el Service con el repositorio falso */
		EstadoUsuarioObraService service = new EstadoUsuarioObraService(repo);

		/*Simular un estado existente (creamos el objeto que queremos que encuentre el repositorio)*/
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
		EstadoUsuarioObraService service = new EstadoUsuarioObraService(repo);

		/*Cuando alguien llame a findByUsuarioIdAndObraId(1L,1L), devuelve Optional.empty()*/
		when(repo.findByUsuarioIdAndObraId(1L, 1L)).thenReturn(Optional.empty());

		/*Comprueba que el resultado sea null*/
		assertNull(service.obtenerEstado(1L, 1L));
	}
}