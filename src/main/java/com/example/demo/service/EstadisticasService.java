package com.example.demo.service;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.EstadisticasResponse;
import com.example.demo.dto.GeneroConteo;
import com.example.demo.dto.MesConteo;
import com.example.demo.dto.CompletadaResponse;
import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.GeneroObra;
import com.example.demo.entity.PeriodoEstadistica;
import com.example.demo.entity.TipoObra;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.EstadoUsuarioObra;
import com.example.demo.entity.Obra;
import com.example.demo.repository.EstadoUsuarioObraRepository;

@Service
public class EstadisticasService {

	private static final int MAX_GENEROS = 5;

	private final EstadoUsuarioObraRepository repository;
	private final PerfilService perfilService;

	public EstadisticasService(EstadoUsuarioObraRepository repository, PerfilService perfilService) {
		this.repository = repository;
		this.perfilService = perfilService;
	}

	@Transactional(readOnly = true)
	public EstadisticasResponse obtener(String email, PeriodoEstadistica periodo) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);
		boolean todo = periodo.esTodo();

		Map<TipoObra, Long> porTipo = new EnumMap<>(TipoObra.class);
		for (Object[] fila : repository.contarPorTipo(usuario.getId(), EstadoObra.COMPLETADA, todo, periodo.desde())) {
			if (fila[0] != null) { // obras sin tipo no cuentan
				porTipo.put((TipoObra) fila[0], (Long) fila[1]);
			}
		}

		List<GeneroConteo> generos = repository
				.contarGeneros(usuario.getId(), EstadoObra.COMPLETADA, todo, periodo.desde()).stream()
				.limit(MAX_GENEROS).map(f -> new GeneroConteo(((GeneroObra) f[0]).name(), (Long) f[1])).toList();

		return new EstadisticasResponse(periodo.name(), porTipo.getOrDefault(TipoObra.LIBRO, 0L),
				porTipo.getOrDefault(TipoObra.PELICULA, 0L), porTipo.getOrDefault(TipoObra.SERIE, 0L),
				porTipo.getOrDefault(TipoObra.VIDEOJUEGO, 0L), generos);
	}

	// Lista de obras completadas (de un tipo, o de todos si tipo == null) en un
	// periodo.
	@Transactional(readOnly = true)
	public List<CompletadaResponse> completadas(String email, TipoObra tipo, PeriodoEstadistica periodo) {
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);
		return listar(usuario.getId(), tipo, periodo.desde(), LocalDateTime.now().plusDays(1));
	}

	// Obras completadas durante un mes concreto (para el calendario del perfil).
	@Transactional(readOnly = true)
	public List<CompletadaResponse> calendario(String email, int anio, int mes) {
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		YearMonth ym;
		try {
			ym = YearMonth.of(anio, mes);
		} catch (DateTimeException e) {
			throw new IllegalArgumentException("Mes o año no válido");
		}

		return listar(usuario.getId(), null, ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
	}

	private List<CompletadaResponse> listar(Long usuarioId, TipoObra tipo, LocalDateTime desde, LocalDateTime hasta) {

		return repository.completadasEntre(usuarioId, EstadoObra.COMPLETADA, desde, hasta).stream()
				.filter(f -> tipo == null || ((Obra) f[1]).getTipo() == tipo).map(f -> {
					EstadoUsuarioObra e = (EstadoUsuarioObra) f[0];
					Obra o = (Obra) f[1];
					return new CompletadaResponse(o.getId(), o.getTitulo(),
							o.getTipo() == null ? "" : o.getTipo().name(), e.getFechaCompletada().toString());
				}).toList();
	}

	// Obras completadas en cada uno de los últimos 12 meses (incluido el actual),
	// por tipo.
	@Transactional(readOnly = true)
	public List<MesConteo> ultimosMeses(String email) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		YearMonth actual = YearMonth.now();
		YearMonth primero = actual.minusMonths(11);

		// Los 12 meses, de más antiguo a más reciente. long[] = {libros, películas,
		// series, videojuegos}.
		Map<YearMonth, long[]> acumulado = new LinkedHashMap<>();
		for (int i = 0; i < 12; i++) {
			acumulado.put(primero.plusMonths(i), new long[4]);
		}

		for (Object[] fila : repository.completadasEntre(usuario.getId(), EstadoObra.COMPLETADA,
				primero.atDay(1).atStartOfDay(), actual.plusMonths(1).atDay(1).atStartOfDay())) {

			EstadoUsuarioObra e = (EstadoUsuarioObra) fila[0];
			Obra o = (Obra) fila[1];

			long[] cuenta = acumulado.get(YearMonth.from(e.getFechaCompletada()));
			if (cuenta == null || o.getTipo() == null) {
				continue;
			}

			switch (o.getTipo()) {
			case LIBRO -> cuenta[0]++;
			case PELICULA -> cuenta[1]++;
			case SERIE -> cuenta[2]++;
			case VIDEOJUEGO -> cuenta[3]++;
			}
		}

		return acumulado.entrySet().stream().map(en -> new MesConteo(en.getKey().toString(), en.getValue()[0],
				en.getValue()[1], en.getValue()[2], en.getValue()[3])).toList();
	}
}
