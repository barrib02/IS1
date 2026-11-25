
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.RecuperacionPasswordService;

class RecuperacionPasswordServiceTest {

	private UsuarioRepository usuarioRepository;
	private BCryptPasswordEncoder passwordEncoder;
	private JavaMailSender mailSender;
	private RecuperacionPasswordService service;
	private Usuario usuario;

	private static final String EMAIL = "usuario@example.com";
	private static final String REMITENTE = "shelfie.verificacion@gmail.com";
	private static final String TOKEN = "A".repeat(43);
	private static final String PASSWORD = "NuevaPassword123";

	@BeforeEach
	void setUp() {

		usuarioRepository = mock(UsuarioRepository.class);
		passwordEncoder = mock(BCryptPasswordEncoder.class);
		mailSender = mock(JavaMailSender.class);

		service = new RecuperacionPasswordService(usuarioRepository, passwordEncoder, mailSender,
				"http://localhost:8080", REMITENTE);

		usuario = new Usuario("Usuario de prueba", EMAIL, "hashAnterior");
		usuario.setEmailVerificado(true);
	}

	private String calcularHash(String token) throws Exception {

		return HexFormat.of()
				.formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
	}

	private String extraerToken(SimpleMailMessage mensaje) {

		Matcher matcher = Pattern.compile("token=([A-Za-z0-9_-]{43})").matcher(mensaje.getText());

		assertTrue(matcher.find());

		return matcher.group(1);
	}

	private void prepararTokenValido() throws Exception {

		String hash = calcularHash(TOKEN);

		usuario.setTokenRecuperacion(hash);
		usuario.setTokenRecuperacionExpiracion(LocalDateTime.now().plusMinutes(30));

		when(usuarioRepository.findByTokenRecuperacion(hash)).thenReturn(Optional.of(usuario));
	}

	// H6.3 - Solicitud de recuperación.

	@Test
	void debeGenerarTokenYEnviarCorreo() throws Exception {

		when(usuarioRepository.findByEmailAndEmailVerificadoTrue(EMAIL)).thenReturn(Optional.of(usuario));

		LocalDateTime antes = LocalDateTime.now();

		service.solicitarRecuperacion(EMAIL);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		verify(mailSender).send(captor.capture());
		verify(usuarioRepository).save(usuario);

		SimpleMailMessage mensaje = captor.getValue();
		String token = extraerToken(mensaje);

		assertEquals(REMITENTE, mensaje.getFrom());
		assertArrayEquals(new String[] { EMAIL }, mensaje.getTo());
		assertEquals("Shelfie - Recuperar contraseña", mensaje.getSubject());

		assertTrue(mensaje.getText().contains("http://localhost:8080/restablecer-password.html?token="));

		assertEquals(43, token.length());
		assertEquals(calcularHash(token), usuario.getTokenRecuperacion());
		assertNotEquals(token, usuario.getTokenRecuperacion());

		assertFalse(usuario.getTokenRecuperacionExpiracion().isBefore(antes.plusMinutes(30)));

		assertFalse(usuario.getTokenRecuperacionExpiracion().isAfter(LocalDateTime.now().plusMinutes(30)));
	}

	@Test
	void noDebeEnviarCorreoSiNoExisteCuentaVerificada() {

		when(usuarioRepository.findByEmailAndEmailVerificadoTrue(EMAIL)).thenReturn(Optional.empty());

		service.solicitarRecuperacion(EMAIL);

		verify(usuarioRepository, never()).save(any(Usuario.class));
		verifyNoInteractions(mailSender);
	}

	@Test
	void debeIgnorarCorreoNulo() {

		service.solicitarRecuperacion(null);

		verifyNoInteractions(usuarioRepository, mailSender);
	}

	@Test
	void debeIgnorarCorreoVacio() {

		service.solicitarRecuperacion("   ");

		verifyNoInteractions(usuarioRepository, mailSender);
	}

	@Test
	void debeEliminarEspaciosDelCorreo() {

		when(usuarioRepository.findByEmailAndEmailVerificadoTrue(EMAIL)).thenReturn(Optional.of(usuario));

		service.solicitarRecuperacion("  " + EMAIL + "  ");

		verify(usuarioRepository).findByEmailAndEmailVerificadoTrue(EMAIL);

		verify(mailSender).send(any(SimpleMailMessage.class));
	}

	@Test
	void debeImpedirSolicitudesConsecutivas() {

		when(usuarioRepository.findByEmailAndEmailVerificadoTrue(EMAIL)).thenReturn(Optional.of(usuario));

		service.solicitarRecuperacion(EMAIL);

		String primerHash = usuario.getTokenRecuperacion();

		service.solicitarRecuperacion(EMAIL);

		assertEquals(primerHash, usuario.getTokenRecuperacion());

		verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

		verify(usuarioRepository, times(1)).save(usuario);
	}

