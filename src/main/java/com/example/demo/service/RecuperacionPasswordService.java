package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;

@Service
public class RecuperacionPasswordService {

	private static final int MINUTOS_CADUCIDAD = 30;
	private static final int MINUTOS_ENTRE_SOLICITUDES = 2;

	private final UsuarioRepository usuarioRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final JavaMailSender mailSender;
	private final String baseUrl;
	private final String remitente;

	private final SecureRandom random = new SecureRandom();

	public RecuperacionPasswordService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder,
			JavaMailSender mailSender, @Value("${app.verification.base-url}") String baseUrl,
			@Value("${spring.mail.username}") String remitente) {

		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.mailSender = mailSender;
		this.baseUrl = baseUrl;
		this.remitente = remitente;
	}

	// H6.3 - Solicitar la recuperación de contraseña.
	@Transactional
	public void solicitarRecuperacion(String email) {

		if (email == null || email.isBlank()) {
			return;
		}

		Usuario usuario = usuarioRepository.findByEmailAndEmailVerificadoTrue(email.trim()).orElse(null);

		// No revelamos si el correo está registrado.
		if (usuario == null) {
			return;
		}
		// Evitamos solicitudes consecutivas de recuperación.
		LocalDateTime expiracion = usuario.getTokenRecuperacionExpiracion();

		if (usuario.getTokenRecuperacion() != null && expiracion != null
				&& expiracion.isAfter(LocalDateTime.now().plusMinutes(MINUTOS_CADUCIDAD - MINUTOS_ENTRE_SOLICITUDES))) {

			return;
		}

		byte[] bytes = new byte[32];
		random.nextBytes(bytes);

		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

		// Guardamos únicamente el hash del token.
		usuario.setTokenRecuperacion(calcularHash(token));

		usuario.setTokenRecuperacionExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_CADUCIDAD));

		usuarioRepository.save(usuario);

		String enlace = baseUrl + "/restablecer-password.html?token=" + token;

		SimpleMailMessage mensaje = new SimpleMailMessage();

		mensaje.setFrom(remitente);
		mensaje.setTo(usuario.getEmail());
		mensaje.setSubject("Shelfie - Recuperar contraseña");

		mensaje.setText("Hola, " + usuario.getNombre() + ":\n\n" + "Hemos recibido una solicitud para restablecer "
				+ "tu contraseña de Shelfie.\n\n" + "Puedes elegir una nueva contraseña aquí:\n" + enlace + "\n\n"
				+ "El enlace caduca en 30 minutos " + "y solo puede utilizarse una vez.\n\n"
				+ "Si no has solicitado este cambio, " + "puedes ignorar este correo.\n\n" + "El equipo de Shelfie.");

		mailSender.send(mensaje);
	}

	// H6.3 - Restablecer la contraseña.
	@Transactional
	public void restablecerPassword(String token, String nuevaPassword) {

		if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {

			throw new IllegalArgumentException("El enlace no es válido o ha caducado");
		}

		validarPassword(nuevaPassword);

		Usuario usuario = usuarioRepository.findByTokenRecuperacion(calcularHash(token))
				.orElseThrow(() -> new IllegalArgumentException("El enlace no es válido o ha caducado"));

		if (usuario.getTokenRecuperacionExpiracion() == null
				|| !usuario.getTokenRecuperacionExpiracion().isAfter(LocalDateTime.now())) {

			throw new IllegalArgumentException("El enlace no es válido o ha caducado");
		}

		usuario.setPassword(passwordEncoder.encode(nuevaPassword));

		// Invalidamos el token después de utilizarlo.
		usuario.setTokenRecuperacion(null);
		usuario.setTokenRecuperacionExpiracion(null);

		usuarioRepository.save(usuario);
	}

	private void validarPassword(String password) {

		if (password == null || password.isBlank() || password.length() < 8) {

			throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
		}

		if (password.getBytes(StandardCharsets.UTF_8).length > 72) {

			throw new IllegalArgumentException("La contraseña es demasiado larga");
		}
	}

	private String calcularHash(String token) {

		try {

			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(hash);

		} catch (NoSuchAlgorithmException e) {

			throw new IllegalStateException("No se ha podido generar el hash", e);
		}
	}
}