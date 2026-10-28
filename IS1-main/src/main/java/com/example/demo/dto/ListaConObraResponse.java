package com.example.demo.dto;

public class ListaConObraResponse {

    private Long id;
    private String nombre;
    private boolean contieneObra;

    public ListaConObraResponse(Long id, String nombre,
                                boolean contieneObra) {
        this.id = id;
        this.nombre = nombre;
        this.contieneObra = contieneObra;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isContieneObra() {
        return contieneObra;
    }
}