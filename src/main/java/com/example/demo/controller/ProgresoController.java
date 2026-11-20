package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ProgresoRequest;
import com.example.demo.dto.ProgresoResponse;
import com.example.demo.service.ProgresoService;

@RestController
@RequestMapping("/api/progreso")
public class ProgresoController {

	private final ProgresoService progresoService;

	public ProgresoController(ProgresoService progresoService) {
		this.progresoService = progresoService;
	}

	// Obras en curso con progreso (para el perfil).
	@GetMapping("/en-curso")
	public List<ProgresoResponse> enCurso(Authentication auth) {
		return progresoService.enCurso(auth.getName());
	}

	@GetMapping("/obra/{obraId}")
	public ProgresoResponse obtener(@PathVariable Long obraId, Authentication auth) {
		return progresoService.obtener(auth.getName(), obraId);
	}

	@PutMapping("/obra/{obraId}")
	public ProgresoResponse guardar(@PathVariable Long obraId, @RequestBody ProgresoRequest request,
			Authentication auth) {
		return progresoService.guardar(auth.getName(), obraId, request);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}
}