package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.entity.PrivacidadPerfil;
import com.example.demo.dto.PerfilResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.PerfilService;
import com.example.demo.service.CloudinaryService;

class PerfilServiceTest {

	@Test
	void obtienePerfilPorId() {

		// Creamos un repositorio falso para no usar la BD.
		UsuarioRepository repo = mock(UsuarioRepository.class);

		// Creamos el servicio que queremos probar.
		CloudinaryService cloudinaryService = mock(CloudinaryService.class);
		PerfilService service = new PerfilService(repo, cloudinaryService);

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
		CloudinaryService cloudinaryService = mock(CloudinaryService.class);
		PerfilService service = new PerfilService(repo, cloudinaryService);

		// Simulamos que no existe ningún usuario con ese ID.
		when(repo.findById(1L)).thenReturn(Optional.empty());

		// Comprobamos que se lanza la excepción esperada.
		assertThrows(IllegalArgumentException.class, () -> service.obtenerPerfilPorId(1L));
	}

	@Test
	void actualizarFotoPerfil_guardaUrlCorrectamente() throws IOException {

		// Creamos las dependencias simuladas.
		UsuarioRepository repo = mock(UsuarioRepository.class);
		CloudinaryService cloudinaryService = mock(CloudinaryService.class);
		PerfilService service = new PerfilService(repo, cloudinaryService);

		// Datos de prueba.
		String email = "prueba@test.com";
		String url = "https://cloudinary.com/foto.jpg";

		Usuario usuario = new Usuario();

		MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.jpg", "image/jpeg",
				"imagen de prueba".getBytes());

		// Simulamos la búsqueda del usuario.
		when(repo.findByEmail(email)).thenReturn(Optional.of(usuario));

		// Simulamos la subida a Cloudinary.
		when(cloudinaryService.subirImagen(archivo, "goodreads-plus/perfiles")).thenReturn(url);

		// Ejecutamos el método.
		String resultado = service.actualizarFotoPerfil(email, archivo);

		// Comprobamos los resultados con JUnit 5.
		assertEquals(url, resultado);
		assertEquals(url, usuario.getFotoPerfilUrl());

		// Comprobamos que se guarda el usuario.
		verify(repo).save(usuario);

