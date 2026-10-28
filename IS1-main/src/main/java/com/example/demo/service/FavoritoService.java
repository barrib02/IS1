package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.FavoritoResponse;
import com.example.demo.entity.Favorito;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ObraRepository;

@Service
public class FavoritoService {

	private static final int MAX_POR_CATEGORIA = 5;

	private final FavoritoRepository favoritoRepository;
	private final ObraRepository obraRepository;
	private final PerfilService perfilService;

	public FavoritoService(FavoritoRepository favoritoRepository, ObraRepository obraRepository,
			PerfilService perfilService) {
		this.favoritoRepository = favoritoRepository;
		this.obraRepository = obraRepository;
		this.perfilService = perfilService;
	}

	// Mis favoritos, ordenados por tipo y posición.
	@Transactional(readOnly = true)
	public List<FavoritoResponse> misFavoritos(String email) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		return convertir(favoritoRepository.findByUsuarioIdOrderByTipoAscPosicionAsc(usuario.getId()));
	}

	// Fijar una obra como favorita.
	@Transactional
	public FavoritoResponse anadir(String email, Long obraId) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		Obra obra = obraRepository.findById(obraId)
				.orElseThrow(() -> new IllegalArgumentException("La obra no existe"));

		if (obra.getTipo() == null) {
			throw new IllegalArgumentException("La obra no tiene tipo");
		}

		if (favoritoRepository.findByUsuarioIdAndObraId(usuario.getId(), obraId).isPresent()) {
			throw new IllegalArgumentException("La obra ya está en tus favoritos");
		}

		long cantidad = favoritoRepository.countByUsuarioIdAndTipo(usuario.getId(), obra.getTipo());

		if (cantidad >= MAX_POR_CATEGORIA) {
			throw new IllegalArgumentException("Solo puedes fijar " + MAX_POR_CATEGORIA + " favoritos por categoría");
		}

		Favorito favorito = favoritoRepository
				.save(new Favorito(usuario.getId(), obraId, obra.getTipo(), (int) cantidad + 1));

		return convertir(List.of(favorito)).get(0);
	}

	// Quitar un favorito y recompactar las posiciones de su categoría.
	@Transactional
	public void quitar(String email, Long obraId) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		Favorito favorito = favoritoRepository.findByUsuarioIdAndObraId(usuario.getId(), obraId)
				.orElseThrow(() -> new IllegalArgumentException("La obra no está en tus favoritos"));

		TipoObra tipo = favorito.getTipo();

		favoritoRepository.delete(favorito);
		favoritoRepository.flush();

		List<Favorito> restantes = favoritoRepository.findByUsuarioIdAndTipoOrderByPosicionAsc(usuario.getId(), tipo);

		int posicion = 1;
		for (Favorito f : restantes) {
			f.setPosicion(posicion++);
		}

		favoritoRepository.saveAll(restantes);
	}

	// Sustituir un favorito por otra obra del mismo tipo,
	// conservando la posición.
	@Transactional
	public FavoritoResponse sustituir(String email, Long obraAntiguaId, Long obraNuevaId) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		Favorito favorito = favoritoRepository.findByUsuarioIdAndObraId(usuario.getId(), obraAntiguaId)
				.orElseThrow(() -> new IllegalArgumentException("La obra a sustituir no está en tus favoritos"));

		if (obraAntiguaId.equals(obraNuevaId)) {
			throw new IllegalArgumentException("Debes elegir una obra distinta");
		}

		Obra nueva = obraRepository.findById(obraNuevaId)
				.orElseThrow(() -> new IllegalArgumentException("La obra nueva no existe"));

		if (nueva.getTipo() != favorito.getTipo()) {
			throw new IllegalArgumentException("La obra nueva debe ser del mismo tipo");
		}

		if (favoritoRepository.findByUsuarioIdAndObraId(usuario.getId(), obraNuevaId).isPresent()) {
			throw new IllegalArgumentException("La obra nueva ya está en tus favoritos");
		}

		favorito.setObraId(obraNuevaId);
		favoritoRepository.save(favorito);

		return convertir(List.of(favorito)).get(0);
	}

	// Cambiar el orden de los favoritos de una categoría.
	// "obraIds" debe contener exactamente los favoritos actuales
	// de esa categoría, en el nuevo orden.
	@Transactional
	public List<FavoritoResponse> reordenar(String email, TipoObra tipo, List<Long> obraIds) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		List<Favorito> actuales = favoritoRepository.findByUsuarioIdAndTipoOrderByPosicionAsc(usuario.getId(), tipo);

		if (obraIds == null || obraIds.size() != actuales.size()) {
			throw new IllegalArgumentException("La lista de obras no coincide con tus favoritos");
		}

		Set<Long> nuevos = new HashSet<>(obraIds);

		Set<Long> existentes = actuales.stream().map(Favorito::getObraId).collect(Collectors.toSet());

		if (nuevos.size() != obraIds.size() || !nuevos.equals(existentes)) {
			throw new IllegalArgumentException("La lista de obras no coincide con tus favoritos");
		}

		Map<Long, Favorito> porObra = actuales.stream().collect(Collectors.toMap(Favorito::getObraId, f -> f));

		int posicion = 1;
		for (Long obraId : obraIds) {
			porObra.get(obraId).setPosicion(posicion++);
		}

		favoritoRepository.saveAll(actuales);

		return convertir(favoritoRepository.findByUsuarioIdAndTipoOrderByPosicionAsc(usuario.getId(), tipo));
	}

	// ---------- Métodos auxiliares ----------

	// Convierte favoritos en DTOs añadiendo título y tipo de la obra.
	private List<FavoritoResponse> convertir(List<Favorito> favoritos) {

		List<Long> ids = favoritos.stream().map(Favorito::getObraId).collect(Collectors.toList());

		Map<Long, Obra> obras = obraRepository.findAllById(ids).stream().collect(Collectors.toMap(Obra::getId, o -> o));

		List<FavoritoResponse> respuesta = new ArrayList<>();

		for (Favorito f : favoritos) {
			Obra obra = obras.get(f.getObraId());

			if (obra == null) {
				continue;
			}

			respuesta.add(new FavoritoResponse(obra.getId(), obra.getTitulo(), f.getTipo().name(), f.getPosicion()));
		}

		return respuesta;
	}
}