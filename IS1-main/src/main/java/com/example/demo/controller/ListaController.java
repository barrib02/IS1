package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ListaConObraResponse;
import com.example.demo.dto.ListaDetalleResponse;
import com.example.demo.dto.ListaRequest;
import com.example.demo.dto.ListaResponse;
import com.example.demo.service.ListaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/listas")
public class ListaController {

	private final ListaService listaService;

	public ListaController(ListaService listaService) {
		this.listaService = listaService;
	}

	// Crear lista.
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ListaResponse crear(@Valid @RequestBody ListaRequest request, Authentication auth) {

		return listaService.crear(auth.getName(), request.getNombre(), request.isPublica());
	}

	// Mis listas (con contador de obras).
	@GetMapping
	public List<ListaResponse> misListas(Authentication auth) {
		return listaService.misListas(auth.getName());
	}

	// Mis listas indicando cuáles contienen una obra.
	@GetMapping("/obra/{obraId}")
	public List<ListaConObraResponse> listasParaObra(@PathVariable Long obraId, Authentication auth) {

		return listaService.listasParaObra(auth.getName(), obraId);
	}

	// Ver una lista con sus obras.
	@GetMapping("/{id}")
	public ListaDetalleResponse obtenerLista(@PathVariable Long id, Authentication auth) {

		return listaService.obtenerLista(auth.getName(), id);
	}

	// Modificar nombre y visibilidad.
	@PutMapping("/{id}")
	public ListaResponse modificar(@PathVariable Long id, @Valid @RequestBody ListaRequest request,
			Authentication auth) {

		return listaService.modificar(auth.getName(), id, request.getNombre(), request.isPublica());
	}

	// Eliminar lista.
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Long id, Authentication auth) {
		listaService.eliminar(auth.getName(), id);
	}

	// Añadir obra a una lista.
	@PostMapping("/{id}/obras/{obraId}")
	public ListaResponse anadirObra(@PathVariable Long id, @PathVariable Long obraId, Authentication auth) {

		return listaService.anadirObra(auth.getName(), id, obraId);
	}

	// Quitar obra de una lista.
	@DeleteMapping("/{id}/obras/{obraId}")
	public ListaResponse quitarObra(@PathVariable Long id, @PathVariable Long obraId, Authentication auth) {

		return listaService.quitarObra(auth.getName(), id, obraId);
	}

	// Errores de negocio.
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}

	// Errores de validación de @Valid.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarValidacion(MethodArgumentNotValidException error) {
		return error.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
	}
}