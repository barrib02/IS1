package com.example.demo.dto;

public class GeneroConteo {
	private String genero;
	private long total;

	public GeneroConteo(String genero, long total) {
		this.genero = genero;
		this.total = total;
	}

	public String getGenero() { return genero; }
	public long getTotal() { return total; }
}