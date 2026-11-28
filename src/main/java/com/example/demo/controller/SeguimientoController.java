
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UsuarioResumenDTO;
import com.example.demo.service.SeguimientoService;

@RestController
@RequestMapping("/api/usuarios")
public class SeguimientoController {

	private final SeguimientoService seguimientoService;

	public SeguimientoController(SeguimientoService seguimientoService) {

		this.seguimientoService = seguimientoService;
	}

	@PostMapping("/{seguidorId}/seguir/{seguidoId}")
	public ResponseEntity<Void> seguir(@PathVariable Long seguidorId, @PathVariable Long seguidoId) {

		seguimientoService.seguir(seguidorId, seguidoId);

		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{seguidorId}/seguir/{seguidoId}")
	public ResponseEntity<Void> dejarDeSeguir(@PathVariable Long seguidorId, @PathVariable Long seguidoId) {

		seguimientoService.dejarDeSeguir(seguidorId, seguidoId);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/{id}/seguidores")
	public List<UsuarioResumenDTO> seguidores(@PathVariable Long id) {

		return seguimientoService.obtenerSeguidores(id);
	}

	@GetMapping("/{id}/seguidos")
	public List<UsuarioResumenDTO> seguidos(@PathVariable Long id) {

		return seguimientoService.obtenerSeguidos(id);
	}

}
