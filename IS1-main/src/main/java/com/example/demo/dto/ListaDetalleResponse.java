package com.example.demo.dto;

import java.util.List;

public class ListaDetalleResponse {

    private Long id;
    private String nombre;
    private boolean publica;
    private boolean mia;
    private List<SearchResultDTO> obras;

    public ListaDetalleResponse(Long id, String nombre, boolean publica,
                                boolean mia, List<SearchResultDTO> obras) {
        this.id = id;
        this.nombre = nombre;
        this.publica = publica;
        this.mia = mia;
        this.obras = obras;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isPublica() {
        return publica;
    }

    // true si la lista pertenece al usuario que la consulta.
    public boolean isMia() {
        return mia;
    }

    public List<SearchResultDTO> getObras() {
        return obras;
    }
}