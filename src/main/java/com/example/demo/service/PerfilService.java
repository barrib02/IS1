package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.dto.PerfilResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.entity.PrivacidadPerfil;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import com.example.demo.dto.UsuarioBusquedaDTO;

@Service
public class PerfilService {

	private static final int MAX_BIOGRAFIA = 500;

	private final UsuarioRepository usuarioRepository;
	private final CloudinaryService cloudinaryService;

	public PerfilService(UsuarioRepository usuarioRepository, CloudinaryService cloudinaryService) {
		this.usuarioRepository = usuarioRepository;
		this.cloudinaryService = cloudinaryService;
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

	// Consultar un perfil por su identificador.
	public PerfilResponse obtenerPerfilPorId(Long id) {
		return obtenerPerfilPorId(id, null);
	}

	// H5.3 - Consultar un perfil respetando su privacidad.
	public PerfilResponse obtenerPerfilPorId(Long id, String emailSolicitante) {

		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		boolean esPropio = emailSolicitante != null && emailSolicitante.equalsIgnoreCase(usuario.getEmail());

		if (!esPropio) {

			switch (usuario.getPrivacidadPerfil()) {

			case PUBLICO:
				break;

			case PRIVADO:
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este perfil es privado");

			case SOLO_AMIGOS:
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este perfil solo es visible para amigos");
			}
		}

		return convertir(usuario);
	}

	@Transactional
	public PerfilResponse actualizarPrivacidad(String email, boolean publico) {
		Usuario usuario = obtenerUsuarioPorEmail(email);
		usuario.setPerfilPublico(publico);
		usuarioRepository.save(usuario);
		return convertir(usuario);
	}

	@Transactional
	public PerfilResponse actualizarPrivacidad(String email, PrivacidadPerfil privacidad) {

		if (privacidad == null) {
			throw new IllegalArgumentException("La privacidad del perfil es obligatoria");
		}

		Usuario usuario = obtenerUsuarioPorEmail(email);

		usuario.setPrivacidadPerfil(privacidad);

		usuarioRepository.save(usuario);

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
		PerfilResponse respuesta = new PerfilResponse(usuario.getId(), usuario.getNombre(), usuario.getBiografia(),
				usuario.getFotoPerfilUrl(), usuario.getBannerUrl());
		respuesta.setRol(usuario.getRol() == null ? "USUARIO" : usuario.getRol().name());
		respuesta.setPrivacidadPerfil(usuario.getPrivacidadPerfil());
		return respuesta;
	}

	// H4.2 - Subir o cambiar la foto de perfil
	@Transactional
	public String actualizarFotoPerfil(String email, MultipartFile archivo) throws IOException {

		Usuario usuario = obtenerUsuarioPorEmail(email);

		String url = cloudinaryService.subirImagen(archivo, "goodreads-plus/perfiles");

		usuario.setFotoPerfilUrl(url);
		usuarioRepository.save(usuario);

		return url;
	}

	// H4.4 - Subir o cambiar el banner
	@Transactional
	public String actualizarBanner(String email, MultipartFile archivo) throws IOException {

		Usuario usuario = obtenerUsuarioPorEmail(email);

		String url = cloudinaryService.subirImagen(archivo, "goodreads-plus/banners");

		usuario.setBannerUrl(url);
		usuarioRepository.save(usuario);

		return url;
	}

	public boolean esModerador(String email) {

		Usuario usuario = obtenerUsuarioPorEmail(email);

		return usuario.getRol() == com.example.demo.entity.RolUsuario.MODERADOR;
	}

	// Cambia el nombre del usuario autenticado.
	@Transactional
	public PerfilResponse actualizarNombre(String email, String nombre) {

		String limpio = (nombre == null) ? "" : nombre.trim();

		if (limpio.isEmpty()) {
			throw new IllegalArgumentException("El nombre es obligatorio");
		}
		if (limpio.length() > 100) {
			throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres");
		}

		Usuario usuario = obtenerUsuarioPorEmail(email);
		usuario.setNombre(limpio);
		usuarioRepository.save(usuario);

		return convertir(usuario);
	}
	
	// Buscar usuarios por nombre (públicos y solo amigos).
	public List<UsuarioBusquedaDTO> buscarUsuarios(String texto, String emailActual) {

		String limpio = (texto == null) ? "" : texto.trim();

		if (limpio.length() < 2) {
			return List.of();
		}

		return usuarioRepository.buscarPorNombre(limpio, emailActual,
				List.of(PrivacidadPerfil.PUBLICO, PrivacidadPerfil.SOLO_AMIGOS), PageRequest.of(0, 20)).stream()
				.map(u -> new UsuarioBusquedaDTO(u.getId(), u.getNombre(), u.getFotoPerfilUrl(),
						u.getPrivacidadPerfil()))
				.toList();
	}

}