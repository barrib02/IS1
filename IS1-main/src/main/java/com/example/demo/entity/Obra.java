package com.example.demo.entity;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import jakarta.persistence.Column;

@Entity
@Table(name = "obras")
public class Obra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String titulo;
	private String autor;
	@Column(columnDefinition = "TEXT")
	private String descripcion;

	@Enumerated(EnumType.STRING)
	private TipoObra tipo;
	

	@ElementCollection(targetClass = GeneroObra.class)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "obra_generos", joinColumns = @JoinColumn(name = "obra_id"))
	@Column(name = "genero")
	private Set<GeneroObra> generos = new HashSet<>();

	public Obra() {
	}

	public Obra(Long id, String titulo, String autor, String descripcion, TipoObra tipo, Set<GeneroObra> generos) {
		this.id = id;
		this.titulo = titulo;
		this.autor = autor;
		this.descripcion = descripcion;
		this.tipo = tipo;
		this.generos = generos;
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


	public Set<GeneroObra> getGeneros() {
	    return generos;
	}

	public void setGeneros(Set<GeneroObra> generos) {
	    this.generos = generos;
	}
}