	@Test
	void debePermitirNuevaSolicitudPasadoElIntervalo() {

		when(usuarioRepository.findByEmailAndEmailVerificadoTrue(EMAIL)).thenReturn(Optional.of(usuario));

		service.solicitarRecuperacion(EMAIL);

		String primerHash = usuario.getTokenRecuperacion();

		// Simulamos que han transcurrido tres minutos.
		usuario.setTokenRecuperacionExpiracion(LocalDateTime.now().plusMinutes(27));

		service.solicitarRecuperacion(EMAIL);

		assertNotEquals(primerHash, usuario.getTokenRecuperacion());

		verify(mailSender, times(2)).send(any(SimpleMailMessage.class));

		verify(usuarioRepository, times(2)).save(usuario);
	}

	@Test
	void debePropagarErrorSiFallaEnvioCorreo() {

		when(usuarioRepository.findByEmailAndEmailVerificadoTrue(EMAIL)).thenReturn(Optional.of(usuario));

		doThrow(new MailSendException("Error SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

		assertThrows(MailSendException.class, () -> service.solicitarRecuperacion(EMAIL));
	}

	// H6.3 - Restablecimiento de contraseña.

	@Test
	void debeRestablecerPasswordEInvalidarToken() throws Exception {

		prepararTokenValido();

		when(passwordEncoder.encode(PASSWORD)).thenReturn("nuevoHashBCrypt");

		service.restablecerPassword(TOKEN, PASSWORD);

		assertEquals("nuevoHashBCrypt", usuario.getPassword());
		assertNull(usuario.getTokenRecuperacion());
		assertNull(usuario.getTokenRecuperacionExpiracion());

		verify(passwordEncoder).encode(PASSWORD);
		verify(usuarioRepository).save(usuario);
	}

	@Test
	void debeRechazarTokenCaducado() throws Exception {

		prepararTokenValido();

		usuario.setTokenRecuperacionExpiracion(LocalDateTime.now().minusMinutes(1));

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(TOKEN, PASSWORD));

		assertEquals("hashAnterior", usuario.getPassword());

		verifyNoInteractions(passwordEncoder);
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void debeRechazarTokenSinFechaDeCaducidad() throws Exception {

		prepararTokenValido();

		usuario.setTokenRecuperacionExpiracion(null);

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(TOKEN, PASSWORD));

		verifyNoInteractions(passwordEncoder);
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void debeRechazarTokenInexistente() throws Exception {

		when(usuarioRepository.findByTokenRecuperacion(calcularHash(TOKEN))).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(TOKEN, PASSWORD));

		verifyNoInteractions(passwordEncoder);
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void debeRechazarTokenConFormatoIncorrecto() {

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword("incorrecto", PASSWORD));

		verifyNoInteractions(usuarioRepository, passwordEncoder);
	}

	@Test
	void debeRechazarTokenNulo() {

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(null, PASSWORD));

		verifyNoInteractions(usuarioRepository, passwordEncoder);
	}

	@Test
	void noDebePermitirReutilizarToken() throws Exception {

		prepararTokenValido();

		when(passwordEncoder.encode(PASSWORD)).thenReturn("nuevoHashBCrypt");

		service.restablecerPassword(TOKEN, PASSWORD);

		when(usuarioRepository.findByTokenRecuperacion(calcularHash(TOKEN))).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(TOKEN, "OtraPassword123"));

		verify(usuarioRepository, times(1)).save(usuario);
		verify(passwordEncoder, times(1)).encode(anyString());
	}

	// H6.3 - Validación de contraseñas.

	@Test
	void debeRechazarPasswordDemasiadoCorta() {

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(TOKEN, "1234567"));

		verifyNoInteractions(usuarioRepository, passwordEncoder);
	}

	@Test
	void debeRechazarPasswordNula() {

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(TOKEN, null));

		verifyNoInteractions(usuarioRepository, passwordEncoder);
	}

	@Test
	void debeRechazarPasswordEnBlanco() {

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(TOKEN, "        "));

		verifyNoInteractions(usuarioRepository, passwordEncoder);
	}

	@Test
	void debeRechazarPasswordSuperiorA72Bytes() {

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(TOKEN, "a".repeat(73)));

		verifyNoInteractions(usuarioRepository, passwordEncoder);
	}

	@Test
	void debeComprobarLimiteDeBytesEnCaracteresUnicode() {

		String password = "ñ".repeat(37);

		assertThrows(IllegalArgumentException.class, () -> service.restablecerPassword(TOKEN, password));

		verifyNoInteractions(usuarioRepository, passwordEncoder);
	}

	@Test
	void debeAceptarPasswordDe72Bytes() throws Exception {

		prepararTokenValido();

		String password = "a".repeat(72);

		when(passwordEncoder.encode(password)).thenReturn("hashBCrypt");

		service.restablecerPassword(TOKEN, password);

		assertEquals("hashBCrypt", usuario.getPassword());

		verify(usuarioRepository).save(usuario);
	}
}
