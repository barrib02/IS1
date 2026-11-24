package com.example.demo.dto;

public class SearchResultDTO {

	private Long id;
	private String titulo;
	private String tipo;

	// API de origen: TMDB, GOOGLE_BOOKS, RAWG o null para obras manuales.
	private String fuenteApi;

	// Identificador de la obra en la API externa.
	private String idExterno;

	// Imagen de portada o póster.
	private String imagenUrl;

	public SearchResultDTO() {
	}

	public SearchResultDTO(Long num, String tit, String tip) {
		this.id = num;
		this.titulo = tit;
		this.tipo = tip;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long num) {
		this.id = num;
	}

	public String getTitulo() {
		return this.titulo;
	}

	public void setTitulo(String tit) {
		this.titulo = tit;
	}

	public String getTipo() {
		return this.tipo;
	}

	public void setTipo(String tip) {
		this.tipo = tip;
	}

	public String getFuenteApi() {
		return fuenteApi;
	}

	public void setFuenteApi(String fuenteApi) {
		this.fuenteApi = fuenteApi;
	}

	public String getIdExterno() {
		return idExterno;
	}

	public void setIdExterno(String idExterno) {
		this.idExterno = idExterno;
	}

	public String getImagenUrl() {
		return imagenUrl;
	}

	public void setImagenUrl(String imagenUrl) {
		this.imagenUrl = imagenUrl;
	}

}