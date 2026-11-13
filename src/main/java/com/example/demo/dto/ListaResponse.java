package com.example.demo.dto;

public class ListaResponse {

	private Long id;
	private String nombre;
	private boolean publica;
	private long numeroObras;
	private String descripcion;
	private String creador;

	public ListaResponse(Long id, String nombre, boolean publica, long numeroObras, String descripcion,
			String creador) {
		this.id = id;
		this.nombre = nombre;
		this.publica = publica;
		this.numeroObras = numeroObras;
		this.descripcion = descripcion;
		this.creador = creador;
	}

	public String getCreador() {
		return creador;
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

	public String getDescripcion() {
		return descripcion;
	}

}