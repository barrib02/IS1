package com.example.demo.dto;

public class ProgresoRequest {
	private Integer paginasLeidas; // libros
	private Integer temporadaActual; // series
	private Integer capituloActual; // series

	public Integer getPaginasLeidas() {
		return paginasLeidas;
	}

	public void setPaginasLeidas(Integer paginasLeidas) {
		this.paginasLeidas = paginasLeidas;
	}

	public Integer getTemporadaActual() {
		return temporadaActual;
	}

	public void setTemporadaActual(Integer temporadaActual) {
		this.temporadaActual = temporadaActual;
	}

	public Integer getCapituloActual() {
		return capituloActual;
	}

	public void setCapituloActual(Integer capituloActual) {
		this.capituloActual = capituloActual;
	}
}