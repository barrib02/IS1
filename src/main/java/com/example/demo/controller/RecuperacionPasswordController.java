package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.RecuperacionPasswordRequest;
import com.example.demo.dto.RestablecerPasswordRequest;
import com.example.demo.service.RecuperacionPasswordService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
public class RecuperacionPasswordController {

	private final RecuperacionPasswordService service;

	public RecuperacionPasswordController(RecuperacionPasswordService service) {

		this.service = service;
	}

	// Solicita el correo de recuperación.
	@PostMapping("/recuperar-password")
	public ResponseEntity<String> solicitarRecuperacion(@Valid @RequestBody RecuperacionPasswordRequest request) {

		try {

			service.solicitarRecuperacion(request.getEmail());

		} catch (MailException error) {

			System.err.println("No se ha podido enviar un correo de recuperación.");

		}

		return ResponseEntity.ok("Si existe una cuenta verificada asociada a ese correo, "
				+ "recibirás un enlace para restablecer tu contraseña.");
	}

	// Guarda la nueva contraseña.
	@PostMapping("/restablecer-password")
	public ResponseEntity<String> restablecerPassword(@Valid @RequestBody RestablecerPasswordRequest request,
			HttpServletRequest httpRequest) {

		try {

			service.restablecerPassword(request.getToken(), request.getPassword());

			SecurityContextHolder.clearContext();

			HttpSession sesion = httpRequest.getSession(false);

			if (sesion != null) {
				sesion.invalidate();
			}

			return ResponseEntity.ok("Contraseña actualizada correctamente. " + "Ya puedes iniciar sesión.");

		} catch (IllegalArgumentException error) {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.getMessage());
		}
	}
}