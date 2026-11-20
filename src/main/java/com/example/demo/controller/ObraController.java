package com.example.demo.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ModeracionRequest;
import com.example.demo.dto.ObraEnvioResponse;
import com.example.demo.dto.ObraRequest;
import com.example.demo.dto.SearchResultDTO;
import com.example.demo.dto.ValidacionResponse;
import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.EstadoValidacion;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.service.ObraService;

@RestController
public class ObraController {

	private final ObraService obraService;

	// Ahora toda la lógica vive en ObraService; el controlador solo recibe y responde.
	public ObraController(ObraService obraService) {
		this.obraService = obraService;
	}

	// Convierte una obra en un DTO ligero (id, título, tipo).
	private SearchResultDTO aDTO(Obra obra) {
		return new SearchResultDTO(obra.getId(), obra.getTitulo(), obra.getTipo() == null ? "" : obra.getTipo().name());
	}

	// =====================================================================
	// BÚSQUEDA Y FICHA (sin cambios)
	// =====================================================================

	@GetMapping("/search")
	public List<SearchResultDTO> buscar(@RequestParam(required = false) String titulo,
			@RequestParam(required = false) TipoObra tipo) {

		return obraService.buscar(titulo, tipo).stream().map(this::aDTO).collect(Collectors.toList());
	}

	@GetMapping("/explorar")
	public List<SearchResultDTO> explorar() {
		return obraService.explorar().stream().map(this::aDTO).collect(Collectors.toList());
	}

	@GetMapping("/work/{id}")
	public ResponseEntity<Obra> obtenerObra(@PathVariable Long id) {
		Obra obra = obraService.obtenerPorId(id);

		if (obra == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(obra);
	}

	@GetMapping("/sugerencias")
	public List<SearchResultDTO> obtenerSugerencias(@RequestParam String texto) {
		return obraService.obtenerSugerencias(texto).stream().map(this::aDTO).collect(Collectors.toList());
	}

	@PutMapping("/work/{id}/estado")
	public ResponseEntity<Obra> cambiarEstado(@PathVariable Long id, @RequestParam EstadoObra estado) {

		Obra obra = obraService.obtenerPorId(id);

		if (obra == null) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(obra);
	}

	// =====================================================================
	// H1.3 - ENVIAR, EDITAR Y CONSULTAR MIS OBRAS
	// =====================================================================

	// Crear una obra (queda pendiente de validación).
	@PostMapping("/api/obras")
	public Obra crearObra(@RequestBody ObraRequest request, Authentication auth) {
		return obraService.crear(auth.getName(), request);
	}

	// Editar una obra: solo su autor o un moderador. Si edita el autor, vuelve a PENDIENTE.
	@PutMapping("/api/obras/{id}")
	public Obra editarObra(@PathVariable Long id, @RequestBody ObraRequest request, Authentication auth) {
		return obraService.editar(auth.getName(), id, request);
	}

	// Mis obras enviadas, con su estado y la justificación del moderador.
	@GetMapping("/api/obras/mias")
	public List<ObraEnvioResponse> misObras(Authentication auth) {
		return obraService.misObras(auth.getName());
	}

	// Estado de validación + justificación (ahora devuelve JSON, no solo el texto del estado).
	@GetMapping("/api/obras/{id}/validacion")
	public ValidacionResponse validacion(@PathVariable Long id, Authentication auth) {
		return obraService.consultarValidacion(auth.getName(), id);
	}

	// =====================================================================
	// H8.1 - MODERACIÓN (solo moderadores)
	// =====================================================================

	// Obras por estado. Sustituye al antiguo /api/obras/pendientes.
	// Ejemplo: GET /api/obras/moderacion?estado=PENDIENTE (por defecto)
	@GetMapping("/api/obras/moderacion")
	public List<ObraEnvioResponse> obrasParaModerar(
			@RequestParam(defaultValue = "PENDIENTE") EstadoValidacion estado, Authentication auth) {

		return obraService.listarParaModerar(auth.getName(), estado);
	}

	// Validar con justificación opcional. Body: { "justificacion": "..." }
	@PutMapping("/api/obras/{id}/validar")
	public Obra validarObra(@PathVariable Long id, @RequestBody(required = false) ModeracionRequest cuerpo,
			Authentication auth) {

		return obraService.validar(auth.getName(), id, cuerpo == null ? null : cuerpo.getJustificacion());
	}

	// Rechazar con justificación OBLIGATORIA. Body: { "justificacion": "..." }
	@PutMapping("/api/obras/{id}/rechazar")
	public Obra rechazarObra(@PathVariable Long id, @RequestBody(required = false) ModeracionRequest cuerpo,
			Authentication auth) {

		return obraService.rechazar(auth.getName(), id, cuerpo == null ? null : cuerpo.getJustificacion());
	}

	// =====================================================================
	// ERRORES: texto claro y código HTTP correcto (antes todo daba 500)
	// =====================================================================

	// Datos incorrectos (título vacío, falta justificación...) -> 400
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String datosIncorrectos(IllegalArgumentException error) {
		return error.getMessage();
	}

	// Sin permisos (no eres el autor / no eres moderador) -> 403
	@ExceptionHandler(SecurityException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String sinPermisos(SecurityException error) {
		return error.getMessage();
	}

	// La obra no existe -> 404
	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String noEncontrada(NoSuchElementException error) {
		return error.getMessage();
	}
}
