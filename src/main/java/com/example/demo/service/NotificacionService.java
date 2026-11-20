package com.example.demo.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Notificacion;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.NotificacionRepository;

@Service
public class NotificacionService {

	// Longitud máxima del mensaje (coincide con la columna de la base de datos).
	private static final int MAX_MENSAJE = 500;

	private final NotificacionRepository notificacionRepository;
	private final PerfilService perfilService;

	public NotificacionService(NotificacionRepository notificacionRepository, PerfilService perfilService) {
		this.notificacionRepository = notificacionRepository;
		this.perfilService = perfilService;
	}

	// Crea una notificación para un usuario (lo usa ObraService al validar o rechazar).
	@Transactional
	public Notificacion crear(Long usuarioId, Long obraId, String tipo, String mensaje) {

		// Si el mensaje es demasiado largo (justificaciones extensas) lo recortamos.
		String texto = mensaje.length() > MAX_MENSAJE ? mensaje.substring(0, MAX_MENSAJE - 1) + "…" : mensaje;

		return notificacionRepository.save(new Notificacion(usuarioId, obraId, tipo, texto));
	}

	// Mis notificaciones, de la más reciente a la más antigua.
	@Transactional(readOnly = true)
	public List<Notificacion> misNotificaciones(String email) {
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);
		return notificacionRepository.findTop50ByUsuarioIdOrderByFechaDesc(usuario.getId());
	}

	// Número de notificaciones sin leer.
	@Transactional(readOnly = true)
	public long contarNoLeidas(String email) {
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);
		return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId());
	}

	// Marcar UNA notificación como leída (solo si es mía).
	@Transactional
	public Notificacion marcarLeida(String email, Long id) {
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		Notificacion notificacion = notificacionRepository.findByIdAndUsuarioId(id, usuario.getId())
				.orElseThrow(() -> new NoSuchElementException("Notificación no encontrada"));

		notificacion.setLeida(true);
		return notificacionRepository.save(notificacion);
	}

	// Marcar TODAS mis notificaciones como leídas. Devuelve cuántas cambiaron.
	@Transactional
	public int marcarTodasLeidas(String email) {
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);
		return notificacionRepository.marcarTodasLeidas(usuario.getId());
	}
}
