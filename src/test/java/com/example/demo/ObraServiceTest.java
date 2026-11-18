package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;
import com.example.demo.service.ObraService;

// Ruta: src/test/java/com/example/demo/service/ObraServiceTest.java
@ExtendWith(MockitoExtension.class)
class ObraServiceTest {

	@Mock
	private ObraRepository obraRepository;

	@InjectMocks
	private ObraService obraService;

	@Test
	void buscarSinCriteriosUsaPaginacionYNoCargaTodo() {
		when(obraRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(new Obra())));

		assertEquals(1, obraService.buscar(null, null).size());

		verify(obraRepository).findAll(any(Pageable.class));
		verify(obraRepository, never()).findAll();   // nunca debe cargar todas las obras
	}

	@Test
	void buscarPorTituloYTipoUsaLaConsultaCombinada() {
		when(obraRepository.findByTituloContainingIgnoreCaseAndTipo(eq("dune"), eq(TipoObra.LIBRO), any(Pageable.class)))
				.thenReturn(List.of(new Obra()));

		assertEquals(1, obraService.buscar("dune", TipoObra.LIBRO).size());
	}

	@Test
	void buscarConTituloEnBlancoSeTrataComoSinTitulo() {
		when(obraRepository.findByTipo(eq(TipoObra.SERIE), any(Pageable.class))).thenReturn(List.of(new Obra()));

		assertEquals(1, obraService.buscar("   ", TipoObra.SERIE).size());
	}

	@Test
	void explorarDevuelveLasObrasRecientes() {
		when(obraRepository.findTop12ByOrderByIdDesc()).thenReturn(List.of(new Obra(), new Obra()));

		assertEquals(2, obraService.explorar().size());
	}
}
