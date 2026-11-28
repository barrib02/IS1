package com.example.demo.dto;

public class SocialDTOs {

	// Lo que ve quien visita el perfil de otro usuario.
	// privacidadPerfil solo se rellena si puedeVerTodo es true.
	// relacion: PROPIO, SIGUE, SOLICITUD_ENVIADA o NO_SIGUE.
	public record Vista(Long id, String nombre, String biografia, String fotoPerfilUrl, String bannerUrl,
			String privacidadPerfil, boolean requiereSolicitud, long numeroSeguidores, long numeroSeguidos,
			boolean puedeVerTodo, String relacion) {
	}

	// Solicitud de seguimiento pendiente que he recibido.
	public record Solicitud(Long usuarioId, String nombre, String fotoPerfilUrl, boolean loSigo) {
	}
}
