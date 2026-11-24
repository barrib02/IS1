
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import com.example.demo.entity.EstadoValidacion;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;

@ExtendWith(MockitoExtension.class)
class ObraExternaPersistenciaServiceTest {

    // Simulamos el repositorio para no conectarnos a Neon.
    @Mock
    private ObraRepository obraRepository;

    // Creamos el servicio con el repositorio simulado.
    @InjectMocks
    private ObraExternaPersistenciaService persistenciaService;

    @Test
    void debeGuardarReferenciaMinima() {

        // Simulamos que Neon devuelve la obra guardada.
        when(obraRepository.saveAndFlush(any(Obra.class)))
                .thenAnswer(invocacion -> {
                    Obra obra = invocacion.getArgument(0);
                    obra.setId(100L);
                    return obra;
                });

        // Ejecutamos el método de persistencia.
        Obra resultado = persistenciaService.guardarReferencia(
                "TMDB",
                "671",
                TipoObra.PELICULA,
                "Harry Potter y la piedra filosofal"
        );

        // Capturamos la obra enviada al repositorio.
        ArgumentCaptor<Obra> captor =
                ArgumentCaptor.forClass(Obra.class);

        verify(obraRepository, times(1))
                .saveAndFlush(captor.capture());

        Obra obraGuardada = captor.getValue();

        // Comprobamos su identificación externa.
        assertEquals("TMDB", obraGuardada.getFuenteApi());
        assertEquals("671", obraGuardada.getIdExterno());
        assertEquals(TipoObra.PELICULA, obraGuardada.getTipo());

        // Comprobamos que conserva un título de respaldo.
        assertEquals("Harry Potter y la piedra filosofal",
                obraGuardada.getTitulo());

        // No almacenamos metadatos que vendrán de la API.
        assertNull(obraGuardada.getDescripcion());
        assertNull(obraGuardada.getImagenUrl());

        // Las obras de APIs no pasan por moderación manual.
        assertTrue(obraGuardada.isValidada());
        assertEquals(EstadoValidacion.VALIDADA,
                obraGuardada.getEstadoValidacion());

        // El resultado corresponde a la obra guardada.
        assertEquals(100L, resultado.getId());
    }
}
