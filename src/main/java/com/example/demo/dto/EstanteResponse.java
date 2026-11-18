package com.example.demo.dto;

import java.util.List;

// Un "estante" del Discover: título creativo, subtítulo y sus obras.
public class EstanteResponse {

	private String titulo;
	private String subtitulo;
	private List<SearchResultDTO> obras;

	public EstanteResponse(String titulo, String subtitulo, List<SearchResultDTO> obras) {
		this.titulo = titulo;
		this.subtitulo = subtitulo;
		this.obras = obras;
	}

	public String getTitulo() { return titulo; }
	public String getSubtitulo() { return subtitulo; }
	public List<SearchResultDTO> getObras() { return obras; }
}
