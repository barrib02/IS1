package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public enum PeriodoEstadistica {
	SEMANA(7), MES(30), ANIO(365), TODO(0);

	private final int dias;

	PeriodoEstadistica(int dias) {
		this.dias = dias;
	}

	public boolean esTodo() {
		return this == TODO;
	}

	// Inicio del periodo (últimos N días). Para TODO devuelve una fecha muy
	// antigua.
	public LocalDateTime desde() {
		return esTodo() ? LocalDateTime.of(1970, 1, 1, 0, 0) : LocalDate.now().minusDays(dias).atStartOfDay();
	}
}
