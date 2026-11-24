
package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.service.ObraExternaService;

@RestController
@RequestMapping("/api/obras/externas")
public class ObraExternaController {

	private final ObraExternaService obraExternaService;

	// Inyectamos el servicio que gestiona las referencias externas.
	public ObraExternaController(ObraExternaService obraExternaService) {
		this.obraExternaService = obraExternaService;
	}

	// Datos necesarios para identificar una obra externa.
	public record ReferenciaExternaRequest(String fuenteApi, String idExterno, TipoObra tipo, String titulo) {
	}

	// Devolvemos únicamente el identificador de Neon.
	public record ReferenciaExternaResponse(Long id) {
	}

	/**
	 * Obtiene o crea una referencia externa cuando el usuario necesita guardar una
	 * interacción con la obra.
	 *
	 * No debe llamarse al abrir una ficha o realizar una búsqueda.
	 */
	@PostMapping
	public ReferenciaExternaResponse obtenerOCrearReferencia(@RequestBody ReferenciaExternaRequest request,
			Authentication authentication) {

		// Solo los usuarios autenticados pueden registrar referencias.
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new SecurityException("Debes iniciar sesión para guardar esta obra");
		}

		// Reutilizamos el servicio existente para evitar duplicados.
		Obra obra = obraExternaService.obtenerOCrearReferencia(request.fuenteApi(), request.idExterno(), request.tipo(),
				request.titulo());

		return new ReferenciaExternaResponse(obra.getId());
	}

	// Los datos de entrada incorrectos devuelven HTTP 400.

	/**
	 * Consulta si una obra externa ya tiene referencia en Neon.
	 *
	 * No crea ni modifica registros.
	 */

	/**
	 * Consulta si una obra externa ya existe en Neon. No crea ni modifica
	 * registros.
	 */
	@GetMapping
	public ReferenciaExternaResponse buscarReferencia(@RequestParam String fuenteApi, @RequestParam String idExterno,
			@RequestParam TipoObra tipo) {

		Obra obra = obraExternaService.buscarReferencia(fuenteApi, idExterno, tipo)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"La obra todavía no tiene una referencia en Neon"));

		return new ReferenciaExternaResponse(obra.getId());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String datosIncorrectos(IllegalArgumentException error) {
		return error.getMessage();
	}

	// Los usuarios sin sesión reciben HTTP 403.
	@ExceptionHandler(SecurityException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String sinPermisos(SecurityException error) {
		return error.getMessage();
	}
}
