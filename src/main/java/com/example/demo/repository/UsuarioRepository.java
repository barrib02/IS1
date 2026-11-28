
package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import com.example.demo.entity.Usuario;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.demo.entity.PrivacidadPerfil;

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

	// H6.3 - Buscar y bloquear el usuario durante el restablecimiento.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Usuario> findByTokenRecuperacion(String tokenRecuperacion);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Usuario> findByEmailAndEmailVerificadoTrue(String email);
	
	// Búsqueda de usuarios por nombre.
	// Incluye PUBLICO y SOLO_AMIGOS. Los usuarios antiguos sin privacidad_perfil
	// se tratan según el campo perfil_publico, igual que en Usuario.getPrivacidadPerfil().
	@Query("""
			SELECT u FROM Usuario u
			WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
			  AND u.emailVerificado = true
			  AND LOWER(u.email) <> LOWER(:emailActual)
			  AND (
			        u.privacidadPerfil IN :visibles
			        OR (u.privacidadPerfil IS NULL
			            AND (u.perfilPublico IS NULL OR u.perfilPublico = true))
			      )
			ORDER BY u.nombre
			""")
	List<Usuario> buscarPorNombre(@Param("texto") String texto,
			@Param("emailActual") String emailActual,
			@Param("visibles") List<PrivacidadPerfil> visibles,
			Pageable pageable);
}
