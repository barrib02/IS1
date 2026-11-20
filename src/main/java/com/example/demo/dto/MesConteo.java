package com.example.demo.dto;

public class MesConteo {
	private String mes; // formato yyyy-MM
	private long libros;
	private long peliculas;
	private long series;
	private long videojuegos;

	public MesConteo(String mes, long libros, long peliculas, long series, long videojuegos) {
		this.mes = mes;
		this.libros = libros;
		this.peliculas = peliculas;
		this.series = series;
		this.videojuegos = videojuegos;
	}

	public String getMes() {
		return mes;
	}

	public long getLibros() {
		return libros;
	}

	public long getPeliculas() {
		return peliculas;
	}

	public long getSeries() {
		return series;
	}

	public long getVideojuegos() {
		return videojuegos;
	}
}