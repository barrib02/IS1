
package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // H6.1 - Buscar un usuario por su correo electrónico.
    // Se utiliza durante el inicio de sesión.
    Optional<Usuario> findByEmail(String email);

    // H6.1 - Comprobar si un correo ya está registrado.
    // Evita que dos usuarios tengan el mismo email.
    boolean existsByEmail(String email);

    // H6.1 - Buscar un usuario por su token de verificación.
    // Permitirá activar la cuenta al pulsar el enlace del correo.
    Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);
}
