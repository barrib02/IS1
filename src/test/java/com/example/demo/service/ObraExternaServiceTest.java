
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.service.ObraExternaService;

@SpringBootTest
class ObraExternaServiceTest {

	@Autowired
	private ObraExternaService obraExternaService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void debeEncontrarOppenheimerEnNeon() {

		// Buscamos Oppenheimer mediante su referencia de TMDB.
		Obra obra = obraExternaService.buscarReferencia("TMDB", "872585", TipoObra.PELICULA).orElseThrow();

		// Comprobamos que se recupera el registro existente.
		assertEquals(62L, obra.getId());
		assertEquals("Oppenheimer", obra.getTitulo());
		assertEquals("TMDB", obra.getFuenteApi());
		assertEquals("872585", obra.getIdExterno());
	}

	@Test
	void debeReutilizarReferenciaExistenteEnNeon() {

		// Solicitamos dos veces la misma película de TMDB.
		Obra primera = obraExternaService.obtenerOCrearReferencia("TMDB", "872585", TipoObra.PELICULA, "Oppenheimer");

		Obra segunda = obraExternaService.obtenerOCrearReferencia("TMDB", "872585", TipoObra.PELICULA, "Oppenheimer");

		// Ambas consultas deben devolver la misma obra.
		assertEquals(primera.getId(), segunda.getId());

		// Comprobamos que se reutiliza el registro existente.
		assertEquals(62L, primera.getId());
		assertEquals(62L, segunda.getId());
	}

	@Test
	void debeCrearReferenciaExternaEnNeon() {

		// Utilizamos un ID exclusivo para no coincidir
		// con ninguna obra real de Shelfie.
		String idExterno = "TEST_SHELFIE_" + java.util.UUID.randomUUID();

		try {

			// Solicitamos la creación de una referencia externa.
			Obra obra = obraExternaService.obtenerOCrearReferencia("TMDB", idExterno, TipoObra.PELICULA,
					"Película de prueba Shelfie");

			// Comprobamos que Neon le ha asignado un ID.
			assertNotNull(obra.getId());

			// Comprobamos que podemos recuperar la misma referencia.
			Obra recuperada = obraExternaService.buscarReferencia("TMDB", idExterno, TipoObra.PELICULA).orElseThrow();

			assertEquals(obra.getId(), recuperada.getId());
			assertEquals("Película de prueba Shelfie", recuperada.getTitulo());

		} finally {

			// Eliminamos únicamente el registro temporal
			// identificado por nuestro ID externo exclusivo.
			jdbcTemplate.update("DELETE FROM obras WHERE fuente_api = ? AND id_externo = ? AND tipo = ?", "TMDB",
					idExterno, "PELICULA");
		}
	}

}
