package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.PerfilResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;

@Service
public class PerfilService {

	private static final int MAX_BIOGRAFIA = 500;

	private final UsuarioRepository usuarioRepository;

	public PerfilService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	// Devuelve la entidad Usuario a partir del email de la sesión.
	// Lo reutilizaremos en los servicios de favoritos y listas.
	public Usuario obtenerUsuarioPorEmail(String email) {
		return usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
	}

	// Perfil del usuario que ha iniciado sesión.
	public PerfilResponse obtenerMiPerfil(String email) {
		return convertir(obtenerUsuarioPorEmail(email));
	}

	// Perfil de cualquier usuario por su id (solo datos públicos).
	public PerfilResponse obtenerPerfilPorId(Long id) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		return convertir(usuario);
	}

	// Crea o edita la biografía del usuario autenticado.
	@Transactional
	public PerfilResponse actualizarBiografia(String email, String biografia) {

		// Si llega null, la biografía queda vacía.
		String texto = (biografia == null) ? "" : biografia.trim();

		if (texto.length() > MAX_BIOGRAFIA) {
			throw new IllegalArgumentException("La biografía no puede superar los " + MAX_BIOGRAFIA + " caracteres");
		}

		Usuario usuario = obtenerUsuarioPorEmail(email);
		usuario.setBiografia(texto);
		usuarioRepository.save(usuario);

		return convertir(usuario);
	}

	// Convierte la entidad en el DTO que se envía al navegador.
	private PerfilResponse convertir(Usuario usuario) {
		return new PerfilResponse(usuario.getId(), usuario.getNombre(), usuario.getBiografia());
	}
}