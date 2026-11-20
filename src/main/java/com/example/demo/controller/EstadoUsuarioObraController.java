package com.example.demo.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.example.demo.entity.EstadoObra;
import com.example.demo.service.EstadoUsuarioObraService;

@RestController
public class EstadoUsuarioObraController {

    private final EstadoUsuarioObraService service;

    public EstadoUsuarioObraController(
            EstadoUsuarioObraService service) {

        this.service = service;
    }

	@PutMapping("/users/{usuarioId}/works/{obraId}/estado")
	public String cambiarEstado(
			@PathVariable Long usuarioId,
			@PathVariable Long obraId,
			@RequestParam String estado,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

		service.cambiarEstado(usuarioId, obraId, EstadoObra.valueOf(estado), fecha);

		return "Estado actualizado";
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}

    @GetMapping("/users/{usuarioId}/works/{obraId}/estado")
    public EstadoObra obtenerEstado(
            @PathVariable Long usuarioId,
            @PathVariable Long obraId) {

        return service.obtenerEstado(usuarioId, obraId);
    }
}

