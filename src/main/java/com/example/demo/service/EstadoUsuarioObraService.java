package com.example.demo.service;

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

		Optional<EstadoUsuarioObra> existente = repository.findByUsuarioIdAndObraId(usuarioId, obraId);

		EstadoUsuarioObra estadoUsuarioObra;

		if (existente.isPresent()) {

			estadoUsuarioObra = existente.get();

			// Si ya tiene ese estado, no registramos una actividad duplicada.
			if (estadoUsuarioObra.getEstado() == nuevoEstado) {
				return true;
			}

			estadoUsuarioObra.setEstado(nuevoEstado);

		} else {

			estadoUsuarioObra = new EstadoUsuarioObra(usuarioId, obraId, nuevoEstado);
		}

		repository.save(estadoUsuarioObra);

		actividadService.registrarActividad(usuarioId, obraId, "ESTADO",
				"Ha cambiado el estado de una obra a " + nuevoEstado);

		return true;
	}

	public EstadoObra obtenerEstado(Long usuarioId, Long obraId) {

		Optional<EstadoUsuarioObra> estado = repository.findByUsuarioIdAndObraId(usuarioId, obraId);

		return estado.map(EstadoUsuarioObra::getEstado).orElse(null);
	}
}