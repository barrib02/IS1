package com.example.demo.dto;

import com.example.demo.entity.PrivacidadPerfil;

import jakarta.validation.constraints.NotNull;

public class PrivacidadRequest {

	@NotNull(message = "Debes seleccionar un nivel de privacidad")
	private PrivacidadPerfil privacidad;

	public PrivacidadPerfil getPrivacidad() {
		return privacidad;
	}

	public void setPrivacidad(PrivacidadPerfil privacidad) {
		this.privacidad = privacidad;
	}
}