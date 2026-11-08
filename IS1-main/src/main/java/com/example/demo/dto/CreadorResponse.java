package com.example.demo.dto;

import java.util.List;

public class CreadorResponse {

	private Long id;

	private String nombre;

	private String foto;

	private String biografia;

	private List<SearchResultDTO> obras;

	private List<SearchResultDTO> destacadas;

	public CreadorResponse(Long id, String nombre, String foto, String biografia, List<SearchResultDTO> obras,
			List<SearchResultDTO> destacadas) {

		this.id = id;
		this.nombre = nombre;
		this.foto = foto;
		this.biografia = biografia;
		this.obras = obras;
		this.destacadas = destacadas;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getFoto() {
		return foto;
	}

	public String getBiografia() {
		return biografia;
	}

	public List<SearchResultDTO> getObras() {
		return obras;
	}

	public List<SearchResultDTO> getDestacadas() {
		return destacadas;
	}
}
