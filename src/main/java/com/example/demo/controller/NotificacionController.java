package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Notificacion;
import com.example.demo.service.NotificacionService;

// Todas las rutas /api/notificaciones/** exigen sesión (lo hace SecurityConfig con anyRequest).
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

	private final NotificacionService notificacionService;

	public NotificacionController(NotificacionService notificacionService) {
		this.notificacionService = notificacionService;
	}

	// GET /api/notificaciones -> mis notificaciones (las 50 últimas).
	@GetMapping
	public List<Notificacion> misNotificaciones(Authentication auth) {
		return notificacionService.misNotificaciones(auth.getName());
	}

	// GET /api/notificaciones/no-leidas -> { "total": 3 } (para el globo de la campana).
	@GetMapping("/no-leidas")
	public Map<String, Long> noLeidas(Authentication auth) {
		return Map.of("total", notificacionService.contarNoLeidas(auth.getName()));
	}

	// PUT /api/notificaciones/{id}/leida -> marca una como leída.
	@PutMapping("/{id}/leida")
	public Notificacion marcarLeida(@PathVariable Long id, Authentication auth) {
		return notificacionService.marcarLeida(auth.getName(), id);
	}

	// PUT /api/notificaciones/leidas -> marca todas como leídas.
	@PutMapping("/leidas")
	public Map<String, Integer> marcarTodasLeidas(Authentication auth) {
		return Map.of("marcadas", notificacionService.marcarTodasLeidas(auth.getName()));
	}

	// Notificación inexistente (o de otro usuario) -> 404.
	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String noEncontrada(NoSuchElementException error) {
		return error.getMessage();
	}
}
