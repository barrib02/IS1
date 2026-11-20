package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ObraEnvioResponse;
import com.example.demo.dto.ObraRequest;
import com.example.demo.dto.ValidacionResponse;
import com.example.demo.entity.EstadoValidacion;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.ObraRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class ObraService {

	// Máximo de obras que devuelve una búsqueda (evita cargar todo el catálogo).
	private static final int MAX_RESULTADOS = 50;

	// Longitud máxima de la justificación del moderador (coincide con la columna).
	private static final int MAX_JUSTIFICACION = 1000;

	private final ObraRepository obraRepository;
	private final PerfilService perfilService;
	private final NotificacionService notificacionService;
	private final UsuarioRepository usuarioRepository;

	public ObraService(ObraRepository obraRepository, PerfilService perfilService,
			NotificacionService notificacionService, UsuarioRepository usuarioRepository) {

		this.obraRepository = obraRepository;
		this.perfilService = perfilService;
		this.notificacionService = notificacionService;
		this.usuarioRepository = usuarioRepository;
	}

	// =====================================================================
	// BÚSQUEDA Y CONSULTA (sin cambios respecto al sprint anterior)
	// =====================================================================

	public List<Obra> getObras() {
		return obraRepository.findAll();
	}

	public List<Obra> buscarPorTitulo(String texto) {
		return obraRepository.findByTituloContainingIgnoreCase(texto);
	}

	// Busca por título y/o tipo. Devuelve como máximo 50 obras ordenadas por título.
	public List<Obra> buscar(String titulo, TipoObra tipo) {

		Pageable limite = PageRequest.of(0, MAX_RESULTADOS, Sort.by("titulo"));

		if (titulo != null && !titulo.isBlank() && tipo != null) {
			return obraRepository.findByTituloContainingIgnoreCaseAndTipo(titulo, tipo, limite);
		}

		if (titulo != null && !titulo.isBlank()) {
			return obraRepository.findByTituloContainingIgnoreCase(titulo, limite);
		}

		if (tipo != null) {
			return obraRepository.findByTipo(tipo, limite);
		}

		return obraRepository.findAll(limite).getContent();
	}

	// Obras más recientes (pantalla de búsqueda antes de escribir nada).
	public List<Obra> explorar() {
		return obraRepository.findTop12ByOrderByIdDesc();
	}

	public List<Obra> obtenerSugerencias(String texto) {
		return obraRepository.findTop5ByTituloStartingWithIgnoreCase(texto);
	}

	// Obra con sus géneros cargados en una sola consulta.
	public Obra obtenerPorId(Long id) {
		return obraRepository.findConGenerosById(id).orElse(null);
	}

	// =====================================================================
	// H1.3 - ENVIAR Y EDITAR OBRAS
	// =====================================================================

	// Crea una obra nueva. Queda PENDIENTE y se guarda quién la ha enviado (autorId),
	// que es lo que permite luego editarla y avisar a esa persona.
	@Transactional
	public Obra crear(String email, ObraRequest request) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		Obra obra = new Obra();
		aplicarDatos(obra, request);

		obra.setAutorId(usuario.getId());
		obra.setValidada(false);
		obra.setEstadoValidacion(EstadoValidacion.PENDIENTE);

		return obraRepository.save(obra);
	}

	// Edita una obra. Pueden hacerlo su autor o un moderador.
	// REVALIDACIÓN AUTOMÁTICA: si el AUTOR cambia algo, la obra vuelve a PENDIENTE
	// (aunque estuviera validada o rechazada) para que un moderador la revise otra vez.
	@Transactional
	public Obra editar(String email, Long id, ObraRequest request) {

		Obra obra = buscarObra(id);
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		boolean esAutor = esAutor(obra, usuario);

		if (!esAutor && !perfilService.esModerador(email)) {
			throw new SecurityException("Solo el autor o un moderador pueden editar esta obra");
		}

		// Guardamos los valores antiguos para saber si ha cambiado algo de verdad.
		String tituloAntes = limpiar(obra.getTitulo());
		String autorAntes = limpiar(obra.getAutor());
		String descripcionAntes = limpiar(obra.getDescripcion());
		TipoObra tipoAntes = obra.getTipo();

		aplicarDatos(obra, request);

		boolean hayCambios = !Objects.equals(tituloAntes, limpiar(obra.getTitulo()))
				|| !Objects.equals(autorAntes, limpiar(obra.getAutor()))
				|| !Objects.equals(descripcionAntes, limpiar(obra.getDescripcion()))
				|| tipoAntes != obra.getTipo();

		if (esAutor && hayCambios) {
			obra.setEstadoValidacion(EstadoValidacion.PENDIENTE);
			obra.setValidada(false);
			obra.setJustificacionModeracion(null); // el motivo anterior ya no aplica
		}

		return obraRepository.save(obra);
	}

	// Obras enviadas por el usuario con sesión (con su estado y justificación).
	@Transactional(readOnly = true)
	public List<ObraEnvioResponse> misObras(String email) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		return obraRepository.findByAutorIdOrderByIdDesc(usuario.getId()).stream()
				.map(o -> aEnvio(o, usuario.getNombre())).collect(Collectors.toList());
	}

	// Estado de validación + justificación. Solo lo ve el autor o un moderador.
	@Transactional(readOnly = true)
	public ValidacionResponse consultarValidacion(String email, Long id) {

		Obra obra = buscarObra(id);
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		if (!esAutor(obra, usuario) && !perfilService.esModerador(email)) {
			throw new SecurityException("No puedes consultar el estado de esta obra");
		}

		return new ValidacionResponse(estadoComoTexto(obra), obra.getJustificacionModeracion());
	}

	// =====================================================================
	// H8.1 - MODERACIÓN
	// =====================================================================

	// Lista de obras según su estado (PENDIENTE, VALIDADA o RECHAZADA). Solo moderadores.
	@Transactional(readOnly = true)
	public List<ObraEnvioResponse> listarParaModerar(String email, EstadoValidacion estado) {

		exigirModerador(email);

		List<Obra> obras = obraRepository.findByEstadoValidacionOrderByIdAsc(estado);

		// Buscamos los nombres de los autores de golpe (una consulta, no una por obra).
		Set<Long> idsAutores = obras.stream().map(Obra::getAutorId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<Long, String> nombres = usuarioRepository.findAllById(idsAutores).stream()
				.collect(Collectors.toMap(Usuario::getId, Usuario::getNombre));

		return obras.stream().map(o -> aEnvio(o, nombres.get(o.getAutorId()))).collect(Collectors.toList());
	}

	// Valida una obra. La justificación es opcional. Avisa al autor.
	@Transactional
	public Obra validar(String email, Long id, String justificacion) {

		exigirModerador(email);
		String nota = limpiarJustificacion(justificacion, false);

		Obra obra = buscarObra(id);
		obra.setEstadoValidacion(EstadoValidacion.VALIDADA);
		obra.setValidada(true);
		obra.setJustificacionModeracion(nota);

		Obra guardada = obraRepository.save(obra);

		String mensaje = "Tu obra \"" + guardada.getTitulo() + "\" ha sido validada.";
		if (nota != null) {
			mensaje += " Nota del moderador: " + nota;
		}
		notificarAutor(guardada, "OBRA_VALIDADA", mensaje);

		return guardada;
	}

	// Rechaza una obra. La justificación es OBLIGATORIA. Avisa al autor.
	@Transactional
	public Obra rechazar(String email, Long id, String justificacion) {

		exigirModerador(email);
		String motivo = limpiarJustificacion(justificacion, true);

		Obra obra = buscarObra(id);
		obra.setEstadoValidacion(EstadoValidacion.RECHAZADA);
		obra.setValidada(false);
		obra.setJustificacionModeracion(motivo);

		Obra guardada = obraRepository.save(obra);

		notificarAutor(guardada, "OBRA_RECHAZADA",
				"Tu obra \"" + guardada.getTitulo() + "\" ha sido rechazada. Motivo: " + motivo);

		return guardada;
	}

	// =====================================================================
	// MÉTODOS AUXILIARES
	// =====================================================================

	private Obra buscarObra(Long id) {
		return obraRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Obra no encontrada"));
	}

	private boolean esAutor(Obra obra, Usuario usuario) {
		return obra.getAutorId() != null && obra.getAutorId().equals(usuario.getId());
	}

	// Lanza SecurityException (el controlador la convierte en un 403).
	private void exigirModerador(String email) {
		if (!perfilService.esModerador(email)) {
			throw new SecurityException("Solo los moderadores pueden realizar esta acción");
		}
	}

	// Copia los datos del formulario a la obra, validándolos.
	private void aplicarDatos(Obra obra, ObraRequest request) {

		String titulo = limpiar(request.getTitulo());

		if (titulo.isEmpty()) {
			throw new IllegalArgumentException("El título es obligatorio");
		}

		if (titulo.length() > 255) {
			throw new IllegalArgumentException("El título no puede superar los 255 caracteres");
		}

		obra.setTitulo(titulo);
		obra.setAutor(limpiar(request.getAutor()));
		obra.setDescripcion(limpiar(request.getDescripcion()));
		obra.setTipo(parsearTipo(request.getTipo()));
	}

	private TipoObra parsearTipo(String texto) {

		if (texto == null || texto.isBlank()) {
			throw new IllegalArgumentException("Elige un tipo de obra");
		}

		try {
			return TipoObra.valueOf(texto.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("El tipo de obra no es válido");
		}
	}

	// Quita espacios; null se convierte en cadena vacía.
	private String limpiar(String texto) {
		return texto == null ? "" : texto.trim();
	}

	// Devuelve null si está vacía (y falla si es obligatoria o demasiado larga).
	private String limpiarJustificacion(String texto, boolean obligatoria) {

		String limpio = limpiar(texto);

		if (limpio.isEmpty()) {
			if (obligatoria) {
				throw new IllegalArgumentException("Debes escribir una justificación para rechazar la obra");
			}
			return null;
		}

		if (limpio.length() > MAX_JUSTIFICACION) {
			throw new IllegalArgumentException(
					"La justificación no puede superar los " + MAX_JUSTIFICACION + " caracteres");
		}

		return limpio;
	}

	// Avisa al autor. Las obras antiguas no tienen autorId, así que no hay a quién avisar.
	private void notificarAutor(Obra obra, String tipo, String mensaje) {
		if (obra.getAutorId() == null) {
			return;
		}
		notificacionService.crear(obra.getAutorId(), obra.getId(), tipo, mensaje);
	}

	private String estadoComoTexto(Obra obra) {
		return obra.getEstadoValidacion() == null ? "PENDIENTE" : obra.getEstadoValidacion().name();
	}

	private ObraEnvioResponse aEnvio(Obra obra, String enviadaPor) {
		return new ObraEnvioResponse(obra.getId(), obra.getTitulo(), obra.getAutor(), obra.getDescripcion(),
				obra.getTipo() == null ? "" : obra.getTipo().name(), estadoComoTexto(obra),
				obra.getJustificacionModeracion(), enviadaPor);
	}
}