		// Comprobamos que se llama a Cloudinary.
		verify(cloudinaryService).subirImagen(archivo, "goodreads-plus/perfiles");
	}

	@Test
	void actualizarBanner_guardaUrlCorrectamente() throws IOException {

		// Creamos las dependencias simuladas.
		UsuarioRepository repo = mock(UsuarioRepository.class);
		CloudinaryService cloudinaryService = mock(CloudinaryService.class);
		PerfilService service = new PerfilService(repo, cloudinaryService);

		// Datos de prueba.
		String email = "prueba@test.com";
		String url = "https://cloudinary.com/banner.jpg";

		Usuario usuario = new Usuario();

		MockMultipartFile archivo = new MockMultipartFile("archivo", "banner.jpg", "image/jpeg",
				"imagen de prueba".getBytes());

		// Simulamos que el usuario existe.
		when(repo.findByEmail(email)).thenReturn(Optional.of(usuario));

		// Simulamos la subida a Cloudinary.
		when(cloudinaryService.subirImagen(archivo, "goodreads-plus/banners")).thenReturn(url);

		// Ejecutamos el método.
		String resultado = service.actualizarBanner(email, archivo);

		// Comprobamos que la URL es correcta.
		assertEquals(url, resultado);
		assertEquals(url, usuario.getBannerUrl());

		// Comprobamos que se guarda en el repositorio.
		verify(repo).save(usuario);

		// Comprobamos que se utiliza la carpeta correcta.
		verify(cloudinaryService).subirImagen(archivo, "goodreads-plus/banners");
	}

	@Test
	void actualizarFotoPerfil_errorCloudinary() throws IOException {

		UsuarioRepository repo = mock(UsuarioRepository.class);
		CloudinaryService cloudinaryService = mock(CloudinaryService.class);
		PerfilService service = new PerfilService(repo, cloudinaryService);

		String email = "prueba@test.com";
		Usuario usuario = new Usuario();

		MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.jpg", "image/jpeg",
				"imagen de prueba".getBytes());

		when(repo.findByEmail(email)).thenReturn(Optional.of(usuario));

		// Simulamos un fallo de Cloudinary.
		when(cloudinaryService.subirImagen(archivo, "goodreads-plus/perfiles"))
				.thenThrow(new IOException("Error al subir imagen"));

		// Comprobamos que se produce la excepción.
		assertThrows(IOException.class, () -> service.actualizarFotoPerfil(email, archivo));

		// Comprobamos que no se guarda el usuario.
		verify(repo, never()).save(any(Usuario.class));
	}

	@Test
	void actualizarBanner_errorCloudinary() throws IOException {

		UsuarioRepository repo = mock(UsuarioRepository.class);
		CloudinaryService cloudinaryService = mock(CloudinaryService.class);
		PerfilService service = new PerfilService(repo, cloudinaryService);

		String email = "prueba@test.com";
		Usuario usuario = new Usuario();

		MockMultipartFile archivo = new MockMultipartFile("archivo", "banner.jpg", "image/jpeg",
				"imagen de prueba".getBytes());

		when(repo.findByEmail(email)).thenReturn(Optional.of(usuario));

		// Simulamos un error de Cloudinary.
		when(cloudinaryService.subirImagen(archivo, "goodreads-plus/banners"))
				.thenThrow(new IOException("Error al subir banner"));

		// Comprobamos que se lanza la excepción.
		assertThrows(IOException.class, () -> service.actualizarBanner(email, archivo));

		// Comprobamos que no se guarda el usuario.
		verify(repo, never()).save(any(Usuario.class));
	}

	@Test
	void actualizarFotoPerfil_usuarioNoExiste() {

		UsuarioRepository repo = mock(UsuarioRepository.class);
		CloudinaryService cloudinaryService = mock(CloudinaryService.class);
		PerfilService service = new PerfilService(repo, cloudinaryService);

		String email = "inexistente@test.com";

		MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.jpg", "image/jpeg",
				"imagen de prueba".getBytes());

		// Simulamos que el usuario no existe.
		when(repo.findByEmail(email)).thenReturn(Optional.empty());

		// Comprobamos que se lanza la excepción.
		assertThrows(IllegalArgumentException.class, () -> service.actualizarFotoPerfil(email, archivo));

		// No debe intentar subir la imagen ni guardar cambios.
		verifyNoInteractions(cloudinaryService);
		verify(repo, never()).save(any(Usuario.class));
	}

	@Test
	void actualizarBanner_usuarioNoExiste() {

		UsuarioRepository repo = mock(UsuarioRepository.class);
		CloudinaryService cloudinaryService = mock(CloudinaryService.class);
		PerfilService service = new PerfilService(repo, cloudinaryService);

		String email = "inexistente@test.com";

		MockMultipartFile archivo = new MockMultipartFile("archivo", "banner.jpg", "image/jpeg",
				"imagen de prueba".getBytes());

		// Simulamos que el usuario no existe.
		when(repo.findByEmail(email)).thenReturn(Optional.empty());

		// Comprobamos que se lanza la excepción.
		assertThrows(IllegalArgumentException.class, () -> service.actualizarBanner(email, archivo));

		// No debe subir la imagen ni guardar cambios.
		verifyNoInteractions(cloudinaryService);
		verify(repo, never()).save(any(Usuario.class));
	}

	@Test
	void actualizarNombre_guardaElNombreLimpio() {
		UsuarioRepository repo = mock(UsuarioRepository.class);
		PerfilService service = new PerfilService(repo, mock(CloudinaryService.class));
		Usuario usuario = new Usuario();
		when(repo.findByEmail("a@a.com")).thenReturn(Optional.of(usuario));

		PerfilResponse r = service.actualizarNombre("a@a.com", "  Kevin  ");

		assertEquals("Kevin", r.getNombre());
		verify(repo).save(usuario);
	}

	@Test
	void actualizarNombre_vacioLanzaError() {
		UsuarioRepository repo = mock(UsuarioRepository.class);
		PerfilService service = new PerfilService(repo, mock(CloudinaryService.class));

		assertThrows(IllegalArgumentException.class, () -> service.actualizarNombre("a@a.com", "   "));
	}

	@Test
	void perfilPrivadoImpideAccesoAOtrosPeroNoAlDueno() {

		UsuarioRepository repo = mock(UsuarioRepository.class);

		PerfilService service = new PerfilService(repo, mock(CloudinaryService.class));

		Usuario usuario = new Usuario();

		usuario.setNombre("Kevin");
		usuario.setEmail("kevin@gmail.com");
		usuario.setBiografia("secreta");
		usuario.setPrivacidadPerfil(PrivacidadPerfil.PRIVADO);

		when(repo.findById(1L)).thenReturn(Optional.of(usuario));

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> service.obtenerPerfilPorId(1L, "otro@a.com"));

		assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());

		PerfilResponse perfil = service.obtenerPerfilPorId(1L, "kevin@gmail.com");

		assertEquals("secreta", perfil.getBiografia());
		assertEquals(PrivacidadPerfil.PRIVADO, perfil.getPrivacidadPerfil());
	}

	@Test
	void perfilPublicoPermiteAccesoAOtrosUsuarios() {

		UsuarioRepository repo = mock(UsuarioRepository.class);
		PerfilService service = new PerfilService(repo, mock(CloudinaryService.class));

		Usuario usuario = new Usuario();

		usuario.setEmail("dueno@test.com");
		usuario.setBiografia("Mi biografía");
		usuario.setPrivacidadPerfil(PrivacidadPerfil.PUBLICO);

		when(repo.findById(1L)).thenReturn(Optional.of(usuario));

		PerfilResponse resultado = service.obtenerPerfilPorId(1L, "otro@test.com");

		assertEquals("Mi biografía", resultado.getBiografia());
		assertEquals(PrivacidadPerfil.PUBLICO, resultado.getPrivacidadPerfil());
	}

	@Test
	void perfilSoloAmigosRestringeAccesoProvisionalmente() {

		UsuarioRepository repo = mock(UsuarioRepository.class);
		PerfilService service = new PerfilService(repo, mock(CloudinaryService.class));

		Usuario usuario = new Usuario();

		usuario.setEmail("dueno@test.com");
		usuario.setBiografia("Mi biografía");
		usuario.setPrivacidadPerfil(PrivacidadPerfil.SOLO_AMIGOS);

		when(repo.findById(1L)).thenReturn(Optional.of(usuario));

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> service.obtenerPerfilPorId(1L, "otro@test.com"));

		assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());

		PerfilResponse resultado = service.obtenerPerfilPorId(1L, "dueno@test.com");

		assertEquals("Mi biografía", resultado.getBiografia());
	}

	@Test
	void actualizarPrivacidadGuardaLosTresNiveles() {

		UsuarioRepository repo = mock(UsuarioRepository.class);
		PerfilService service = new PerfilService(repo, mock(CloudinaryService.class));

		Usuario usuario = new Usuario();

		usuario.setEmail("prueba@test.com");

		when(repo.findByEmail("prueba@test.com")).thenReturn(Optional.of(usuario));

		for (PrivacidadPerfil privacidad : PrivacidadPerfil.values()) {

			PerfilResponse resultado = service.actualizarPrivacidad("prueba@test.com", privacidad);

			assertEquals(privacidad, usuario.getPrivacidadPerfil());
			assertEquals(privacidad, resultado.getPrivacidadPerfil());

			assertEquals(privacidad == PrivacidadPerfil.PUBLICO, resultado.isPerfilPublico());
		}

		verify(repo, times(3)).save(usuario);
	}

	@Test
	void actualizarPrivacidadRechazaValorNulo() {

		UsuarioRepository repo = mock(UsuarioRepository.class);
		PerfilService service = new PerfilService(repo, mock(CloudinaryService.class));

		assertThrows(IllegalArgumentException.class, () -> service.actualizarPrivacidad("prueba@test.com", null));

		verifyNoInteractions(repo);
	}

}
