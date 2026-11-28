package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

// Solicitud de seguimiento pendiente hacia un perfil "solo amigos".
// Cuando se acepta se borra y se crea el Seguimiento real.
@Entity
@Table(name = "solicitudes_seguimiento", uniqueConstraints = @UniqueConstraint(columnNames = { "solicitante_id",
		"destinatario_id" }))
public class SolicitudSeguimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "solicitante_id", nullable = false)
	private Long solicitanteId;

	@Column(name = "destinatario_id", nullable = false)
	private Long destinatarioId;

	@Column(nullable = false)
	private LocalDateTime fecha = LocalDateTime.now();

	public SolicitudSeguimiento() {
	}

	public SolicitudSeguimiento(Long solicitanteId, Long destinatarioId) {
		this.solicitanteId = solicitanteId;
		this.destinatarioId = destinatarioId;
		this.fecha = LocalDateTime.now();
	}

	public Long getId() { return id; }
	public Long getSolicitanteId() { return solicitanteId; }
	public Long getDestinatarioId() { return destinatarioId; }
	public LocalDateTime getFecha() { return fecha; }
}
