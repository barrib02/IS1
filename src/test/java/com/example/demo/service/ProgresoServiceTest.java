package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.demo.dto.ProgresoRequest;
import com.example.demo.dto.ProgresoResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.EstadoUsuarioObraRepository;
import com.example.demo.repository.ObraRepository;
import com.example.demo.service.ActividadService;
import com.example.demo.service.PerfilService;
import com.example.demo.service.ProgresoService;

class ProgresoServiceTest {

	private final EstadoUsuarioObraRepository repo = mock(EstadoUsuarioObraRepository.class);
	private final ObraRepository obraRepo = mock(ObraRepository.class);
	private final PerfilService perfil = mock(PerfilService.class);
	private final ActividadService actividad = mock(ActividadService.class);
	private final TmdbService tmdb = mock(TmdbService.class);

	private final ProgresoService service = new ProgresoService(repo, obraRepo, perfil, actividad, tmdb);

	private void preparar(TipoObra tipo) {
		Usuario usuario = mock(Usuario.class);
		when(usuario.getId()).thenReturn(1L);
		when(perfil.obtenerUsuarioPorEmail("a@a.com")).thenReturn(usuario);

		Obra obra = new Obra();
		obra.setId(10L);
		obra.setTitulo("Obra");
		obra.setTipo(tipo);
		when(obraRepo.findById(10L)).thenReturn(Optional.of(obra));
		when(repo.findByUsuarioIdAndObraId(1L, 10L)).thenReturn(Optional.empty());
		when(repo.save(any(EstadoUsuarioObra.class))).thenAnswer(i -> i.getArgument(0));
	}

	@Test
	void guardaPaginasDeUnLibroYLoPoneEnCurso() {
		preparar(TipoObra.LIBRO);
		ProgresoRequest req = new ProgresoRequest();
		req.setPaginasLeidas(120);

		ProgresoResponse r = service.guardar("a@a.com", 10L, req);

		assertEquals(120, r.getPaginasLeidas());
		assertEquals("EN_CURSO", r.getEstado());
	}

	@Test
	void guardaTemporadaYCapituloDeUnaSerie() {
		preparar(TipoObra.SERIE);
		ProgresoRequest req = new ProgresoRequest();
		req.setTemporadaActual(2);
		req.setCapituloActual(5);

		ProgresoResponse r = service.guardar("a@a.com", 10L, req);

		assertEquals(2, r.getTemporadaActual());
		assertEquals(5, r.getCapituloActual());
	}

	@Test
	void noPermitePaginasNegativas() {
		preparar(TipoObra.LIBRO);
		ProgresoRequest req = new ProgresoRequest();
		req.setPaginasLeidas(-3);

		assertThrows(IllegalArgumentException.class, () -> service.guardar("a@a.com", 10L, req));
		verify(repo, never()).save(any(EstadoUsuarioObra.class));
	}

	@Test
	void unaPeliculaNoAdmiteProgreso() {
		preparar(TipoObra.PELICULA);
		assertThrows(IllegalArgumentException.class, () -> service.guardar("a@a.com", 10L, new ProgresoRequest()));
	}

	@Test
	void registraActividadAlCambiarElProgreso() {
		preparar(TipoObra.LIBRO);
		ProgresoRequest req = new ProgresoRequest();
		req.setPaginasLeidas(120);

		service.guardar("a@a.com", 10L, req);

		verify(actividad).registrarActividad(eq(1L), eq(10L), eq("PROGRESO"), anyString());
	}

	private ProgresoRequest prepararSerieTmdb(int temporada, int capitulo) {

		preparar(TipoObra.SERIE);

		Obra obra = obraRepo.findById(10L).orElseThrow();

		obra.setFuenteApi("TMDB");
		obra.setIdExterno("1399");

		ProgresoRequest req = new ProgresoRequest();

		req.setTemporadaActual(temporada);
		req.setCapituloActual(capitulo);

		return req;
	}

	@Test
	void permiteGuardarEpisodioExistenteEnTmdb() {

		ProgresoRequest req = prepararSerieTmdb(1, 2);

		when(tmdb.obtenerTemporada("1399", 1)).thenReturn("""
				{
				    "season_number": 1,
				    "episodes": [
				        {"episode_number": 1},
				        {"episode_number": 2}
				    ]
				}
				""");

		ProgresoResponse r = service.guardar("a@a.com", 10L, req);

		assertEquals(1, r.getTemporadaActual());
		assertEquals(2, r.getCapituloActual());

		verify(repo).save(any(EstadoUsuarioObra.class));
	}

	@Test
	void rechazaEpisodioInexistenteEnTmdb() {

		ProgresoRequest req = prepararSerieTmdb(1, 99);

		when(tmdb.obtenerTemporada("1399", 1)).thenReturn("""
				{
				    "season_number": 1,
				    "episodes": [
				        {"episode_number": 1},
				        {"episode_number": 2}
				    ]
				}
				""");

		assertThrows(IllegalArgumentException.class, () -> service.guardar("a@a.com", 10L, req));

		verify(repo, never()).save(any(EstadoUsuarioObra.class));
	}

	@Test
	void noGuardaProgresoSiTmdbNoEstaDisponible() {

		ProgresoRequest req = prepararSerieTmdb(1, 2);

		when(tmdb.obtenerTemporada("1399", 1)).thenThrow(new RuntimeException("API no disponible"));

		var error = assertThrows(org.springframework.web.server.ResponseStatusException.class,
				() -> service.guardar("a@a.com", 10L, req));

		assertEquals(503, error.getStatusCode().value());

		verify(repo, never()).save(any(EstadoUsuarioObra.class));
	}
}