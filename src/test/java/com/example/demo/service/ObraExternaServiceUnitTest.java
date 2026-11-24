
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ObraExternaServiceUnitTest {

	// Simulamos el repositorio para no conectarnos a Neon.
	@Mock
	private ObraRepository obraRepository;

	// Simulamos el servicio que guarda referencias externas.
	@Mock
	private ObraExternaPersistenciaService persistenciaService;

	// Simulamos el gestor de transacciones de Spring.
	@Mock
	private org.springframework.transaction.PlatformTransactionManager transactionManager;

	// Creamos el servicio con las dependencias simuladas.
	@InjectMocks
	private ObraExternaService obraExternaService;

	@Test
	void debeReutilizarUnaObraExistente() {

		// Preparamos una obra que supuestamente ya existe.
		Obra obraExistente = new Obra();
		obraExistente.setId(62L);
		obraExistente.setTitulo("Oppenheimer");
		obraExistente.setFuenteApi("TMDB");
		obraExistente.setIdExterno("872585");
		obraExistente.setTipo(TipoObra.PELICULA);

		// Simulamos que Neon devuelve esa obra.
		when(obraRepository.findByFuenteApiAndIdExternoAndTipo("TMDB", "872585", TipoObra.PELICULA))
				.thenReturn(Optional.of(obraExistente));

		// Ejecutamos el método que queremos probar.
		Obra resultado = obraExternaService.obtenerOCrearReferencia("TMDB", "872585", TipoObra.PELICULA, "Oppenheimer");

		// Comprobamos que se reutiliza el ID existente.
		assertEquals(62L, resultado.getId());

		// Comprobamos que no se guarda una obra nueva.
		verify(obraRepository, never()).save(any(Obra.class));
	}

	@Test
	void debeCrearReferenciaCuandoNoExiste() {

		// Simulamos que la película todavía no existe en Neon.
		when(obraRepository.findByFuenteApiAndIdExternoAndTipo("TMDB", "671", TipoObra.PELICULA))
				.thenReturn(Optional.empty());

		// Preparamos la referencia que devolvería
		// el servicio de persistencia después de guardarla.
		Obra obraGuardada = new Obra();
		obraGuardada.setId(100L);
		obraGuardada.setFuenteApi("TMDB");
		obraGuardada.setIdExterno("671");
		obraGuardada.setTipo(TipoObra.PELICULA);
		obraGuardada.setTitulo("Harry Potter y la piedra filosofal");
		obraGuardada.setValidada(true);

		// Simulamos el resultado del guardado.
		when(persistenciaService.guardarReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal")).thenReturn(obraGuardada);

		// Ejecutamos el método que queremos probar.
		Obra resultado = obraExternaService.obtenerOCrearReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal");

		// Comprobamos que se devuelve la referencia correcta.
		assertEquals(100L, resultado.getId());
		assertEquals("TMDB", resultado.getFuenteApi());
		assertEquals("671", resultado.getIdExterno());
		assertEquals(TipoObra.PELICULA, resultado.getTipo());
		assertEquals("Harry Potter y la piedra filosofal", resultado.getTitulo());

		// Comprobamos que no contiene metadatos innecesarios.
		assertNull(resultado.getDescripcion());
		assertNull(resultado.getImagenUrl());

		// Comprobamos que la referencia está validada.
		assertTrue(resultado.isValidada());

		// Verificamos que se solicita el guardado una sola vez.
		verify(persistenciaService, times(1)).guardarReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal");
	}

	@Test
	void debeRechazarDatosInvalidos() {

		// Comprobamos que no se acepta una API sin nombre.
		assertThrows(IllegalArgumentException.class,
				() -> obraExternaService.obtenerOCrearReferencia("", "671", TipoObra.PELICULA, "Harry Potter"));

		// Comprobamos que no se acepta una obra sin ID externo.
		assertThrows(IllegalArgumentException.class,
				() -> obraExternaService.obtenerOCrearReferencia("TMDB", "", TipoObra.PELICULA, "Harry Potter"));

		// Comprobamos que no se acepta una obra sin tipo.
		assertThrows(IllegalArgumentException.class,
				() -> obraExternaService.obtenerOCrearReferencia("TMDB", "671", null, "Harry Potter"));

		// Comprobamos que no se acepta una obra sin título.
		assertThrows(IllegalArgumentException.class,
				() -> obraExternaService.obtenerOCrearReferencia("TMDB", "671", TipoObra.PELICULA, ""));

		// Comprobamos que una película no puede proceder
		// de la API de Google Books.
		assertThrows(IllegalArgumentException.class, () -> obraExternaService.obtenerOCrearReferencia("GOOGLE_BOOKS",
				"671", TipoObra.PELICULA, "Harry Potter"));

		// Comprobamos que una API desconocida se rechaza.
		assertThrows(IllegalArgumentException.class, () -> obraExternaService.obtenerOCrearReferencia("API_DESCONOCIDA",
				"671", TipoObra.PELICULA, "Harry Potter"));

		// Si los datos son incorrectos, no debe acceder
		// al repositorio ni guardar ninguna referencia.
		verifyNoInteractions(obraRepository);
	}

	@Test
	void debePropagarErrorSiLaBaseDeDatosRechazaDuplicado() {

		// Simulamos que la obra no existe inicialmente.
		when(obraRepository.findByFuenteApiAndIdExternoAndTipo("TMDB", "671", TipoObra.PELICULA))
				.thenReturn(Optional.empty());

		// Simulamos un error de integridad durante el guardado.
		when(persistenciaService.guardarReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal"))
				.thenThrow(new org.springframework.dao.DataIntegrityViolationException(
						"Error de integridad en la base de datos"));

		// Simulamos que la consulta de recuperación
		// tampoco encuentra una referencia existente.
		when(transactionManager.getTransaction(any()))
				.thenReturn(new org.springframework.transaction.support.SimpleTransactionStatus());

		// Si no existe una referencia que recuperar,
		// el servicio debe propagar el error original.
		assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> obraExternaService
				.obtenerOCrearReferencia("TMDB", "671", TipoObra.PELICULA, "Harry Potter y la piedra filosofal"));

		// Comprobamos que solo se intentó guardar una vez.
		verify(persistenciaService, times(1)).guardarReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal");
	}

	@Test
	void debeRecuperarReferenciaCreadaPorOtroUsuario() {

		// Preparamos la obra que otro usuario ya ha guardado.
		Obra obraExistente = new Obra();
		obraExistente.setId(100L);
		obraExistente.setTitulo("Harry Potter y la piedra filosofal");
		obraExistente.setFuenteApi("TMDB");
		obraExistente.setIdExterno("671");
		obraExistente.setTipo(TipoObra.PELICULA);

		// La primera búsqueda no encuentra la obra.
		// La segunda búsqueda, tras el conflicto, sí la encuentra.
		when(obraRepository.findByFuenteApiAndIdExternoAndTipo("TMDB", "671", TipoObra.PELICULA))
				.thenReturn(Optional.empty(), Optional.of(obraExistente));

		// Simulamos que otro usuario ha guardado la obra primero.
		when(persistenciaService.guardarReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal"))
				.thenThrow(new org.springframework.dao.DataIntegrityViolationException("Referencia duplicada"));

		// Permitimos que TransactionTemplate ejecute
		// la consulta de recuperación.
		when(transactionManager.getTransaction(any()))
				.thenReturn(new org.springframework.transaction.support.SimpleTransactionStatus());

		// Ejecutamos la operación.
		Obra resultado = obraExternaService.obtenerOCrearReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal");

		// Debe recuperar el registro existente.
		assertEquals(100L, resultado.getId());

		// La búsqueda debe haberse ejecutado dos veces.
		verify(obraRepository, times(2)).findByFuenteApiAndIdExternoAndTipo("TMDB", "671", TipoObra.PELICULA);

		// Solo debe haberse intentado crear una referencia.
		verify(persistenciaService, times(1)).guardarReferencia("TMDB", "671", TipoObra.PELICULA,
				"Harry Potter y la piedra filosofal");
	}

}
