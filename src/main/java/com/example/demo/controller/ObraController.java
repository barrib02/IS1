package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.service.PerfilService;

import java.util.List;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ObraRequest;
import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.EstadoValidacion;
import com.example.demo.entity.Obra;
import com.example.demo.service.ObraService;

@RestController
public class ObraController {

	private final ObraService obraService;
	private final ObraRepository obraRepository;
	private final PerfilService perfilService;

	public ObraController(ObraService obraService, ObraRepository obraRepository, PerfilService perfilService) {

		this.obraService = obraService;
		this.obraRepository = obraRepository;
		this.perfilService = perfilService;
	}

	@GetMapping("/search")
	public List<Obra> buscar(@RequestParam(required = false) String titulo,
			@RequestParam(required = false) TipoObra tipo) {

		return obraService.buscar(titulo, tipo);
	}

	@GetMapping("/work/{id}")
	public ResponseEntity<Obra> obtenerObra(@PathVariable Long id) {
		// Búsqueda directa y segura por ID en la base de datos
		Obra obra = obraService.obtenerPorId(id);

		if (obra == null) {
			return ResponseEntity.notFound().build(); // Devuelve 404 real si no existe
		}

		return ResponseEntity.ok(obra); // Devuelve 200 OK con el JSON de la obra
	}

	@GetMapping("/sugerencias")
	public List<Obra> obtenerSugerencias(

			@RequestParam String texto) {

		return obraService.obtenerSugerencias(texto);
	}

	@PutMapping("/work/{id}/estado")
	public ResponseEntity<Obra> cambiarEstado(@PathVariable Long id, @RequestParam EstadoObra estado) {

		Obra obra = obraService.obtenerPorId(id);

		if (obra != null) {
			// obra.setEstado(estado); // Descomenta cuando implementes el campo estado en
			// la entidad
		} else {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(obra);
	}

	@PostMapping("/api/obras")
	public Obra crearObra(@RequestBody ObraRequest request, Authentication auth) {

		Obra obra = new Obra();

		obra.setTitulo(request.getTitulo());
		obra.setDescripcion(request.getDescripcion());
		obra.setAutor(request.getAutor());

		obra.setTipo(TipoObra.valueOf(request.getTipo()));

		obra.setValidada(false);
		obra.setEstadoValidacion(EstadoValidacion.PENDIENTE);

		return obraRepository.save(obra);
	}

	@PutMapping("/api/obras/{id}")
	public Obra editarObra(@PathVariable Long id, @RequestBody ObraRequest request) {

		Obra obra = obraRepository.findById(id).orElseThrow();

		obra.setTitulo(request.getTitulo());
		obra.setDescripcion(request.getDescripcion());
		obra.setAutor(request.getAutor());

		return obraRepository.save(obra);
	}

	@GetMapping("/api/obras/{id}/validacion")
	public String validacion(@PathVariable Long id) {

		Obra obra = obraRepository.findById(id).orElseThrow();

		return obra.getEstadoValidacion().name();
	}

	@GetMapping("/api/obras/pendientes")
	public List<Obra> obrasPendientes(Authentication auth) {

		if (!perfilService.esModerador(auth.getName())) {

			throw new IllegalArgumentException("Solo los moderadores pueden acceder");
		}

		return obraRepository.findAll().stream()
				.filter(obra -> obra.getEstadoValidacion() == EstadoValidacion.PENDIENTE).collect(Collectors.toList());
	}

	@PutMapping("/api/obras/{id}/validar")
	public Obra validarObra(@PathVariable Long id, Authentication auth) {

		if (!perfilService.esModerador(auth.getName())) {
			throw new IllegalArgumentException("Solo los moderadores pueden validar obras");
		}

		Obra obra = obraRepository.findById(id).orElseThrow();

		obra.setEstadoValidacion(EstadoValidacion.VALIDADA);

		obra.setValidada(true);

		return obraRepository.save(obra);
	}

	@PutMapping("/api/obras/{id}/rechazar")
	public Obra rechazarObra(@PathVariable Long id, Authentication auth) {

		if (!perfilService.esModerador(auth.getName())) {

			throw new IllegalArgumentException("Solo los moderadores pueden rechazar obras");
		}

		Obra obra = obraRepository.findById(id).orElseThrow();

		obra.setEstadoValidacion(EstadoValidacion.RECHAZADA);

		obra.setValidada(false);

		return obraRepository.save(obra);
	}

}
