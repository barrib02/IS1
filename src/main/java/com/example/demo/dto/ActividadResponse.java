package com.example.demo.dto;

import java.time.LocalDateTime;

public record ActividadResponse(Long id, Long usuarioId, Long obraId, String tituloObra, String tipoObra, String tipo,
		String descripcion, LocalDateTime fecha) {
}