package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.controller.ObraController;
import com.example.demo.dto.ObraRequest;
import com.example.demo.dto.SearchResultDTO;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.service.BusquedaUnificadaService;
import com.example.demo.service.ObraService;

class ObraControllerTest {

	private ObraService obraService;
	private BusquedaUnificadaService busquedaService;
	private ObraController controller;
	private Authentication auth;
	private ObraRequest request;

	@BeforeEach
	void preparar() {

		obraService = mock(ObraService.class);

		busquedaService = mock(BusquedaUnificadaService.class);

		auth = mock(Authentication.class);

		controller = new ObraController(obraService, busquedaService);

		request = new ObraRequest();

		request.setTitulo("Obra de prueba");
		request.setTipo("LIBRO");
		request.setAutor("Autor de prueba");
		request.setDescripcion("Descripción de prueba");
	}

	@Test
	void permiteRegistrarSinCoincidencias() {

		when(auth.getName()).thenReturn("usuario@test.com");

		when(busquedaService.comprobarAntesDeRegistrar("Obra de prueba", TipoObra.LIBRO)).thenReturn(List.of());

		Obra obra = new Obra();

		when(obraService.crear("usuario@test.com", request)).thenReturn(obra);

		assertSame(obra, controller.crearObra(request, auth));

		verify(obraService).crear("usuario@test.com", request);
	}

	@Test
	void impideRegistrarSinRevisarCoincidencias() {

		SearchResultDTO coincidencia = new SearchResultDTO(10L, "Obra de prueba", "LIBRO");

		when(busquedaService.comprobarAntesDeRegistrar("Obra de prueba", TipoObra.LIBRO))
				.thenReturn(List.of(coincidencia));

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> controller.crearObra(request, auth));

		assertEquals(HttpStatus.CONFLICT, error.getStatusCode());

		verifyNoInteractions(obraService);
	}

	@Test
	void permiteRegistrarTrasConfirmarCoincidencias() {

		request.setCoincidenciasRevisadas(true);

		when(auth.getName()).thenReturn("usuario@test.com");

		SearchResultDTO coincidencia = new SearchResultDTO(10L, "Obra de prueba", "LIBRO");

		when(busquedaService.comprobarAntesDeRegistrar("Obra de prueba", TipoObra.LIBRO))
				.thenReturn(List.of(coincidencia));

		Obra obra = new Obra();

		when(obraService.crear("usuario@test.com", request)).thenReturn(obra);

		assertSame(obra, controller.crearObra(request, auth));

		verify(obraService).crear("usuario@test.com", request);
	}

	@Test
	void impideRegistrarSiFallaLaApi() {

		when(busquedaService.comprobarAntesDeRegistrar("Obra de prueba", TipoObra.LIBRO))
				.thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE));

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> controller.crearObra(request, auth));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());

		verifyNoInteractions(obraService);
	}

	@Test
	void rechazaTituloVacio() {

		request.setTitulo("");

		assertThrows(IllegalArgumentException.class, () -> controller.crearObra(request, auth));

		verifyNoInteractions(obraService, busquedaService);
	}

	@Test
	void rechazaTipoIncorrecto() {

		request.setTipo("TIPO_INVALIDO");

		assertThrows(IllegalArgumentException.class, () -> controller.crearObra(request, auth));

		verifyNoInteractions(obraService, busquedaService);
	}

	@Test
	void editarObraNoConsultaApis() {

		when(auth.getName()).thenReturn("usuario@test.com");

		Obra obra = new Obra();

		when(obraService.editar("usuario@test.com", 10L, request)).thenReturn(obra);

		assertSame(obra, controller.editarObra(10L, request, auth));

		verifyNoInteractions(busquedaService);
	}
}