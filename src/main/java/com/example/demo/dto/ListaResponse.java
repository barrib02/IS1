package com.example.demo.dto;

public class ListaResponse {

    private Long id;
    private String nombre;
    private boolean publica;
    private long numeroObras;

    public ListaResponse(Long id, String nombre,
                         boolean publica, long numeroObras) {
        this.id = id;
        this.nombre = nombre;
        this.publica = publica;
        this.numeroObras = numeroObras;
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

    public long getNumeroObras() {
        return numeroObras;
    }
}