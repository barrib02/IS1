package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.Obra;
import com.example.demo.service.ObraService;

@RestController
public class ObraController {

	private final ObraService obraService;

	public ObraController(ObraService obraService) {
		this.obraService = obraService;
	}

	
	@GetMapping("/search")
	public List<Obra> buscar(
	        @RequestParam String titulo) {

	    return obraService.buscarPorTitulo(titulo);
	}

	@GetMapping("/work/{id}")
	public Obra obtenerObra(@PathVariable Long id) {
		return obraService.getObras().stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null);
	}

	@PutMapping("/work/{id}/estado")
	public Obra cambiarEstado(
	        @PathVariable Long id,
	        @RequestParam EstadoObra estado) {

	    Obra obra = obraService.getObras().stream()
	            .filter(o -> o.getId().equals(id))
	            .findFirst()
	            .orElse(null);

	    if (obra != null) {
	        //obra.setEstado(estado);
	    }

	    return obra;
	}

}
