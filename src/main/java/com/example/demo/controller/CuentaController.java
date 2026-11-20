package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.EliminarCuentaRequest;
import com.example.demo.service.CuentaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/cuenta")
public class CuentaController {

	private final CuentaService cuentaService;

	public CuentaController(CuentaService cuentaService) {
		this.cuentaService = cuentaService;
	}

	// POST /api/cuenta/eliminar Body: { "password": "..." }
	@PostMapping("/eliminar")
	public ResponseEntity<String> eliminar(@RequestBody EliminarCuentaRequest request, Authentication auth,
			HttpServletRequest http) {

		cuentaService.eliminar(auth.getName(), request.getPassword());

		// Cerramos la sesión igual que en el logout.
		SecurityContextHolder.clearContext();
		HttpSession sesion = http.getSession(false);
		if (sesion != null) {
			sesion.invalidate();
		}

		return ResponseEntity.ok("Cuenta eliminada");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}
}
