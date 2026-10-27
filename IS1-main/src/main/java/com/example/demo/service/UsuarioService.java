
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;

    // Inyectamos el repositorio, BCrypt y el servicio de correo.
    public UsuarioService(
            UsuarioRepository usuarioRepository,
            BCryptPasswordEncoder encoder,
            EmailService emailService) {

        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    // H6.1 - REGISTRO DE USUARIOS
    public Usuario registrar(String nombre,
                             String email,
                             String password) {

        // Evitamos registrar dos usuarios con el mismo correo.
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya existe");
        }

        // Ciframos la contraseña antes de guardarla.
        String passwordCifrada = encoder.encode(password);

        // Creamos la cuenta sin verificar.
        Usuario usuario = new Usuario(
                nombre,
                email,
                passwordCifrada
        );

        usuario.setEmailVerificado(false);

        // Generamos un token único de verificación.
        String token = UUID.randomUUID().toString();

        usuario.setTokenVerificacion(token);

        // El enlace caducará en 24 horas.
        usuario.setTokenExpiracion(
                LocalDateTime.now().plusHours(24)
        );

        // Guardamos la cuenta en Neon.
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Enviamos el correo de verificación mediante Gmail.
        emailService.enviarCorreoVerificacion(
                usuarioGuardado.getEmail(),
                token
        );

        return usuarioGuardado;
    }

    // H6.1 - VERIFICACIÓN DEL CORREO ELECTRÓNICO
    public boolean verificarEmail(String token) {

        // Rechazamos tokens vacíos.
        if (token == null || token.isBlank()) {
            return false;
        }

        // Buscamos la cuenta asociada al token.
        Usuario usuario = usuarioRepository
                .findByTokenVerificacion(token)
                .orElse(null);

        if (usuario == null
                || usuario.getTokenExpiracion() == null) {
            return false;
        }

        // Comprobamos que el token no haya caducado.
        if (!LocalDateTime.now()
                .isBefore(usuario.getTokenExpiracion())) {
            return false;
        }

        // Activamos la cuenta.
        usuario.setEmailVerificado(true);

        // Eliminamos el token para que no pueda reutilizarse.
        usuario.setTokenVerificacion(null);
        usuario.setTokenExpiracion(null);

        // Guardamos los cambios en Neon.
        usuarioRepository.save(usuario);

        return true;
    }

    // H6.2 - INICIO DE SESIÓN
    public boolean login(String email,
                         String password) {

        // Buscamos al usuario por su correo.
        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElse(null);

        if (usuario == null) {
            return false;
        }

        // Comprobamos la contraseña cifrada.
        // Temporalmente no exigimos correo verificado
        // hasta comprobar que el envío funciona.
        return usuario.isEmailVerificado()
                && encoder.matches(
                        password,
                        usuario.getPassword()
        );
    }
}
