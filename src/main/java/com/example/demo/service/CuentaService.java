package com.example.demo.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Lista;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.*;

@Service
public class CuentaService {

	private final PerfilService perfilService;
	private final UsuarioRepository usuarioRepository;
	private final BCryptPasswordEncoder encoder;
	private final FavoritoRepository favoritoRepository;
	private final ListaRepository listaRepository;
	private final ListaObraRepository listaObraRepository;
	private final ValoracionRepository valoracionRepository;
	private final ResenaRepository resenaRepository;
	private final ActividadRepository actividadRepository;
	private final EstadoUsuarioObraRepository estadoRepository;
	private final NotificacionRepository notificacionRepository;
	private final ObraRepository obraRepository;

	public CuentaService(PerfilService perfilService, UsuarioRepository usuarioRepository,
			BCryptPasswordEncoder encoder, FavoritoRepository favoritoRepository, ListaRepository listaRepository,
			ListaObraRepository listaObraRepository, ValoracionRepository valoracionRepository,
			ResenaRepository resenaRepository, ActividadRepository actividadRepository,
			EstadoUsuarioObraRepository estadoRepository, NotificacionRepository notificacionRepository,
			ObraRepository obraRepository) {
		this.perfilService = perfilService;
		this.usuarioRepository = usuarioRepository;
		this.encoder = encoder;
		this.favoritoRepository = favoritoRepository;
		this.listaRepository = listaRepository;
		this.listaObraRepository = listaObraRepository;
		this.valoracionRepository = valoracionRepository;
		this.resenaRepository = resenaRepository;
		this.actividadRepository = actividadRepository;
		this.estadoRepository = estadoRepository;
		this.notificacionRepository = notificacionRepository;
		this.obraRepository = obraRepository;
	}

	// Borra la cuenta y todos sus datos. Exige la contraseña actual.
	@Transactional
	public void eliminar(String email, String password) {

		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		if (password == null || !encoder.matches(password, usuario.getPassword())) {
			throw new IllegalArgumentException("Contraseña incorrecta");
		}

		Long id = usuario.getId();

		List<Lista> listas = listaRepository.findByUsuarioId(id);
		for (Lista l : listas) {
			listaObraRepository.deleteByListaId(l.getId());
		}
		listaRepository.deleteAll(listas);

		favoritoRepository.deleteByUsuarioId(id);
		valoracionRepository.deleteByUsuarioId(id);
		resenaRepository.deleteByUsuarioId(id);
		actividadRepository.deleteByUsuarioId(id);
		estadoRepository.deleteByUsuarioId(id);
		notificacionRepository.deleteByUsuarioId(id);
		obraRepository.desvincularAutor(id);

		usuarioRepository.delete(usuario);
	}
}