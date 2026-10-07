package com.example.demo.entity;

public enum EstadoObra {
PENDIENTE,
EN_CURSO,
COMPLETADA,
ABANDONADA
}

public enum TipoObra {
LIBRO,
PELICULA,
SERIE,
VIDEOJUEGO
}

public class Obra {
 
private Long id;
private String titulo;
private String descripcion;
private TipoObra tipo;
private EstadoObra estado;
 
public Obra() {
}
 
public Obra(Long id, String titulo, String descripcion,
TipoObra tipo, EstadoObra estado) {
this.id = id;
this.titulo = titulo;
this.descripcion = descripcion;
this.tipo = tipo;
this.estado = estado;
}
 
public Long getId() {
return id;
}
 
public void setId(Long id) {
this.id = id;
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
 
public TipoObra getTipo() {
return tipo;
}
 
public void setTipo(TipoObra tipo) {
this.tipo = tipo;
}
 
public EstadoObra getEstado() {
return estado;
}
 
public void setEstado(EstadoObra estado) {
this.estado = estado;
}
}
