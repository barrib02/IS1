package com.example.demo.controller;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.demo.entity.EstadoObra;
import com.example.demo.service.EstadoUsuarioObraService;
import com.example.demo.service.PerfilService;

@RestController
public class EstadoUsuarioObraController {

	private final EstadoUsuarioObraService service;
	private final PerfilService perfilService;

	public EstadoUsuarioObraController(EstadoUsuarioObraService service, PerfilService perfilService) {

		this.service = service;
		this.perfilService = perfilService;
	}

	// Comprueba que el usuario solo accede a sus propios estados.
	private void comprobarPropietario(Long usuarioId, Authentication auth) {

		if (auth == null || !auth.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
		}

		Long usuarioAutenticadoId = perfilService.obtenerUsuarioPorEmail(auth.getName()).getId();

		if (!Objects.equals(usuarioId, usuarioAutenticadoId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a los estados de otro usuario");
		}
	}

	@PutMapping("/users/{usuarioId}/works/{obraId}/estado")
	public String cambiarEstado(@PathVariable Long usuarioId, @PathVariable Long obraId, @RequestParam String estado,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
			Authentication auth) {

		comprobarPropietario(usuarioId, auth);

		service.cambiarEstado(usuarioId, obraId, EstadoObra.valueOf(estado), fecha);

		return "Estado actualizado";
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}

	@GetMapping("/users/{usuarioId}/works/{obraId}/estado")
	public EstadoObra obtenerEstado(@PathVariable Long usuarioId, @PathVariable Long obraId, Authentication auth) {

		comprobarPropietario(usuarioId, auth);

		return service.obtenerEstado(usuarioId, obraId);
	}
}