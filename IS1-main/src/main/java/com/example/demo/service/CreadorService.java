package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Creador;
import com.example.demo.repository.CreadorRepository;
import com.example.demo.repository.ObraRepository;
import com.example.demo.dto.CreadorResponse;
import com.example.demo.dto.SearchResultDTO;

@Service
public class CreadorService {

	private final CreadorRepository creadorRepository;
	private final ObraRepository obraRepository;

	public CreadorService(CreadorRepository creadorRepository, ObraRepository obraRepository) {

		this.creadorRepository = creadorRepository;
		this.obraRepository = obraRepository;
	}

	public Creador obtenerCreador(Long id) {
		return creadorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Creador no encontrado"));
	}

	public CreadorResponse obtenerFicha(Long id) {

		// Buscar creador
		Creador creador = creadorRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Creador no encontrado"));

		// Buscar sus obras
		List<SearchResultDTO> obras = obraRepository.findByCreadorIdOrderByAnioAsc(id).stream()
				.map(o -> new SearchResultDTO(o.getId(), o.getTitulo(), o.getTipo().name())).toList();

		List<SearchResultDTO> destacadas = obtenerDestacadas(id);

		return new CreadorResponse(creador.getId(), creador.getNombre(), creador.getFoto(), creador.getBiografia(),
				obras, destacadas);
	}

	// Devuelve las 3 primeras obras del creador
	public List<SearchResultDTO> obtenerDestacadas(Long creadorId) {

		return obraRepository.findByCreadorIdOrderByAnioAsc(creadorId).stream().limit(3)
				.map(o -> new SearchResultDTO(o.getId(), o.getTitulo(), o.getTipo().name())).toList();
	}
}
