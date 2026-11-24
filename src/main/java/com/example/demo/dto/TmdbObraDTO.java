
package com.example.demo.dto;

import java.util.HashSet;
import java.util.Set;

import com.example.demo.entity.GeneroObra;

// Representa una pelicula o serie obtenida de la API de TMDB.
// Permite mostrar sus datos sin almacenarlos en Neon.
public class TmdbObraDTO {

    // Identificador de la obra en TMDB.
    private Long tmdbId;

    // Titulo de la pelicula o serie.
    private String titulo;

    // Descripcion de la obra.
    private String descripcion;

    // Tipo de obra: PELICULA o SERIE.
    private String tipo;

    // Año de estreno de la obra.
    private Integer anio;

    // Ruta de la imagen de portada proporcionada por TMDB.
    private String posterPath;

    // Generos adaptados a los utilizados en Shelfie.
    private Set<GeneroObra> generos = new HashSet<>();

    // Constructor vacio necesario para trabajar con JSON.
    public TmdbObraDTO() {
    }

    // Devuelve el identificador externo de TMDB.
    public Long getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Set<GeneroObra> getGeneros() {
        return generos;
    }

    public void setGeneros(Set<GeneroObra> generos) {
        this.generos = generos;
    }
}
