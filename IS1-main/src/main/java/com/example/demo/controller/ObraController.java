package com.example.demo.controller;

import java.util.List;
import com.example.demo.entity.TipoObra;

import org.springframework.http.ResponseEntity;
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
	        @RequestParam(required = false) String titulo,
	        @RequestParam(required = false) TipoObra tipo) {

	    return obraService.buscar(titulo, tipo);
	}

	@GetMapping("/work/{id}")
	public ResponseEntity<Obra> obtenerObra(@PathVariable Long id) {
		// Búsqueda directa y segura por ID en la base de datos
		Obra obra = obraService.obtenerPorId(id);
		
		if (obra == null) {
            return ResponseEntity.notFound().build(); // Devuelve 404 real si no existe
        }
        
        return ResponseEntity.ok(obra); // Devuelve 200 OK con el JSON de la obra
	}

	@GetMapping("/sugerencias")
	public List<Obra> obtenerSugerencias(

	        @RequestParam String texto){

	    return obraService.obtenerSugerencias(texto);
	}
	
	@PutMapping("/work/{id}/estado")
	public ResponseEntity<Obra> cambiarEstado(
	        @PathVariable Long id,
	        @RequestParam EstadoObra estado) {

	    Obra obra = obraService.obtenerPorId(id);

	    if (obra != null) {
	        // obra.setEstado(estado); // Descomenta cuando implementes el campo estado en la entidad
	    } else {
	        return ResponseEntity.notFound().build();
	    }

	    return ResponseEntity.ok(obra);
	}

}