package com.example.demo.service;

import com.example.demo.model.Obra;
import com.example.demo.model.TipoObra;
import com.example.demo.model.EstadoObra;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ObraService {

    private final List<Obra> obras = new ArrayList<>();

    public ObraService() {

        obras.add(new Obra(
                1L,
                "El Señor de los Anillos",
                "J.R.R. Tolkien",
                TipoObra.LIBRO,
                EstadoObra.PENDIENTE
        ));

        obras.add(new Obra(
                2L,
                "Breaking Bad",
                "Vince Gilligan",
                TipoObra.SERIE,
                EstadoObra.EN_CURSO
        ));

        obras.add(new Obra(
                3L,
                "The Witcher 3",
                "CD Projekt Red",
                TipoObra.VIDEOJUEGO,
                EstadoObra.COMPLETADA
        ));
    }

    public List<Obra> getObras() {
        return obras;
    }
}
