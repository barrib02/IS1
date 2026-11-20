package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.EstadoUsuarioObra;
import com.example.demo.repository.EstadoUsuarioObraRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadoUsuarioObraService {

	@Autowired
	private final EstadoUsuarioObraRepository repository;
	private final ActividadService actividadService;

	public EstadoUsuarioObraService(EstadoUsuarioObraRepository repository, ActividadService actividadService) {

		this.repository = repository;
		this.actividadService = actividadService;
	}

	@Transactional
	public boolean cambiarEstado(Long usuarioId, Long obraId, EstadoObra nuevoEstado) {
		return cambiarEstado(usuarioId, obraId, nuevoEstado, null);
	}

	// "fecha" solo se usa al marcar COMPLETADA. Si es null, se usa el momento actual.
	@Transactional
	public boolean cambiarEstado(Long usuarioId, Long obraId, EstadoObra nuevoEstado, LocalDate fecha) {

		if (fecha != null && fecha.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("La fecha no puede ser futura");
		}

		Optional<EstadoUsuarioObra> existente = repository.findByUsuarioIdAndObraId(usuarioId, obraId);

		EstadoUsuarioObra registro;
		boolean cambiaEstado;

		if (existente.isPresent()) {
			registro = existente.get();
			cambiaEstado = registro.getEstado() != nuevoEstado;

			// Mismo estado y sin fecha nueva: no hay nada que hacer.
			boolean soloCambiaFecha = nuevoEstado == EstadoObra.COMPLETADA && fecha != null;
			if (!cambiaEstado && !soloCambiaFecha) {
				return true;
			}
			registro.setEstado(nuevoEstado);
		} else {
			registro = new EstadoUsuarioObra(usuarioId, obraId, nuevoEstado);
			cambiaEstado = true;
		}

		LocalDateTime ahora = LocalDateTime.now();
		registro.setFechaActualizacion(ahora);

		if (nuevoEstado == EstadoObra.COMPLETADA) {
			if (fecha != null) {
				// A mediodía, para que ninguna conversión de zona horaria cambie el día.
				registro.setFechaCompletada(fecha.equals(LocalDate.now()) ? ahora : fecha.atTime(12, 0));
			} else if (cambiaEstado || registro.getFechaCompletada() == null) {
				registro.setFechaCompletada(ahora);
			}
		} else {
			registro.setFechaCompletada(null);
		}

		repository.save(registro);

		// Si solo se corrigió la fecha, no se registra otra actividad.
		if (cambiaEstado) {
			actividadService.registrarActividad(usuarioId, obraId, "ESTADO",
					"Ha cambiado el estado de una obra a " + nuevoEstado);
		}

		return true;
	}

	public EstadoObra obtenerEstado(Long usuarioId, Long obraId) {

		Optional<EstadoUsuarioObra> estado = repository.findByUsuarioIdAndObraId(usuarioId, obraId);

		return estado.map(EstadoUsuarioObra::getEstado).orElse(null);
	}
}