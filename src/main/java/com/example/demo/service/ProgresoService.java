package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ProgresoRequest;
import com.example.demo.dto.ProgresoResponse;
import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.EstadoUsuarioObra;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.EstadoUsuarioObraRepository;
import com.example.demo.repository.ObraRepository;

@Service
public class ProgresoService {

	private static final int MAX_VALOR = 100000;

	private final EstadoUsuarioObraRepository estadoRepository;
	private final ObraRepository obraRepository;
	private final PerfilService perfilService;
	private final ActividadService actividadService;

	public ProgresoService(EstadoUsuarioObraRepository estadoRepository, ObraRepository obraRepository,
			PerfilService perfilService, ActividadService actividadService) {
		this.estadoRepository = estadoRepository;
		this.obraRepository = obraRepository;
		this.perfilService = perfilService;
		this.actividadService = actividadService;
	}

	// Guarda el progreso. Libro: páginas. Serie: temporada y capítulo.
	@Transactional
	public ProgresoResponse guardar(String email, Long obraId, ProgresoRequest request) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);
		Obra obra = buscarObra(obraId);

		EstadoUsuarioObra registro = estadoRepository.findByUsuarioIdAndObraId(usuario.getId(), obraId)
				.orElseGet(() -> new EstadoUsuarioObra(usuario.getId(), obraId, EstadoObra.EN_CURSO));

		String descripcion;
		boolean hayCambio;

		if (obra.getTipo() == TipoObra.LIBRO) {
			Integer paginas = validar(request.getPaginasLeidas(), 0, "Las páginas leídas");
			hayCambio = !paginas.equals(registro.getPaginasLeidas());
			registro.setPaginasLeidas(paginas);
			descripcion = "Ha leído " + paginas + " páginas de \"" + obra.getTitulo() + "\"";

		} else if (obra.getTipo() == TipoObra.SERIE) {
			Integer temporada = validar(request.getTemporadaActual(), 1, "La temporada");
			Integer capitulo = validar(request.getCapituloActual(), 1, "El capítulo");
			hayCambio = !temporada.equals(registro.getTemporadaActual())
					|| !capitulo.equals(registro.getCapituloActual());
			registro.setTemporadaActual(temporada);
			registro.setCapituloActual(capitulo);
			descripcion = "Ha llegado a la temporada " + temporada + ", capítulo " + capitulo + " de \""
					+ obra.getTitulo() + "\"";

		} else {
			throw new IllegalArgumentException("Este tipo de obra no admite progreso");
		}

		if (registro.getEstado() == null || registro.getEstado() == EstadoObra.PENDIENTE
				|| registro.getEstado() == EstadoObra.ABANDONADA) {
			registro.setEstado(EstadoObra.EN_CURSO);
			registro.setFechaCompletada(null);
		}
		registro.setFechaActualizacion(LocalDateTime.now());

		EstadoUsuarioObra guardado = estadoRepository.save(registro);

		if (hayCambio) {
			actividadService.registrarActividad(usuario.getId(), obraId, "PROGRESO", descripcion);
		}

		return convertir(guardado, obra);
	}

	// Progreso de una obra concreta (campos a null si todavía no hay).
	@Transactional(readOnly = true)
	public ProgresoResponse obtener(String email, Long obraId) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);
		Obra obra = buscarObra(obraId);

		EstadoUsuarioObra registro = estadoRepository.findByUsuarioIdAndObraId(usuario.getId(), obraId).orElse(null);

		return convertir(registro, obra);
	}

	// Obras EN_CURSO con progreso registrado (para el perfil).
	@Transactional(readOnly = true)
	public List<ProgresoResponse> enCurso(String email) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		List<EstadoUsuarioObra> registros = estadoRepository
				.findByUsuarioIdAndEstadoOrderByFechaActualizacionDesc(usuario.getId(), EstadoObra.EN_CURSO).stream()
				.filter(r -> r.getPaginasLeidas() != null || r.getTemporadaActual() != null)
				.collect(Collectors.toList());

		Map<Long, Obra> obras = obraRepository
				.findAllById(registros.stream().map(EstadoUsuarioObra::getObraId).collect(Collectors.toList())).stream()
				.collect(Collectors.toMap(Obra::getId, o -> o));

		return registros.stream().filter(r -> obras.containsKey(r.getObraId()))
				.map(r -> convertir(r, obras.get(r.getObraId()))).collect(Collectors.toList());
	}

	// ---------- auxiliares ----------

	private Obra buscarObra(Long id) {
		return obraRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("La obra no existe"));
	}

	private Integer validar(Integer valor, int minimo, String nombre) {
		if (valor == null) {
			throw new IllegalArgumentException(nombre + " es obligatorio");
		}
		if (valor < minimo || valor > MAX_VALOR) {
			throw new IllegalArgumentException(nombre + " debe estar entre " + minimo + " y " + MAX_VALOR);
		}
		return valor;
	}
	
	private ProgresoResponse convertir(EstadoUsuarioObra r, Obra obra) {
		return new ProgresoResponse(obra.getId(), obra.getTitulo(),
				obra.getTipo() == null ? "" : obra.getTipo().name(),
				r == null || r.getEstado() == null ? null : r.getEstado().name(),
				r == null ? null : r.getPaginasLeidas(),
				r == null ? null : r.getTemporadaActual(),
				r == null ? null : r.getCapituloActual(),
				r == null || r.getFechaCompletada() == null ? null : r.getFechaCompletada().toLocalDate().toString());
	}
}