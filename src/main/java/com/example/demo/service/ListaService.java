package com.example.demo.service;

import com.example.demo.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ListaConObraResponse;
import com.example.demo.dto.ListaDetalleResponse;
import com.example.demo.dto.ListaResponse;
import com.example.demo.dto.SearchResultDTO;
import com.example.demo.entity.Lista;
import com.example.demo.entity.ListaObra;
import com.example.demo.entity.Obra;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.ListaObraRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.repository.ObraRepository;

@Service
public class ListaService {

	private static final int MAX_NOMBRE = 100;

	private final ListaRepository listaRepository;
	private final ListaObraRepository listaObraRepository;
	private final ObraRepository obraRepository;
	private final PerfilService perfilService;
	private final UsuarioRepository usuarioRepository;

	public ListaService(ListaRepository listaRepository, ListaObraRepository listaObraRepository,
			ObraRepository obraRepository, PerfilService perfilService, UsuarioRepository usuarioRepository) {

		this.listaRepository = listaRepository;
		this.listaObraRepository = listaObraRepository;
		this.obraRepository = obraRepository;
		this.perfilService = perfilService;
		this.usuarioRepository = usuarioRepository;
	}

	// H1.6 - Crear una lista.
	@Transactional
	public ListaResponse crear(String email, String nombre, String descripcion, boolean publica) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);
		String nombreLimpio = validarNombre(nombre);

		Lista lista = new Lista(usuario.getId(), nombreLimpio, publica);

		lista.setDescripcion(descripcion);

		lista = listaRepository.save(lista);

		return convertir(lista);
	}

	// H1.6 / H1.7 - Mis listas, con el número de obras de cada una.
	@Transactional(readOnly = true)
	public List<ListaResponse> misListas(String email) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		return listaRepository.findByUsuarioId(usuario.getId()).stream().map(this::convertir)
				.collect(Collectors.toList());
	}

	// Ver una lista con sus obras.
	// Pública: la ve cualquier usuario con sesión. Privada: solo su dueño.
	@Transactional(readOnly = true)
	public ListaDetalleResponse obtenerLista(String email, Long listaId) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		Lista lista = listaRepository.findById(listaId)
				.orElseThrow(() -> new IllegalArgumentException("Lista no encontrada"));

		boolean esMia = lista.getUsuarioId().equals(usuario.getId());

		// Si es privada y no es mía, respondemos como si no existiera.
		if (!lista.isPublica() && !esMia) {
			throw new IllegalArgumentException("Lista no encontrada");
		}

		List<Long> idsObras = listaObraRepository.findByListaId(listaId).stream().map(ListaObra::getObraId)
				.collect(Collectors.toList());

		List<SearchResultDTO> obras = new ArrayList<>();

		for (Obra obra : obraRepository.findAllById(idsObras)) {
			obras.add(new SearchResultDTO(obra.getId(), obra.getTitulo(),
					obra.getTipo() == null ? "" : obra.getTipo().name()));
		}

		Usuario propietario = usuarioRepository.findById(lista.getUsuarioId()).orElse(null);

		String creador = propietario != null ? propietario.getNombre() : "Desconocido";

		return new ListaDetalleResponse(lista.getId(), lista.getNombre(), lista.isPublica(), esMia,
				lista.getDescripcion(), creador, obras);
	}

	// H1.6 - Modificar nombre y visibilidad.
	@Transactional
	public ListaResponse modificar(String email, Long listaId, String nombre, String descripcion, boolean publica) {

		Lista lista = obtenerListaPropia(email, listaId);

		lista.setNombre(validarNombre(nombre));
		lista.setPublica(publica);

		lista.setDescripcion(descripcion);

		return convertir(listaRepository.save(lista));
	}

	// Eliminar una lista y sus obras asociadas.
	@Transactional
	public void eliminar(String email, Long listaId) {

		Lista lista = obtenerListaPropia(email, listaId);

		listaObraRepository.deleteByListaId(lista.getId());
		listaRepository.delete(lista);
	}

	// H1.7 - Añadir una obra a una lista.
	@Transactional
	public ListaResponse anadirObra(String email, Long listaId, Long obraId) {

		Lista lista = obtenerListaPropia(email, listaId);

		if (!obraRepository.existsById(obraId)) {
			throw new IllegalArgumentException("La obra no existe");
		}

		if (listaObraRepository.existsByListaIdAndObraId(listaId, obraId)) {
			throw new IllegalArgumentException("La obra ya está en esta lista");
		}

		listaObraRepository.save(new ListaObra(listaId, obraId));

		return convertir(lista);
	}

	// H1.7 - Quitar una obra de una lista.
	@Transactional
	public ListaResponse quitarObra(String email, Long listaId, Long obraId) {

		Lista lista = obtenerListaPropia(email, listaId);

		if (!listaObraRepository.existsByListaIdAndObraId(listaId, obraId)) {
			throw new IllegalArgumentException("La obra no está en esta lista");
		}

		listaObraRepository.deleteByListaIdAndObraId(listaId, obraId);

		return convertir(lista);
	}

	// Mis listas indicando si ya contienen la obra (para work.html).
	@Transactional(readOnly = true)
	public List<ListaConObraResponse> listasParaObra(String email, Long obraId) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		return listaRepository.findByUsuarioId(usuario.getId()).stream().map(l -> new ListaConObraResponse(l.getId(),
				l.getNombre(), listaObraRepository.existsByListaIdAndObraId(l.getId(), obraId)))
				.collect(Collectors.toList());
	}

	// ---------- Métodos auxiliares ----------

	// Devuelve la lista solo si pertenece al usuario de la sesión.
	private Lista obtenerListaPropia(String email, Long listaId) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		Lista lista = listaRepository.findById(listaId)
				.orElseThrow(() -> new IllegalArgumentException("Lista no encontrada"));

		if (!lista.getUsuarioId().equals(usuario.getId())) {
			// No revelamos que existe una lista de otro usuario.
			throw new IllegalArgumentException("Lista no encontrada");
		}

		return lista;
	}

	private String validarNombre(String nombre) {

		if (nombre == null || nombre.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre de la lista es obligatorio");
		}

		String limpio = nombre.trim();

		if (limpio.length() > MAX_NOMBRE) {
			throw new IllegalArgumentException("El nombre no puede superar los " + MAX_NOMBRE + " caracteres");
		}

		return limpio;
	}

	private ListaResponse convertir(Lista lista) {

		Usuario usuario = usuarioRepository.findById(lista.getUsuarioId()).orElse(null);

		String creador = usuario != null ? usuario.getNombre() : "Desconocido";

		return new ListaResponse(lista.getId(), lista.getNombre(), lista.isPublica(),
				listaObraRepository.countByListaId(lista.getId()), lista.getDescripcion(), creador);
	}

	@Transactional(readOnly = true)
	public List<ListaResponse> obtenerListasPublicas() {

		return listaRepository.findByPublicaTrue().stream().map(this::convertir).toList();
	}

	@Transactional(readOnly = true)
	public List<ListaResponse> buscarListas(String texto) {

		return listaRepository.findByPublicaTrueAndNombreContainingIgnoreCase(texto).stream().map(this::convertir)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ListaResponse> obtenerSugerenciasListas(String texto) {

		return listaRepository.findTop5ByPublicaTrueAndNombreStartingWithIgnoreCase(texto).stream().map(this::convertir)
				.toList();
	}

}