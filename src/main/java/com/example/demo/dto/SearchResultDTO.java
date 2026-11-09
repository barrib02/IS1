package com.example.demo.dto;

public class SearchResultDTO {

	private Long id;
	private String titulo;
	private String tipo;

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

}