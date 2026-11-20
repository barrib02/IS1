package com.example.demo.dto;

public class ProgresoResponse {
	private Long obraId;
	private String titulo;
	private String tipo;
	private String estado; // puede ser null si no hay estado
	private Integer paginasLeidas;
	private Integer temporadaActual;
	private Integer capituloActual;
	private String fechaCompletada; // yyyy-MM-dd o null

	public ProgresoResponse(Long obraId, String titulo, String tipo, String estado, Integer paginasLeidas,
			Integer temporadaActual, Integer capituloActual, String fechaCompletada) {
		this.obraId = obraId;
		this.titulo = titulo;
		this.tipo = tipo;
		this.estado = estado;
		this.paginasLeidas = paginasLeidas;
		this.temporadaActual = temporadaActual;
		this.capituloActual = capituloActual;
		this.fechaCompletada = fechaCompletada;
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

	public String getEstado() {
		return estado;
	}

	public Integer getPaginasLeidas() {
		return paginasLeidas;
	}

	public Integer getTemporadaActual() {
		return temporadaActual;
	}

	public Integer getCapituloActual() {
		return capituloActual;
	}

	public String getFechaCompletada() {
		return fechaCompletada;
	}
}