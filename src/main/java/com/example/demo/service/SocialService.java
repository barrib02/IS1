package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.SocialDTOs;
import com.example.demo.dto.UsuarioBusquedaDTO;
import com.example.demo.entity.Notificacion;
import com.example.demo.entity.PrivacidadPerfil;
import com.example.demo.entity.Seguimiento;
import com.example.demo.entity.SolicitudSeguimiento;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.NotificacionSeguimientoRepository;
import com.example.demo.repository.SeguimientoConsultasRepository;
import com.example.demo.repository.SolicitudSeguimientoRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class SocialService {

	private static final String TIPO_SOLICITUD = "SOLICITUD_SEGUIMIENTO";

	private final UsuarioRepository usuarios;
	private final SeguimientoConsultasRepository seguimientos;
	private final SolicitudSeguimientoRepository solicitudes;
	private final NotificacionSeguimientoRepository notificaciones;

	public SocialService(UsuarioRepository usuarios, SeguimientoConsultasRepository seguimientos,
			SolicitudSeguimientoRepository solicitudes, NotificacionSeguimientoRepository notificaciones) {
		this.usuarios = usuarios;
		this.seguimientos = seguimientos;
		this.solicitudes = solicitudes;
		this.notificaciones = notificaciones;
	}

	private Usuario porEmail(String email) {
		return usuarios.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
	}

	private Usuario porId(Long id) {
		return usuarios.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
	}

	private boolean esPropio(Usuario yo, Usuario otro) {
		return yo.getId().equals(otro.getId());
	}

	// Regla de visibilidad: perfil propio, perfil público, o "solo amigos" que ya sigo (aceptado).
	private boolean puedeVerTodo(Usuario yo, Usuario objetivo) {
		if (esPropio(yo, objetivo)) {
			return true;
		}
		switch (objetivo.getPrivacidadPerfil()) {
		case PUBLICO:
			return true;
		case SOLO_AMIGOS:
			return seguimientos.existsBySeguidorIdAndSeguidoId(yo.getId(), objetivo.getId());
		default:
			return false;
		}
	}

	private void exigirVerTodo(Usuario yo, Usuario objetivo) {
		if (!puedeVerTodo(yo, objetivo)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta información");
		}
	}

	private UsuarioBusquedaDTO aDto(Usuario u) {
		return new UsuarioBusquedaDTO(u.getId(), u.getNombre(), u.getFotoPerfilUrl(), u.getPrivacidadPerfil());
	}

	// ---------- Ver el perfil de otro usuario ----------

	public SocialDTOs.Vista vista(String emailSolicitante, Long id) {
		Usuario yo = porEmail(emailSolicitante);
		Usuario objetivo = porId(id);

		boolean propio = esPropio(yo, objetivo);

		if (!propio && objetivo.getPrivacidadPerfil() == PrivacidadPerfil.PRIVADO) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este perfil es privado");
		}

		boolean todo = puedeVerTodo(yo, objetivo);

		String relacion;
		if (propio) {
			relacion = "PROPIO";
		} else if (seguimientos.existsBySeguidorIdAndSeguidoId(yo.getId(), objetivo.getId())) {
			relacion = "SIGUE";
		} else if (solicitudes.existsBySolicitanteIdAndDestinatarioId(yo.getId(), objetivo.getId())) {
			relacion = "SOLICITUD_ENVIADA";
		} else {
			relacion = "NO_SIGUE";
		}

		return new SocialDTOs.Vista(objetivo.getId(), objetivo.getNombre(), objetivo.getBiografia(),
				objetivo.getFotoPerfilUrl(), objetivo.getBannerUrl(),
				todo ? objetivo.getPrivacidadPerfil().name() : null,
				objetivo.getPrivacidadPerfil() == PrivacidadPerfil.SOLO_AMIGOS,
				seguimientos.countBySeguidoId(objetivo.getId()), seguimientos.countBySeguidorId(objetivo.getId()), todo,
				relacion);
	}

	public List<UsuarioBusquedaDTO> seguidores(String emailSolicitante, Long id) {
		Usuario yo = porEmail(emailSolicitante);
		Usuario objetivo = porId(id);
		exigirVerTodo(yo, objetivo);
		return seguimientos.findBySeguidoId(id).stream().map(Seguimiento::getSeguidor).map(this::aDto).toList();
	}

	public List<UsuarioBusquedaDTO> seguidos(String emailSolicitante, Long id) {
		Usuario yo = porEmail(emailSolicitante);
		Usuario objetivo = porId(id);
		exigirVerTodo(yo, objetivo);
		return seguimientos.findBySeguidorId(id).stream().map(Seguimiento::getSeguido).map(this::aDto).toList();
	}

	// ---------- Seguir / dejar de seguir ----------

	// Devuelve "SIGUE" (cuenta pública) o "SOLICITUD_ENVIADA" (solo amigos).
	@Transactional
	public String seguir(String email, Long idObjetivo) {
		Usuario yo = porEmail(email);
		Usuario objetivo = porId(idObjetivo);

		if (esPropio(yo, objetivo)) {
			throw new IllegalArgumentException("No puedes seguirte a ti mismo");
		}
		if (seguimientos.existsBySeguidorIdAndSeguidoId(yo.getId(), objetivo.getId())) {
			return "SIGUE";
		}

		switch (objetivo.getPrivacidadPerfil()) {

		case PUBLICO:
			seguimientos.save(new Seguimiento(yo, objetivo));
			return "SIGUE";

		case SOLO_AMIGOS:
			if (solicitudes.existsBySolicitanteIdAndDestinatarioId(yo.getId(), objetivo.getId())) {
				return "SOLICITUD_ENVIADA";
			}
			solicitudes.save(new SolicitudSeguimiento(yo.getId(), objetivo.getId()));

			Notificacion aviso = new Notificacion(objetivo.getId(), null, TIPO_SOLICITUD,
					yo.getNombre() + " quiere seguirte");
			aviso.setEmisorId(yo.getId());
			notificaciones.save(aviso);
			return "SOLICITUD_ENVIADA";

		default:
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este perfil es privado");
		}
	}

	// Deja de seguir o cancela una solicitud pendiente.
	@Transactional
	public String dejarDeSeguir(String email, Long idObjetivo) {
		Usuario yo = porEmail(email);

		seguimientos.findBySeguidorIdAndSeguidoId(yo.getId(), idObjetivo).ifPresent(seguimientos::delete);
		solicitudes.findBySolicitanteIdAndDestinatarioId(yo.getId(), idObjetivo).ifPresent(solicitudes::delete);
		limpiarAvisos(idObjetivo, yo.getId());

		return "NO_SIGUE";
	}

	// ---------- Solicitudes recibidas ----------

	public List<SocialDTOs.Solicitud> solicitudesRecibidas(String email) {
		Usuario yo = porEmail(email);

		return solicitudes.findByDestinatarioIdOrderByFechaDesc(yo.getId()).stream().map(s -> {
			Usuario u = porId(s.getSolicitanteId());
			boolean loSigo = seguimientos.existsBySeguidorIdAndSeguidoId(yo.getId(), u.getId());
			return new SocialDTOs.Solicitud(u.getId(), u.getNombre(), u.getFotoPerfilUrl(), loSigo);
		}).toList();
	}

	@Transactional
	public void aceptar(String email, Long idSolicitante) {
		Usuario yo = porEmail(email);

		SolicitudSeguimiento solicitud = solicitudes.findBySolicitanteIdAndDestinatarioId(idSolicitante, yo.getId())
				.orElseThrow(() -> new IllegalArgumentException("No hay ninguna solicitud de ese usuario"));

		Usuario solicitante = porId(idSolicitante);

		if (!seguimientos.existsBySeguidorIdAndSeguidoId(idSolicitante, yo.getId())) {
			seguimientos.save(new Seguimiento(solicitante, yo));
		}

		solicitudes.delete(solicitud);
		limpiarAvisos(yo.getId(), idSolicitante);
	}

	@Transactional
	public void rechazar(String email, Long idSolicitante) {
		Usuario yo = porEmail(email);

		solicitudes.findBySolicitanteIdAndDestinatarioId(idSolicitante, yo.getId()).ifPresent(solicitudes::delete);
		limpiarAvisos(yo.getId(), idSolicitante);
	}

	// Borra las notificaciones de solicitud que "emisor" envió a "destinatario".
	private void limpiarAvisos(Long destinatarioId, Long emisorId) {
		notificaciones.deleteAll(notificaciones.findByUsuarioIdAndEmisorIdAndTipo(destinatarioId, emisorId,
				TIPO_SOLICITUD));
	}
}
