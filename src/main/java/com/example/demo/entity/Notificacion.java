package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

// Aviso para un usuario (por ejemplo: "tu obra ha sido rechazada").
// Hibernate creará la tabla "notificaciones" automáticamente al arrancar.
@Entity
@Table(name = "notificaciones")
public class Notificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Usuario que RECIBE la notificación.
	@Column(name = "usuario_id", nullable = false)
	private Long usuarioId;

	// Obra relacionada (puede ser null en otros tipos de aviso).
	@Column(name = "obra_id")
	private Long obraId;

	// Tipo de aviso: OBRA_VALIDADA, OBRA_RECHAZADA...
	@Column(nullable = false, length = 50)
	private String tipo;

	@Column(nullable = false, length = 500)
	private String mensaje;

	// false = todavía no la ha visto (cuenta en el globo rojo de la campana).
	@Column(nullable = false)
	private boolean leida = false;

	@Column(nullable = false)
	private LocalDateTime fecha;
	
	// Usuario que provoca el aviso (por ejemplo, quien envía una solicitud).
	@Column(name = "emisor_id")
	private Long emisorId;

	public Long getEmisorId() { return emisorId; }
	public void setEmisorId(Long emisorId) { this.emisorId = emisorId; }

	// Constructor vacío necesario para JPA.
	public Notificacion() {
	}

	public Notificacion(Long usuarioId, Long obraId, String tipo, String mensaje) {
		this.usuarioId = usuarioId;
		this.obraId = obraId;
		this.tipo = tipo;
		this.mensaje = mensaje;
		this.leida = false;
		this.fecha = LocalDateTime.now();
	}

	public Long getId() { return id; }

	public Long getUsuarioId() { return usuarioId; }
	public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

	public Long getObraId() { return obraId; }
	public void setObraId(Long obraId) { this.obraId = obraId; }

	public String getTipo() { return tipo; }
	public void setTipo(String tipo) { this.tipo = tipo; }

	public String getMensaje() { return mensaje; }
	public void setMensaje(String mensaje) { this.mensaje = mensaje; }

	public boolean isLeida() { return leida; }
	public void setLeida(boolean leida) { this.leida = leida; }

	public LocalDateTime getFecha() { return fecha; }
	public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
