package com.example.demo.dto;

public class CompletadaResponse {
	private Long obraId;
	private String titulo;
	private String tipo;
	private String fecha;

	public CompletadaResponse(Long obraId, String titulo, String tipo, String fecha) {
		this.obraId = obraId;
		this.titulo = titulo;
		this.tipo = tipo;
		this.fecha = fecha;
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

	public String getFecha() {
		return fecha;
	}
}