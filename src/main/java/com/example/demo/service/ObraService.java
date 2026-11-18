package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;

@Service
public class ObraService {

	// Máximo de obras que devuelve una búsqueda (evita cargar todo el catálogo).
	private static final int MAX_RESULTADOS = 50;

	private final ObraRepository obraRepository;

	public ObraService(ObraRepository obraRepository) {
		this.obraRepository = obraRepository;
	}

	public List<Obra> getObras() {
		return obraRepository.findAll();
	}

	public List<Obra> buscarPorTitulo(String texto) {
		return obraRepository.findByTituloContainingIgnoreCase(texto);
	}

	// Busca por título y/o tipo. Ahora devuelve como máximo 50 obras ordenadas por título.
	public List<Obra> buscar(String titulo, TipoObra tipo) {

		Pageable limite = PageRequest.of(0, MAX_RESULTADOS, Sort.by("titulo"));

		// Título + tipo
		if (titulo != null && !titulo.isBlank() && tipo != null) {
			return obraRepository.findByTituloContainingIgnoreCaseAndTipo(titulo, tipo, limite);
		}

		// Solo título
		if (titulo != null && !titulo.isBlank()) {
			return obraRepository.findByTituloContainingIgnoreCase(titulo, limite);
		}

		// Solo tipo
		if (tipo != null) {
			return obraRepository.findByTipo(tipo, limite);
		}

		// Sin criterios
		return obraRepository.findAll(limite).getContent();
	}

	// Obras más recientes (pantalla de búsqueda antes de escribir nada).
	public List<Obra> explorar() {
		return obraRepository.findTop12ByOrderByIdDesc();
	}

	public List<Obra> obtenerSugerencias(String texto) {
		return obraRepository.findTop5ByTituloStartingWithIgnoreCase(texto);
	}

	// Obra con sus géneros cargados en una sola consulta.
	public Obra obtenerPorId(Long id) {
		return obraRepository.findConGenerosById(id).orElse(null);
	}

}
