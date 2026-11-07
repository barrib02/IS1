package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Creador;
import com.example.demo.repository.CreadorRepository;

@Service
public class CreadorService {

	private final CreadorRepository creadorRepository;

	public CreadorService(CreadorRepository creadorRepository) {
		this.creadorRepository = creadorRepository;
	}

	public Creador obtenerCreador(Long id) { 
		return creadorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Creador no encontrado"));
	}
}
