package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.demo.dto.CreadorResponse;
import com.example.demo.dto.SearchResultDTO;
import com.example.demo.entity.Creador;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.CreadorRepository;
import com.example.demo.repository.ObraRepository;
import com.example.demo.service.CreadorService;

class CreadorServiceTest {

	@Test
	void obtieneFichaCorrectamente() {

		// Repositorios falsos
		CreadorRepository creadorRepo = mock(CreadorRepository.class);

		ObraRepository obraRepo = mock(ObraRepository.class);

		// Servicio a probar
		CreadorService service = new CreadorService(creadorRepo, obraRepo);

		// Creador de ejemplo
		Creador creador = new Creador();

		creador.setNombre("J.K. Rowling");
		creador.setBiografia("Autora de Harry Potter");

		// Obra asociada
		Obra obra = new Obra();

		obra.setId(1L);
		obra.setTitulo("Harry Potter");
		obra.setTipo(TipoObra.LIBRO);

		when(creadorRepo.findById(1L)).thenReturn(Optional.of(creador));

		when(obraRepo.findByCreadorIdOrderByAnioAsc(1L)).thenReturn(List.of(obra));

		CreadorResponse resultado = service.obtenerFicha(1L);

		assertEquals("J.K. Rowling", resultado.getNombre());

		assertEquals(1, resultado.getObras().size());
	}

	@Test
	void lanzaErrorSiNoExiste() {

		CreadorRepository creadorRepo = mock(CreadorRepository.class);

		ObraRepository obraRepo = mock(ObraRepository.class);

		CreadorService service = new CreadorService(creadorRepo, obraRepo);

		when(creadorRepo.findById(1L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> service.obtenerFicha(1L));
	}

	@Test
	void devuelveObrasOrdenadasPorAnio() {

		CreadorRepository creadorRepo = mock(CreadorRepository.class);

		ObraRepository obraRepo = mock(ObraRepository.class);

		CreadorService service = new CreadorService(creadorRepo, obraRepo);

		Creador creador = new Creador();

		when(creadorRepo.findById(1L)).thenReturn(Optional.of(creador));

		Obra obra1 = new Obra();
		obra1.setTitulo("Harry Potter");
		obra1.setAnio(1997);
		obra1.setTipo(TipoObra.LIBRO);

		Obra obra2 = new Obra();
		obra2.setTitulo("Harry Potter 2");
		obra2.setAnio(1998);
		obra2.setTipo(TipoObra.LIBRO);

		when(obraRepo.findByCreadorIdOrderByAnioAsc(1L)).thenReturn(List.of(obra1, obra2));

		CreadorResponse resultado = service.obtenerFicha(1L);

		assertEquals("Harry Potter", resultado.getObras().get(0).getTitulo());
	}

	@Test
	void devuelveTresObrasDestacadas() {

		CreadorRepository creadorRepo = mock(CreadorRepository.class);

		ObraRepository obraRepo = mock(ObraRepository.class);

		CreadorService service = new CreadorService(creadorRepo, obraRepo);

		Obra obra1 = new Obra();
		obra1.setId(1L);
		obra1.setTitulo("Obra 1");
		obra1.setTipo(TipoObra.LIBRO);

		Obra obra2 = new Obra();
		obra2.setId(2L);
		obra2.setTitulo("Obra 2");
		obra2.setTipo(TipoObra.LIBRO);

		Obra obra3 = new Obra();
		obra3.setId(3L);
		obra3.setTitulo("Obra 3");
		obra3.setTipo(TipoObra.LIBRO);

		Obra obra4 = new Obra();
		obra4.setId(4L);
		obra4.setTitulo("Obra 4");
		obra4.setTipo(TipoObra.LIBRO);

		when(obraRepo.findByCreadorIdOrderByAnioAsc(1L)).thenReturn(List.of(obra1, obra2, obra3, obra4));

		List<SearchResultDTO> resultado = service.obtenerDestacadas(1L);

		assertEquals(3, resultado.size());
	}

	@Test
	void devuelveListaVaciaSiNoTieneObras() {

		CreadorRepository creadorRepo = mock(CreadorRepository.class);

		ObraRepository obraRepo = mock(ObraRepository.class);

		CreadorService service = new CreadorService(creadorRepo, obraRepo);

		when(obraRepo.findByCreadorIdOrderByAnioAsc(1L)).thenReturn(List.of());

		List<SearchResultDTO> resultado = service.obtenerDestacadas(1L);

		assertTrue(resultado.isEmpty());
	}
}