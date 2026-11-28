package com.example.demo.dto;

import com.example.demo.entity.PrivacidadPerfil;

public class UsuarioBusquedaDTO {

	private Long id;
	private String nombre;
	private String fotoPerfilUrl;
	private PrivacidadPerfil privacidadPerfil;

	public UsuarioBusquedaDTO(Long id, String nombre, String fotoPerfilUrl, PrivacidadPerfil privacidadPerfil) {
		this.id = id;
		this.nombre = nombre;
		this.fotoPerfilUrl = fotoPerfilUrl;
		this.privacidadPerfil = privacidadPerfil;
	}

	public Long getId() { return id; }
	public String getNombre() { return nombre; }
	public String getFotoPerfilUrl() { return fotoPerfilUrl; }
	public PrivacidadPerfil getPrivacidadPerfil() { return privacidadPerfil; }
}