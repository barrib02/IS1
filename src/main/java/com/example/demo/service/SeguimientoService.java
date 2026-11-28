package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.UsuarioResumenDTO;
import com.example.demo.entity.Seguimiento;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.SeguimientoRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class SeguimientoService {

	private final SeguimientoRepository seguimientoRepository;
	private final UsuarioRepository usuarioRepository;

	public SeguimientoService(SeguimientoRepository seguimientoRepository, UsuarioRepository usuarioRepository) {

		this.seguimientoRepository = seguimientoRepository;

		this.usuarioRepository = usuarioRepository;
	}

	public void seguir(Long seguidorId, Long seguidoId) {

		if (seguidorId.equals(seguidoId)) {
			throw new IllegalArgumentException("No puedes seguirte a ti mismo");
		}

		Usuario seguidor = usuarioRepository.findById(seguidorId).orElseThrow();

		Usuario seguido = usuarioRepository.findById(seguidoId).orElseThrow();

		if (seguimientoRepository.existsBySeguidorAndSeguido(seguidor, seguido)) {

			return;
		}

		Seguimiento seguimiento = new Seguimiento(seguidor, seguido);

		seguimientoRepository.save(seguimiento);
	}

	public void dejarDeSeguir(Long seguidorId, Long seguidoId) {

		Usuario seguidor = usuarioRepository.findById(seguidorId).orElseThrow();

		Usuario seguido = usuarioRepository.findById(seguidoId).orElseThrow();

		seguimientoRepository.findBySeguidorAndSeguido(seguidor, seguido).ifPresent(seguimientoRepository::delete);
	}

	public List<UsuarioResumenDTO> obtenerSeguidores(Long usuarioId) {

		Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();

		return seguimientoRepository.findBySeguido(usuario).stream()
				.map(s -> new UsuarioResumenDTO(s.getSeguidor().getId(), s.getSeguidor().getNombre())).toList();
	}

	public List<UsuarioResumenDTO> obtenerSeguidos(Long usuarioId) {

		Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();

		return seguimientoRepository.findBySeguidor(usuario).stream()
				.map(s -> new UsuarioResumenDTO(s.getSeguido().getId(), s.getSeguido().getNombre())).toList();
	}

}