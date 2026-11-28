package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.SocialDTOs;
import com.example.demo.dto.UsuarioBusquedaDTO;
import com.example.demo.service.SocialService;

@RestController
@RequestMapping("/api")
public class SocialController {

	private final SocialService social;

	public SocialController(SocialService social) {
		this.social = social;
	}

	// ----- Ver el perfil de otro usuario -----

	@GetMapping("/vista-usuario/{id}")
	public SocialDTOs.Vista vista(@PathVariable Long id, Authentication auth) {
		return social.vista(auth.getName(), id);
	}

	@GetMapping("/vista-usuario/{id}/seguidores")
	public List<UsuarioBusquedaDTO> seguidores(@PathVariable Long id, Authentication auth) {
		return social.seguidores(auth.getName(), id);
	}

	@GetMapping("/vista-usuario/{id}/seguidos")
	public List<UsuarioBusquedaDTO> seguidos(@PathVariable Long id, Authentication auth) {
		return social.seguidos(auth.getName(), id);
	}

	// ----- Seguir / dejar de seguir -----

	@PostMapping("/seguimiento/{id}")
	public Map<String, String> seguir(@PathVariable Long id, Authentication auth) {
		return Map.of("relacion", social.seguir(auth.getName(), id));
	}

	@DeleteMapping("/seguimiento/{id}")
	public Map<String, String> dejarDeSeguir(@PathVariable Long id, Authentication auth) {
		return Map.of("relacion", social.dejarDeSeguir(auth.getName(), id));
	}

	// ----- Solicitudes recibidas -----

	@GetMapping("/seguimiento/solicitudes")
	public List<SocialDTOs.Solicitud> solicitudes(Authentication auth) {
		return social.solicitudesRecibidas(auth.getName());
	}

	@PostMapping("/seguimiento/solicitudes/{id}/aceptar")
	public Map<String, String> aceptar(@PathVariable Long id, Authentication auth) {
		social.aceptar(auth.getName(), id);
		return Map.of("estado", "ACEPTADA");
	}

	@PostMapping("/seguimiento/solicitudes/{id}/rechazar")
	public Map<String, String> rechazar(@PathVariable Long id, Authentication auth) {
		social.rechazar(auth.getName(), id);
		return Map.of("estado", "RECHAZADA");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}
}
