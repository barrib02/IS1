package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.UsuarioService;

class UsuarioServiceTest {

    @Test
    void noPermiteEmailsDuplicados() {

        // Creamos los mocks necesarios.
        UsuarioRepository repo =
                mock(UsuarioRepository.class);

        BCryptPasswordEncoder encoder =
                mock(BCryptPasswordEncoder.class);

        EmailService emailService =
                mock(EmailService.class);

        // Creamos el servicio que queremos probar.
        UsuarioService service =
                new UsuarioService(
                        repo,
                        encoder,
                        emailService);

        // Simulamos que el email ya existe.
        when(repo.existsByEmail("test@test.com"))
                .thenReturn(true);

        // Comprobamos que se lanza una excepción.
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrar(
                        "Alba",
                        "test@test.com",
                        "password123"));
    }

    @Test
    void registraUsuarioCorrectamente() {

        UsuarioRepository repo =
                mock(UsuarioRepository.class);

        BCryptPasswordEncoder encoder =
                mock(BCryptPasswordEncoder.class);

        EmailService emailService =
                mock(EmailService.class);

        UsuarioService service =
                new UsuarioService(
                        repo,
                        encoder,
                        emailService);

        // Simulamos que el email NO existe.
        when(repo.existsByEmail("alba@test.com"))
                .thenReturn(false);

        // Simulamos que BCrypt devuelve una contraseña cifrada.
        when(encoder.encode("password123"))
                .thenReturn("PASSWORD_CIFRADA");

        Usuario usuarioGuardado =
                new Usuario(
                        "Alba",
                        "alba@test.com",
                        "PASSWORD_CIFRADA");

        // Simulamos el guardado en la BD.
        when(repo.save(any(Usuario.class)))
                .thenReturn(usuarioGuardado);

        Usuario resultado =
                service.registrar(
                        "Alba",
                        "alba@test.com",
                        "password123");

        assertEquals(
                "alba@test.com",
                resultado.getEmail());
    }

    @Test
    void loginCorrecto() {

        UsuarioRepository repo =
                mock(UsuarioRepository.class);

        BCryptPasswordEncoder encoder =
                mock(BCryptPasswordEncoder.class);

        EmailService emailService =
                mock(EmailService.class);

        UsuarioService service =
                new UsuarioService(
                        repo,
                        encoder,
                        emailService);

        Usuario usuario = new Usuario();

        usuario.setEmail("alba@test.com");
        usuario.setPassword("HASH");
        usuario.setEmailVerificado(true);

        // Simulamos que encontramos al usuario.
        when(repo.findByEmail("alba@test.com"))
                .thenReturn(Optional.of(usuario));

        // Simulamos que la contraseña coincide.
        when(encoder.matches(
                "password123",
                "HASH"))
                .thenReturn(true);

        boolean resultado =
                service.login(
                        "alba@test.com",
                        "password123");

        assertTrue(resultado);
    }

    @Test
    void loginIncorrecto() {

        UsuarioRepository repo =
                mock(UsuarioRepository.class);

        BCryptPasswordEncoder encoder =
                mock(BCryptPasswordEncoder.class);

        EmailService emailService =
                mock(EmailService.class);

        UsuarioService service =
                new UsuarioService(
                        repo,
                        encoder,
                        emailService);

        // Simulamos que no existe ningún usuario.
        when(repo.findByEmail("alba@test.com"))
                .thenReturn(Optional.empty());

        boolean resultado =
                service.login(
                        "alba@test.com",
                        "password123");

        assertFalse(resultado);
    }
}