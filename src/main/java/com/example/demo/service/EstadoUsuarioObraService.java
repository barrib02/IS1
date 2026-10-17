package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.EstadoUsuarioObra;
import com.example.demo.repository.EstadoUsuarioObraRepository;

@Service
public class EstadoUsuarioObraService {

	@Autowired
	private EstadoUsuarioObraRepository repository;

	public boolean cambiarEstado(Long usuarioId, Long obraId, EstadoObra nuevoEstado) {

		Optional<EstadoUsuarioObra> existente = repository.findByUsuarioIdAndObraId(usuarioId, obraId);

		EstadoUsuarioObra estadoUsuarioObra;

		if (existente.isPresent()) {

			estadoUsuarioObra = existente.get();
			estadoUsuarioObra.setEstado(nuevoEstado);

		} else {

			estadoUsuarioObra = new EstadoUsuarioObra(usuarioId, obraId, nuevoEstado);

		}

		repository.save(estadoUsuarioObra);

		return true;
	}

	public EstadoObra obtenerEstado(Long usuarioId, Long obraId) {

		Optional<EstadoUsuarioObra> estado = repository.findByUsuarioIdAndObraId(usuarioId, obraId);

		return estado.map(EstadoUsuarioObra::getEstado).orElse(null);
	}
}