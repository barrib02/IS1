package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.demo.dto.PerfilResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.PerfilService;

class PerfilServiceTest {

	@Test
	void obtienePerfilPorId() {

		// Creamos un repositorio falso para no usar la BD.
		UsuarioRepository repo = mock(UsuarioRepository.class);

		// Creamos el servicio que queremos probar.
		PerfilService service = new PerfilService(repo);

		// Creamos un usuario de prueba.
		Usuario usuario = new Usuario();

		usuario.setNombre("Kevin");
		usuario.setEmail("kevin@gmail.com");
		usuario.setBiografia("Me gustan los libros");

		// Simulamos que la búsqueda por ID encuentra al usuario.
		when(repo.findById(1L)).thenReturn(Optional.of(usuario));

		// Ejecutamos el método que queremos probar.
		PerfilResponse perfil = service.obtenerPerfilPorId(1L);

		// Comprobamos que el nombre es correcto.
		assertEquals("Kevin", perfil.getNombre());

		// Comprobamos que la biografía es correcta.
		assertEquals("Me gustan los libros", perfil.getBiografia());
	}

	@Test
	void lanzaErrorSiUsuarioNoExiste() {

		// Creamos el repositorio falso.
		UsuarioRepository repo = mock(UsuarioRepository.class);

		// Creamos el servicio.
		PerfilService service = new PerfilService(repo);

		// Simulamos que no existe ningún usuario con ese ID.
		when(repo.findById(1L)).thenReturn(Optional.empty());

		// Comprobamos que se lanza la excepción esperada.
		assertThrows(IllegalArgumentException.class, () -> service.obtenerPerfilPorId(1L));
	}
}
