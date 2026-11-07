package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;
import com.example.demo.service.ObraService;

class ObraServiceTest {

    @Test
    void buscaPorTitulo() {

        // Creamos un repositorio falso.
        ObraRepository repo =
                mock(ObraRepository.class);

        // Creamos el servicio 
        ObraService service =
                new ObraService(repo);

        // Creamos una obra de ejemplo.
        Obra obra = new Obra();
        obra.setTitulo("Harry Potter");

        // Simulamos la respuesta del repositorio.
        when(repo.findByTituloContainingIgnoreCase("Harry"))
                .thenReturn(List.of(obra));

        // Ejecutamos el método que queremos probar.
        List<Obra> resultado =
                service.buscar("Harry", null);

        // Comprobamos que encontramos una obra.
        assertEquals(1, resultado.size());

        // Comprobamos que es la que esperábamos.
        assertEquals(
                "Harry Potter",
                resultado.get(0).getTitulo());
    }

    @Test
    void buscaPorTipo() {

        ObraRepository repo =
                mock(ObraRepository.class);

        ObraService service =
                new ObraService(repo);

        Obra obra = new Obra();
        obra.setTipo(TipoObra.LIBRO);

        when(repo.findByTipo(TipoObra.LIBRO))
                .thenReturn(List.of(obra));

        List<Obra> resultado =
                service.buscar(null, TipoObra.LIBRO);

        assertEquals(1, resultado.size());

        assertEquals(
                TipoObra.LIBRO,
                resultado.get(0).getTipo());
    }

    @Test
    void buscaPorTituloYTipo() {

        ObraRepository repo =
                mock(ObraRepository.class);

        ObraService service =
                new ObraService(repo);

        Obra obra = new Obra();
        obra.setTitulo("Harry Potter");
        obra.setTipo(TipoObra.LIBRO);

        when(repo.findByTituloContainingIgnoreCaseAndTipo(
                "Harry",
                TipoObra.LIBRO))
                .thenReturn(List.of(obra));

        List<Obra> resultado =
                service.buscar(
                        "Harry",
                        TipoObra.LIBRO);

        assertEquals(1, resultado.size());

        assertEquals(
                "Harry Potter",
                resultado.get(0).getTitulo());

        assertEquals(
                TipoObra.LIBRO,
                resultado.get(0).getTipo());
    }

    @Test
    void devuelveTodasLasObrasSiNoHayFiltros() {

        ObraRepository repo =
                mock(ObraRepository.class);

        ObraService service =
                new ObraService(repo);

        Obra obra1 = new Obra();
        Obra obra2 = new Obra();

        when(repo.findAll())
                .thenReturn(List.of(obra1, obra2));

        List<Obra> resultado =
                service.buscar(null, null);

        assertEquals(2, resultado.size());
    }
}