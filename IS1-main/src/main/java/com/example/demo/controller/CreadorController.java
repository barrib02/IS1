package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CreadorResponse;
import com.example.demo.entity.Creador;
import com.example.demo.service.CreadorService;

@RestController
public class CreadorController {

	private final CreadorService creadorService;

	public CreadorController(CreadorService creadorService) {

		this.creadorService = creadorService;
	}

	@GetMapping("/creador/{id}")
	public ResponseEntity<CreadorResponse> obtenerCreador(@PathVariable Long id) {
		return ResponseEntity.ok(creadorService.obtenerFicha(id));
	}
}
