package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.Valoracion;
import com.example.demo.repository.ValoracionRepository;
import com.example.demo.service.ValoracionService;

class ValoracionServiceTest {

    @Test
    void noPermiteMasDeCincoEstrellas() {

        ValoracionRepository repositorio = mock(ValoracionRepository.class);
        ValoracionService servicio = new ValoracionService(repositorio);

        Valoracion valoracion = new Valoracion();
        valoracion.setObraId(1L);
        valoracion.setPuntuacion(6);

        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearValoracion(valoracion);
        });
    }
    @Test
    void noPermitePuntuacionesNegativas() {

        ValoracionRepository repositorio = mock(ValoracionRepository.class);
        ValoracionService servicio = new ValoracionService(repositorio);

        Valoracion valoracion = new Valoracion();
        valoracion.setObraId(1L);
        valoracion.setPuntuacion(-1);

        assertThrows(IllegalArgumentException.class, () -> {
            servicio.crearValoracion(valoracion);
        });
    }
    @Test
    void permiteUnaPuntuacionValida() {

        ValoracionRepository repositorio = mock(ValoracionRepository.class);
        ValoracionService servicio = new ValoracionService(repositorio);

        Valoracion valoracion = new Valoracion();
        valoracion.setObraId(1L);
        valoracion.setPuntuacion(4);

        when(repositorio.save(valoracion)).thenReturn(valoracion);

        Valoracion resultado = servicio.crearValoracion(valoracion);

        assertEquals(4, resultado.getPuntuacion());
    }
 
}