package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.dto.ArchivoExportado;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.ExportacionService;
import com.example.demo.service.PerfilService;

class ExportacionServiceTest {

	private final PerfilService perfil = mock(PerfilService.class);
	private final ValoracionRepository valoraciones = mock(ValoracionRepository.class);
	private final ObraRepository obras = mock(ObraRepository.class);

	private final ExportacionService service = new ExportacionService(perfil, mock(ListaRepository.class),
			mock(ListaObraRepository.class), valoraciones, mock(ResenaRepository.class), obras);

	private void preparar() {
		Usuario usuario = mock(Usuario.class);
		when(usuario.getId()).thenReturn(1L);
		when(perfil.obtenerUsuarioPorEmail("a@a.com")).thenReturn(usuario);

		Obra obra = new Obra();
		obra.setId(10L);
		obra.setTitulo("=Dune");
		obra.setTipo(TipoObra.LIBRO);
		when(valoraciones.findByUsuarioId(1L)).thenReturn(List.of(new Valoracion(1L, 10L, 5, 1L)));
		when(obras.findAllById(any())).thenReturn(List.of(obra));
	}

	@Test
	void exportaValoracionesEnCsvYNeutralizaFormulas() {
		preparar();

		ArchivoExportado archivo = service.exportarValoraciones("a@a.com", "csv");
		String csv = new String(archivo.contenido(), StandardCharsets.UTF_8);

		assertTrue(archivo.nombre().endsWith(".csv"));
		assertTrue(csv.contains("\"obraId\";\"titulo\";\"tipo\";\"puntuacion\""));
		assertTrue(csv.contains("\"'=Dune\""));
	}

	@Test
	void exportaValoracionesEnJson() {
		preparar();

		ArchivoExportado archivo = service.exportarValoraciones("a@a.com", "json");

		assertTrue(archivo.nombre().endsWith(".json"));
		assertTrue(new String(archivo.contenido(), StandardCharsets.UTF_8).contains("\"puntuacion\""));
	}

	@Test
	void formatoInvalidoLanzaError() {
		assertThrows(IllegalArgumentException.class, () -> service.exportarValoraciones("a@a.com", "xml"));
	}
}
