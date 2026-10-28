package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.BiografiaRequest;
import com.example.demo.dto.PerfilResponse;
import com.example.demo.service.PerfilService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

	private final PerfilService perfilService;

	public PerfilController(PerfilService perfilService) {
		this.perfilService = perfilService;
	}

	// Mi perfil. Spring rellena "auth" con el usuario de la sesión;
	// auth.getName() es el email guardado en AuthController al hacer login.
	@GetMapping
	public PerfilResponse miPerfil(Authentication auth) {
		return perfilService.obtenerMiPerfil(auth.getName());
	}

	// Editar mi biografía.
	@PutMapping("/biografia")
	public PerfilResponse editarBiografia(@Valid @RequestBody BiografiaRequest request, Authentication auth) {

		return perfilService.actualizarBiografia(auth.getName(), request.getBiografia());
	}

	// Perfil de otro usuario (datos públicos).
	@GetMapping("/{id}")
	public PerfilResponse perfilDeUsuario(@PathVariable Long id) {
		return perfilService.obtenerPerfilPorId(id);
	}

	// Errores de negocio (usuario no encontrado, biografía demasiado larga).
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}

	// Errores de validación de @Valid (por ejemplo, más de 500 caracteres).
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarValidacion(MethodArgumentNotValidException error) {
		return error.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
	}
}