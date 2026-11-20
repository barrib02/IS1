package com.example.demo.dto;

public class ObraEnvioResponse {
	private Long id;
	private String titulo;
	private String autor;
	private String descripcion;
	private String tipo;
	private String estadoValidacion;
	private String justificacion;
	private String enviadaPor; // nombre del usuario que la subió

	public ObraEnvioResponse(Long id, String titulo, String autor, String descripcion, String tipo,
			String estadoValidacion, String justificacion, String enviadaPor) {
		this.id = id;
		this.titulo = titulo;
		this.autor = autor;
		this.descripcion = descripcion;
		this.tipo = tipo;
		this.estadoValidacion = estadoValidacion;
		this.justificacion = justificacion;
		this.enviadaPor = enviadaPor;
	}

	public Long getId() {
		return id;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getAutor() {
		return autor;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public String getTipo() {
		return tipo;
	}

	public String getEstadoValidacion() {
		return estadoValidacion;
	}

	public String getJustificacion() {
		return justificacion;
	}

	public String getEnviadaPor() {
		return enviadaPor;
	}
}
