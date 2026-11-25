package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.demo.dto.ObraRequest;
import com.example.demo.entity.EstadoValidacion;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.ObraRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.NotificacionService;
import com.example.demo.service.ObraService;
import com.example.demo.service.PerfilService;

// Ruta: src/test/java/com/example/demo/service/ObraServiceTest.java
@ExtendWith(MockitoExtension.class)
class ObraServiceTest {

	@Mock
	private ObraRepository obraRepository;
	@Mock
	private PerfilService perfilService;
	@Mock
	private NotificacionService notificacionService;
	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private ObraService obraService;

	// ---------- Utilidades de los tests ----------

	// Una obra validada cuyo autor es el usuario 5.
	private Obra obraValidadaDelUsuario5() {
		Obra obra = new Obra();
		obra.setId(1L);
		obra.setTitulo("Viejo título");
		obra.setAutor("Autor");
		obra.setDescripcion("Descripción");
		obra.setTipo(TipoObra.LIBRO);
		obra.setAutorId(5L);
		obra.setEstadoValidacion(EstadoValidacion.VALIDADA);
		obra.setValidada(true);
		return obra;
	}

	private ObraRequest peticion(String titulo) {
		ObraRequest request = new ObraRequest();
		request.setTitulo(titulo);
		request.setAutor("Autor");
		request.setDescripcion("Descripción");
		request.setTipo("LIBRO");
		return request;
	}

	private Usuario usuarioConId(Long id) {
		Usuario usuario = mock(Usuario.class);
		when(usuario.getId()).thenReturn(id);
		return usuario;
	}

	// ---------- Búsqueda (sprint anterior) ----------

	@Test
	void buscarSinCriteriosUsaPaginacionYNoCargaTodo() {
		when(obraRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(new Obra())));

		assertEquals(1, obraService.buscar(null, null).size());

		verify(obraRepository).findAll(any(Pageable.class));
		verify(obraRepository, never()).findAll();
	}

	@Test
	void explorarDevuelveLasObrasRecientes() {
		when(obraRepository.findTop12ByOrderByIdDesc()).thenReturn(List.of(new Obra(), new Obra()));

		assertEquals(2, obraService.explorar().size());
	}

	// ---------- H1.3: edición y revalidación automática ----------

	@Test
	void siElAutorEditaUnaObraValidadaVuelveAPendiente() {
		Obra obra = obraValidadaDelUsuario5();
		when(obraRepository.findById(1L)).thenReturn(Optional.of(obra));
		when(perfilService.obtenerUsuarioPorEmail("autor@a.com")).thenReturn(usuarioConId(5L));
		when(obraRepository.save(any(Obra.class))).thenAnswer(i -> i.getArgument(0));

		Obra resultado = obraService.editar("autor@a.com", 1L, peticion("Nuevo título"));

		assertEquals("Nuevo título", resultado.getTitulo());
		assertEquals(EstadoValidacion.PENDIENTE, resultado.getEstadoValidacion());
		assertFalse(resultado.isValidada());
	}

	@Test
	void siElAutorGuardaSinCambiosNoSeRevalida() {
		Obra obra = obraValidadaDelUsuario5();
		when(obraRepository.findById(1L)).thenReturn(Optional.of(obra));
		when(perfilService.obtenerUsuarioPorEmail("autor@a.com")).thenReturn(usuarioConId(5L));
		when(obraRepository.save(any(Obra.class))).thenAnswer(i -> i.getArgument(0));

		// Mismos datos que ya tenía la obra.
		Obra resultado = obraService.editar("autor@a.com", 1L, peticion("Viejo título"));

		assertEquals(EstadoValidacion.VALIDADA, resultado.getEstadoValidacion());
		assertTrue(resultado.isValidada());
	}

	@Test
	void otroUsuarioNoPuedeEditarLaObra() {
		when(obraRepository.findById(1L)).thenReturn(Optional.of(obraValidadaDelUsuario5()));
		when(perfilService.obtenerUsuarioPorEmail("otro@a.com")).thenReturn(usuarioConId(9L));

		assertThrows(SecurityException.class, () -> obraService.editar("otro@a.com", 1L, peticion("Hack")));

		verify(obraRepository, never()).save(any(Obra.class));
	}

	@Test
	void editarConTituloVacioFalla() {
		when(obraRepository.findById(1L)).thenReturn(Optional.of(obraValidadaDelUsuario5()));
		when(perfilService.obtenerUsuarioPorEmail("autor@a.com")).thenReturn(usuarioConId(5L));

		assertThrows(IllegalArgumentException.class, () -> obraService.editar("autor@a.com", 1L, peticion("   ")));
	}

	// ---------- H8.1: moderación ----------

	@Test
	void rechazarSinJustificacionFalla() {
		when(perfilService.esModerador("mod@a.com")).thenReturn(true);

		assertThrows(IllegalArgumentException.class, () -> obraService.rechazar("mod@a.com", 1L, "   "));

		verify(obraRepository, never()).save(any(Obra.class));
	}

	@Test
	void rechazarGuardaElMotivoYNotificaAlAutor() {
		Obra obra = obraValidadaDelUsuario5();
		when(perfilService.esModerador("mod@a.com")).thenReturn(true);
		when(obraRepository.findById(1L)).thenReturn(Optional.of(obra));
		when(obraRepository.save(any(Obra.class))).thenAnswer(i -> i.getArgument(0));

		Obra resultado = obraService.rechazar("mod@a.com", 1L, "Falta la descripción");

		assertEquals(EstadoValidacion.RECHAZADA, resultado.getEstadoValidacion());
		assertFalse(resultado.isValidada());
		verify(notificacionService).crear(eq(5L), eq(1L), eq("OBRA_RECHAZADA"), anyString());
	}

	@Test
	void validarNotificaAlAutor() {
		Obra obra = obraValidadaDelUsuario5();
		obra.setEstadoValidacion(EstadoValidacion.PENDIENTE);
		obra.setValidada(false);
		when(perfilService.esModerador("mod@a.com")).thenReturn(true);
		when(obraRepository.findById(1L)).thenReturn(Optional.of(obra));
		when(obraRepository.save(any(Obra.class))).thenAnswer(i -> i.getArgument(0));

		Obra resultado = obraService.validar("mod@a.com", 1L, null);

		assertEquals(EstadoValidacion.VALIDADA, resultado.getEstadoValidacion());
		assertTrue(resultado.isValidada());
		verify(notificacionService).crear(eq(5L), eq(1L), eq("OBRA_VALIDADA"), anyString());
	}

	@Test
	void unUsuarioNormalNoPuedeValidar() {
		when(perfilService.esModerador("user@a.com")).thenReturn(false);

		assertThrows(SecurityException.class, () -> obraService.validar("user@a.com", 1L, null));

		verify(obraRepository, never()).save(any(Obra.class));
	}
}
