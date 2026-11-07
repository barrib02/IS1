package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.Resena;
import com.example.demo.repository.ResenaRepository;
import com.example.demo.service.ResenaService;

class ResenaServiceTest {

	@Test
	void noPermiteResenaVacia() {

		/* Creamos un repositorio falso para no usar la base de datos real. */
		ResenaRepository repo = mock(ResenaRepository.class);

		// Creamos el servicio que queremos probar.
		ResenaService service = new ResenaService(repo);

		// Creamos una resena vacia.
		Resena resena = new Resena();
		resena.setTexto("");

		// Comprobamos que el servicio lanza una excepcion
		assertThrows(IllegalArgumentException.class, () -> service.crearResena(resena));
	}

	@Test
	void permiteResenaValida() {

		// Creamos un repositorio falso
		ResenaRepository repo = mock(ResenaRepository.class);

		// Creamos el servicio que vamos a probar
		ResenaService service = new ResenaService(repo);

		// Creamos una resena valida.
		Resena resena = new Resena();
		resena.setTexto("Muy buena obra");

		// Simulamos que al guardar en la base de datos
		// se devuelve exactamente la misma reseña.
		when(repo.save(any())).thenReturn(resena);

		// Ejecutamos el método que queremos probar.
		Resena resultado = service.crearResena(resena);

		// Verificamos que el texto guardado es correcto.
		assertEquals("Muy buena obra", resultado.getTexto());
	}
	
	@Test
	void noPermiteMasDe200Palabras() {
		// Creo el repositorio falso
	    ResenaRepository repo =
	            mock(ResenaRepository.class);
	    
	    //Creo el servicio que voy a probar
	    ResenaService service =
	            new ResenaService(repo);

	    // Creo un resena con mas de 200 palabras
	    Resena resena = new Resena();

	    String texto = "palabra ".repeat(201);

	    resena.setTexto(texto);
	    
	    // Compruebo que lanza excepcion
	    assertThrows(
	            IllegalArgumentException.class,
	            () -> service.crearResena(resena));
	}
}