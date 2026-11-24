
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.example.demo.dto.SearchResultDTO;
import com.example.demo.dto.TmdbObraDTO;

class TmdbResultadoMapperTest {

    // Probamos el conversor sin conectarnos a TMDB ni a Neon.
    private final TmdbResultadoMapper mapper = new TmdbResultadoMapper();

    @Test
    void debeConvertirPeliculaDeTmdb() {

        // Preparamos una película procedente de TMDB.
        TmdbObraDTO obra = new TmdbObraDTO();
        obra.setTmdbId(671L);
        obra.setTitulo("Harry Potter y la piedra filosofal");
        obra.setTipo("PELICULA");
        obra.setPosterPath("/poster.jpg");

        // Convertimos la película al formato de Shelfie.
        SearchResultDTO resultado = mapper.convertir(obra);

        // Todavía no tiene identificador interno en Neon.
        assertNull(resultado.getId());

        // Comprobamos los datos de búsqueda.
        assertEquals("Harry Potter y la piedra filosofal",
                resultado.getTitulo());
        assertEquals("PELICULA", resultado.getTipo());

        // Comprobamos la identificación externa.
        assertEquals("TMDB", resultado.getFuenteApi());
        assertEquals("671", resultado.getIdExterno());

        // Comprobamos la URL del póster.
        assertEquals(
                "https://image.tmdb.org/t/p/w500/poster.jpg",
                resultado.getImagenUrl()
        );
    }

    @Test
    void debeConvertirSerieSinPoster() {

        // Preparamos una serie que no tiene póster.
        TmdbObraDTO obra = new TmdbObraDTO();
        obra.setTmdbId(1399L);
        obra.setTitulo("Juego de tronos");
        obra.setTipo("SERIE");

        SearchResultDTO resultado = mapper.convertir(obra);

        // Conservamos el tipo y el identificador externo.
        assertEquals("SERIE", resultado.getTipo());
        assertEquals("1399", resultado.getIdExterno());

        // Si TMDB no proporciona un póster, no inventamos uno.
        assertNull(resultado.getImagenUrl());
    }
}
