package com.example.demo.service;

import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.entity.EstadoObra;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ObraService {

	private final List<Obra> obras = new ArrayList<>();

	public ObraService() {

		obras.add(new Obra(1L, "El Señor de los Anillos", "J.R.R. Tolkien", "", TipoObra.LIBRO, EstadoObra.PENDIENTE));

		obras.add(new Obra(2L, "Breaking Bad", "Vince Gilligan", "", TipoObra.SERIE, EstadoObra.EN_CURSO));

		obras.add(new Obra(3L, "The Witcher 3", "CD Projekt Red", "", TipoObra.VIDEOJUEGO, EstadoObra.COMPLETADA));
	}

	public List<Obra> getObras() {
		return obras;
	}
	
	public List<Obra> buscarPorTitulo(String texto) {

	    List<Obra> resultados = new ArrayList<>();

	    for (Obra obra : obras) {

	        if (obra.getTitulo().toLowerCase()
	                .contains(texto.toLowerCase())) {

	            resultados.add(obra);
	        }
	    }

	    return resultados;
	}
	
}
