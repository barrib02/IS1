package com.example.demo.dto;

import java.util.List;

public class EstadisticasResponse {
	private String periodo;
	private long librosLeidos;
	private long peliculasVistas;
	private long seriesCompletadas;
	private long videojuegosCompletados;
	private List<GeneroConteo> generosFavoritos;

	public EstadisticasResponse(String periodo, long librosLeidos, long peliculasVistas, long seriesCompletadas,
			long videojuegosCompletados, List<GeneroConteo> generosFavoritos) {
		this.periodo = periodo;
		this.librosLeidos = librosLeidos;
		this.peliculasVistas = peliculasVistas;
		this.seriesCompletadas = seriesCompletadas;
		this.videojuegosCompletados = videojuegosCompletados;
		this.generosFavoritos = generosFavoritos;
	}

	public String getPeriodo() {
		return periodo;
	}

	public long getLibrosLeidos() {
		return librosLeidos;
	}

	public long getPeliculasVistas() {
		return peliculasVistas;
	}

	public long getSeriesCompletadas() {
		return seriesCompletadas;
	}

	public long getVideojuegosCompletados() {
		return videojuegosCompletados;
	}

	public List<GeneroConteo> getGenerosFavoritos() {
		return generosFavoritos;
	}
}