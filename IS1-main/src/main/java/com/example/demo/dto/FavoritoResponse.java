package com.example.demo.dto;

public class FavoritoResponse {

    private Long obraId;
    private String titulo;
    private String tipo;
    private Integer posicion;

    public FavoritoResponse(Long obraId, String titulo,
                            String tipo, Integer posicion) {
        this.obraId = obraId;
        this.titulo = titulo;
        this.tipo = tipo;
        this.posicion = posicion;
    }

    public Long getObraId() {
        return obraId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getPosicion() {
        return posicion;
    }
}