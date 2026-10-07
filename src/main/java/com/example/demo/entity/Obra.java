package com.example.demo.entity;

public class Obra {
 
private Long id;
private String titulo;
private String autor;
private String descripcion;
private TipoObra tipo;
private EstadoObra estado;
 
public Obra() {
}
 
public Obra(Long id, String titulo, String autor String descripcion,
TipoObra tipo, EstadoObra estado) {
this.id = id;
this.titulo = titulo;
this.autor = autor;
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

public String getAutor() {
return autor;
}
 
public void setAutor(String autor) {
this.autor = autor;
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

public EstadoObra setEstado(EstadoObra estado){
this.estado = estado;
 
public void setEstado(EstadoObra estado) {
this.estado = estado;
}
}
