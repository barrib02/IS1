package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.example.demo.dto.CompletadaResponse;
import com.example.demo.dto.EstadisticasResponse;
import com.example.demo.dto.MesConteo;
import com.example.demo.entity.PeriodoEstadistica;
import com.example.demo.entity.TipoObra;
import com.example.demo.service.EstadisticasService;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

	private final EstadisticasService estadisticasService;

	public EstadisticasController(EstadisticasService estadisticasService) {
		this.estadisticasService = estadisticasService;
	}

	// GET /api/estadisticas?periodo=TODO|SEMANA|MES|ANIO
	@GetMapping
	public EstadisticasResponse misEstadisticas(@RequestParam(defaultValue = "TODO") PeriodoEstadistica periodo,
			Authentication auth) {
		return estadisticasService.obtener(auth.getName(), periodo);
	}
	
	// GET /api/estadisticas/completadas?tipo=LIBRO&periodo=MES
	@GetMapping("/completadas")
	public List<CompletadaResponse> completadas(@RequestParam(required = false) TipoObra tipo,
			@RequestParam(defaultValue = "TODO") PeriodoEstadistica periodo, Authentication auth) {
		return estadisticasService.completadas(auth.getName(), tipo, periodo);
	}

	// GET /api/estadisticas/calendario?anio=2026&mes=11
	@GetMapping("/calendario")
	public List<CompletadaResponse> calendario(@RequestParam int anio, @RequestParam int mes,
			Authentication auth) {
		return estadisticasService.calendario(auth.getName(), anio, mes);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}
	
	// GET /api/estadisticas/mensual
	@GetMapping("/mensual")
	public List<MesConteo> mensual(Authentication auth) {
		return estadisticasService.ultimosMeses(auth.getName());
	}
}
