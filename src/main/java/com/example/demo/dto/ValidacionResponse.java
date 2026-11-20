package com.example.demo.dto;

public class ValidacionResponse {
	private String estado;        // PENDIENTE, VALIDADA o RECHAZADA
	private String justificacion; // puede ser null

	public ValidacionResponse(String estado, String justificacion) {
		this.estado = estado;
		this.justificacion = justificacion;
	}

	public String getEstado() {
		return estado;
	}

	public String getJustificacion() {
		return justificacion;
	}

}
