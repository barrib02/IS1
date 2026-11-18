package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.EstanteResponse;
import com.example.demo.entity.TipoObra;
import com.example.demo.service.EstanteService;

@RestController
public class EstanteController {

	private final EstanteService estanteService;

	public EstanteController(EstanteService estanteService) {
		this.estanteService = estanteService;
	}

	// GET /explorar/estantes            -> Discover general
	// GET /explorar/estantes?tipo=LIBRO -> Discover solo de libros
	@GetMapping("/explorar/estantes")
	public List<EstanteResponse> estantes(@RequestParam(required = false) TipoObra tipo) {
		return estanteService.obtenerEstantes(tipo);
	}
}