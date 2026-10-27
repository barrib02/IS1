package com.example.demo.service;

import java.util.List;
import com.example.demo.entity.TipoObra;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.Obra;
import com.example.demo.repository.ObraRepository;

@Service
public class ObraService {

	@Autowired
	private ObraRepository obraRepository;

	public List<Obra> getObras() {
		return obraRepository.findAll();
	}

	public List<Obra> buscarPorTitulo(String texto) {
		return obraRepository.findByTituloContainingIgnoreCase(texto);
	}

	public List<Obra> buscar(String titulo, TipoObra tipo) {
		
		//Si el usuario ha escrito un titulo y tambien ha seleccionado un tipo: 
		if (titulo != null && !titulo.isBlank() && tipo != null) { 

			return obraRepository.findByTituloContainingIgnoreCaseAndTipo(titulo, tipo);
		}

		//Si el usuario solo escribe texto
		if (titulo != null && !titulo.isBlank()) {

			return obraRepository.findByTituloContainingIgnoreCase(titulo);
		}
		
		//Si el usuario solo elige un tipo 
		if (tipo != null) {

			return obraRepository.findByTipo(tipo);
		}

		return obraRepository.findAll();
	}
	
	public Obra obtenerPorId(Long id) {
	    return obraRepository.findById(id).orElse(null);
	}

}