package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.dto.EstadisticasResponse;
import com.example.demo.dto.MesConteo;
import com.example.demo.entity.*;
import com.example.demo.repository.EstadoUsuarioObraRepository;
import com.example.demo.service.EstadisticasService;
import com.example.demo.service.PerfilService;

class EstadisticasServiceTest {

	@Test
	void cuentaLibrosPeliculasYGeneros() {
		EstadoUsuarioObraRepository repo = mock(EstadoUsuarioObraRepository.class);
		PerfilService perfil = mock(PerfilService.class);
		EstadisticasService service = new EstadisticasService(repo, perfil);

		Usuario usuario = mock(Usuario.class);
		when(usuario.getId()).thenReturn(1L);
		when(perfil.obtenerUsuarioPorEmail("a@a.com")).thenReturn(usuario);

		when(repo.contarPorTipo(eq(1L), eq(EstadoObra.COMPLETADA), eq(true), any()))
				.thenReturn(List.of(new Object[] { TipoObra.LIBRO, 3L }, new Object[] { TipoObra.PELICULA, 2L }));
		when(repo.contarGeneros(eq(1L), eq(EstadoObra.COMPLETADA), eq(true), any()))
				.thenReturn(List.of(new Object[] { GeneroObra.TERROR, 4L }, new Object[] { GeneroObra.DRAMA, 1L }));

		EstadisticasResponse r = service.obtener("a@a.com", PeriodoEstadistica.TODO);

		assertEquals(3, r.getLibrosLeidos());
		assertEquals(2, r.getPeliculasVistas());
		assertEquals(0, r.getSeriesCompletadas());
		assertEquals("TERROR", r.getGenerosFavoritos().get(0).getGenero());
	}
	
	@Test
	void agrupaLasObrasCompletadasPorMesYTipo() {
		EstadoUsuarioObraRepository repo = mock(EstadoUsuarioObraRepository.class);
		PerfilService perfil = mock(PerfilService.class);
		EstadisticasService service = new EstadisticasService(repo, perfil);

		Usuario usuario = mock(Usuario.class);
		when(usuario.getId()).thenReturn(1L);
		when(perfil.obtenerUsuarioPorEmail("a@a.com")).thenReturn(usuario);

		EstadoUsuarioObra estado = new EstadoUsuarioObra(1L, 10L, EstadoObra.COMPLETADA);
		estado.setFechaCompletada(LocalDateTime.now());
		Obra obra = new Obra();
		obra.setTipo(TipoObra.LIBRO);

		List<Object[]> filas = new ArrayList<>();
		filas.add(new Object[] { estado, obra });
		when(repo.completadasEntre(eq(1L), eq(EstadoObra.COMPLETADA), any(), any())).thenReturn(filas);

		List<MesConteo> meses = service.ultimosMeses("a@a.com");

		assertEquals(12, meses.size());
		assertEquals(YearMonth.now().toString(), meses.get(11).getMes());
		assertEquals(1, meses.get(11).getLibros());
		assertEquals(0, meses.get(0).getLibros());
	}
}