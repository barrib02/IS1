
package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.Actividad;
import com.example.demo.repository.ActividadRepository;
import com.example.demo.service.ActividadService;

class ActividadServiceTest {

    @Test
    void registraActividadCorrectamente() {

        // Creamos el repositorio falso.
        ActividadRepository repo = mock(ActividadRepository.class);

        // Creamos el servicio.
        ActividadService service = new ActividadService(repo);

        // Simulamos una actividad.
        Actividad actividad = new Actividad(
                1L, 10L, "VALORACION", "Ha valorado una obra");

        when(repo.save(any(Actividad.class))).thenReturn(actividad);

        // Ejecutamos el método.
        Actividad resultado = service.registrarActividad(
                1L, 10L, "VALORACION", "Ha valorado una obra");

        // Comprobamos el resultado.
        assertNotNull(resultado);
        assertEquals(1L, resultado.getUsuarioId());
        assertEquals(10L, resultado.getObraId());
        assertEquals("VALORACION", resultado.getTipo());
        assertEquals("Ha valorado una obra", resultado.getDescripcion());

        verify(repo).save(any(Actividad.class));
    }

    @Test
    void obtieneActividadesDeUnUsuario() {

        // Creamos el repositorio falso.
        ActividadRepository repo = mock(ActividadRepository.class);

        // Creamos el servicio.
        ActividadService service = new ActividadService(repo);

        // Creamos actividades de prueba.
        Actividad actividad1 = new Actividad(
                1L, 10L, "RESENA", "Ha publicado una reseña");

        Actividad actividad2 = new Actividad(
                1L, 20L, "VALORACION", "Ha valorado una obra");

        when(repo.findByUsuarioIdOrderByFechaDesc(1L))
                .thenReturn(List.of(actividad2, actividad1));

        // Ejecutamos la consulta.
        List<Actividad> resultado =
                service.obtenerActividadReciente(1L);

        // Comprobamos los resultados.
        assertEquals(2, resultado.size());
        assertEquals("VALORACION", resultado.get(0).getTipo());
        assertEquals("RESENA", resultado.get(1).getTipo());

        verify(repo).findByUsuarioIdOrderByFechaDesc(1L);
    }

    @Test
    void devuelveListaVaciaSiNoHayActividades() {

        ActividadRepository repo = mock(ActividadRepository.class);
        ActividadService service = new ActividadService(repo);

        when(repo.findByUsuarioIdOrderByFechaDesc(1L))
                .thenReturn(List.of());

        List<Actividad> resultado =
                service.obtenerActividadReciente(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
