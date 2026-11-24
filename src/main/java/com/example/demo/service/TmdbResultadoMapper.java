
package com.example.demo.service;

import org.springframework.stereotype.Component;

import com.example.demo.dto.SearchResultDTO;
import com.example.demo.dto.TmdbObraDTO;

@Component
public class TmdbResultadoMapper {

    // Convierte una obra de TMDB al formato del buscador de Shelfie.
    public SearchResultDTO convertir(TmdbObraDTO obra) {

        // El ID interno es null porque todavía no
        // hemos registrado la obra en Neon.
        SearchResultDTO resultado = new SearchResultDTO(
                null,
                obra.getTitulo(),
                obra.getTipo()
        );

        // Conservamos el identificador externo de TMDB.
        resultado.setFuenteApi("TMDB");
        resultado.setIdExterno(
                String.valueOf(obra.getTmdbId())
        );

        // Construimos la URL del póster sin guardarlo en Neon.
        String posterPath = obra.getPosterPath();

        if (posterPath != null && !posterPath.isBlank()) {
            resultado.setImagenUrl(
                    "https://image.tmdb.org/t/p/w500" + posterPath
            );
        }

        return resultado;
    }
}
