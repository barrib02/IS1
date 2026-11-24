
package com.example.demo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ObraExternaService {

	private final ObraRepository obraRepository;

	private final ObraExternaPersistenciaService persistenciaService;
	private final TransactionTemplate transactionTemplate;

// Inyectamos el repositorio, el servicio de persistencia
// y el gestor de transacciones.
	public ObraExternaService(ObraRepository obraRepository, ObraExternaPersistenciaService persistenciaService,
			org.springframework.transaction.PlatformTransactionManager transactionManager) {

		this.obraRepository = obraRepository;
		this.persistenciaService = persistenciaService;
		this.transactionTemplate = new TransactionTemplate(transactionManager);

		// La consulta de recuperación debe ejecutarse en una
		// transacción nueva si falla una inserción anterior.
		this.transactionTemplate
				.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	// Busca una obra externa que ya esté registrada en Neon.
	// No modifica la base de datos.
	@Transactional(readOnly = true)
	public Optional<Obra> buscarReferencia(String fuenteApi, String idExterno, TipoObra tipo) {

		return obraRepository.findByFuenteApiAndIdExternoAndTipo(fuenteApi, idExterno, tipo);
	}

	// Busca o crea una referencia externa.
	// Si otro usuario la crea simultáneamente, intenta
	// recuperar la referencia que ya existe en Neon.
	public Obra obtenerOCrearReferencia(String fuenteApi, String idExterno, TipoObra tipo, String titulo) {

		// Validamos los datos recibidos.
		if (fuenteApi == null || fuenteApi.isBlank() || idExterno == null || idExterno.isBlank() || tipo == null
				|| titulo == null || titulo.isBlank()) {

			throw new IllegalArgumentException("Faltan datos para identificar la obra externa");
		}

		boolean fuenteValida = switch (fuenteApi) {
		case "TMDB" -> tipo == TipoObra.PELICULA || tipo == TipoObra.SERIE;
		case "GOOGLE_BOOKS" -> tipo == TipoObra.LIBRO;
		case "RAWG" -> tipo == TipoObra.VIDEOJUEGO;
		default -> false;
		};

		if (!fuenteValida) {
			throw new IllegalArgumentException("La API de origen no corresponde al tipo de obra");
		}

		// Primero buscamos si la referencia ya existe.
		Optional<Obra> existente = obraRepository.findByFuenteApiAndIdExternoAndTipo(fuenteApi, idExterno, tipo);

		if (existente.isPresent()) {
			return existente.get();
		}

		try {
			// Intentamos crear la referencia en una
			// transacción independiente.
			return persistenciaService.guardarReferencia(fuenteApi, idExterno, tipo, titulo);

		} catch (DataIntegrityViolationException excepcion) {

			// Otro usuario podría haber creado la referencia.
			// Consultamos de nuevo en una transacción independiente.
			Obra recuperada = transactionTemplate.execute(estado -> obraRepository
					.findByFuenteApiAndIdExternoAndTipo(fuenteApi, idExterno, tipo).orElse(null));

			if (recuperada != null) {
				return recuperada;
			}

			// Si no existe, el error tenía otra causa.
			// No ocultamos los problemas de base de datos.
			throw excepcion;
		}
	}

}
