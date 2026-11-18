package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.dto.EstanteResponse;
import com.example.demo.dto.SearchResultDTO;
import com.example.demo.entity.GeneroObra;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;

@Service
public class EstanteService {

	// Obras por estante.
	private static final int POR_ESTANTE = 10;

	// Estante por género: título creativo + géneros que lo forman.
	private record PorGenero(String titulo, String subtitulo, List<GeneroObra> generos) {}

	// Estante por tipo (solo se usa en el Discover general).
	private record PorTipo(TipoObra tipo, String titulo, String subtitulo) {}

	private static final List<PorTipo> TIPOS = List.of(
			new PorTipo(TipoObra.LIBRO, "Libros para perderse", "Novedades para devorar entre páginas"),
			new PorTipo(TipoObra.PELICULA, "Noches de cine", "Películas para poner en la pantalla grande"),
			new PorTipo(TipoObra.SERIE, "Maratones de fin de semana", "Series para decir \"un capítulo más\""),
			new PorTipo(TipoObra.VIDEOJUEGO, "Una partida más", "Videojuegos para no soltar el mando"));

	private static final List<PorGenero> GENEROS = List.of(
			new PorGenero("Para no dormir", "Terror y suspense para dejar la luz encendida",
					List.of(GeneroObra.TERROR, GeneroObra.THRILLER)),
			new PorGenero("Para ver en familia", "Planes para todas las edades",
					List.of(GeneroObra.INFANTIL, GeneroObra.ANIMACION, GeneroObra.JUVENIL)),
			new PorGenero("Mundos que no existen", "Fantasía y ciencia ficción para escapar de la realidad",
					List.of(GeneroObra.FANTASIA, GeneroObra.CIENCIA_FICCION)),
			new PorGenero("Para llorar a gusto", "Dramas y romances que te dejan el corazón blandito",
					List.of(GeneroObra.DRAMA, GeneroObra.ROMANCE)),
			new PorGenero("¿Quién fue el culpable?", "Misterio y crimen para los que no pueden parar",
					List.of(GeneroObra.MISTERIO, GeneroObra.CRIMEN)),
			new PorGenero("Descarga de adrenalina", "Acción y aventura sin un respiro",
					List.of(GeneroObra.ACCION, GeneroObra.AVENTURA)),
			new PorGenero("Risas aseguradas", "Comedias para desconectar", List.of(GeneroObra.COMEDIA)),
			new PorGenero("Para jugar con amigos", "Multijugador, deportes y carreras",
					List.of(GeneroObra.MULTIJUGADOR, GeneroObra.DEPORTES, GeneroObra.CARRERAS)),
			new PorGenero("Basado en hechos reales", "Documentales y relatos históricos",
					List.of(GeneroObra.DOCUMENTAL, GeneroObra.HISTORICO)));

	private final ObraRepository obraRepository;

	public EstanteService(ObraRepository obraRepository) {
		this.obraRepository = obraRepository;
	}

	// tipo == null  -> Discover general (un estante por tipo + estantes por género).
	// tipo != null  -> solo ese tipo ("Recién llegados" + estantes por género de ese tipo).
	public List<EstanteResponse> obtenerEstantes(TipoObra tipo) {

		List<EstanteResponse> estantes = new ArrayList<>();
		Pageable masNuevas = PageRequest.of(0, POR_ESTANTE, Sort.by(Sort.Direction.DESC, "id"));
		Pageable sinOrden = PageRequest.of(0, POR_ESTANTE); // las consultas @Query ya ordenan solas

		if (tipo == null) {
			for (PorTipo p : TIPOS) {
				anadir(estantes, p.titulo(), p.subtitulo(), obraRepository.findByTipo(p.tipo(), masNuevas));
			}
		} else {
			anadir(estantes, "Recién llegados", "Lo último que se ha añadido", obraRepository.findByTipo(tipo, masNuevas));
		}

		for (PorGenero p : GENEROS) {
			List<Obra> obras = (tipo == null)
					? obraRepository.buscarPorGeneros(p.generos(), sinOrden)
					: obraRepository.buscarPorGenerosYTipo(p.generos(), tipo, sinOrden);
			anadir(estantes, p.titulo(), p.subtitulo(), obras);
		}

		return estantes;
	}

	// Añade el estante solo si tiene obras (así no salen filas vacías).
	private void anadir(List<EstanteResponse> estantes, String titulo, String subtitulo, List<Obra> obras) {
		if (obras.isEmpty()) {
			return;
		}
		List<SearchResultDTO> dtos = obras.stream()
				.map(o -> new SearchResultDTO(o.getId(), o.getTitulo(), o.getTipo() == null ? "" : o.getTipo().name()))
				.toList();
		estantes.add(new EstanteResponse(titulo, subtitulo, dtos));
	}
}