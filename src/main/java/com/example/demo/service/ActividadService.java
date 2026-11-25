
package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.demo.dto.ActividadResponse;
import com.example.demo.entity.Obra;
import com.example.demo.repository.ObraRepository;
import com.example.demo.entity.Actividad;
import com.example.demo.repository.ActividadRepository;

@Service
public class ActividadService {

	private final ActividadRepository actividadRepository;
	private final ObraRepository obraRepository;

	public ActividadService(ActividadRepository actividadRepository, ObraRepository obraRepository) {

		this.actividadRepository = actividadRepository;
		this.obraRepository = obraRepository;
	}

	// Registrar una nueva actividad del usuario.
	public Actividad registrarActividad(Long usuarioId, Long obraId, String tipo, String descripcion) {

		Actividad actividad = new Actividad(usuarioId, obraId, tipo, descripcion);

		return actividadRepository.save(actividad);
	}

	// Consultar las actividades de un usuario,
	// ordenadas de más reciente a más antigua.
	public List<Actividad> obtenerActividadReciente(Long usuarioId) {

		return actividadRepository.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId);
	}

	// Recupera las actividades junto con los datos de sus obras.
	public List<ActividadResponse> obtenerHistorial(Long usuarioId) {

		List<Actividad> actividades = obtenerActividadReciente(usuarioId).stream().limit(10).toList();

		List<Long> idsObras = actividades.stream().map(Actividad::getObraId).filter(Objects::nonNull).distinct()
				.toList();

		Map<Long, Obra> obras = obraRepository.findAllById(idsObras).stream()
				.collect(Collectors.toMap(Obra::getId, obra -> obra));

		return actividades.stream().map(actividad -> {

			Obra obra = actividad.getObraId() == null ? null : obras.get(actividad.getObraId());

			return new ActividadResponse(actividad.getId(), actividad.getUsuarioId(), actividad.getObraId(),
					obra == null ? null : obra.getTitulo(),
					obra == null || obra.getTipo() == null ? null : obra.getTipo().name(), actividad.getTipo(),
					actividad.getDescripcion(), actividad.getFecha());

		}).toList();
	}
}
