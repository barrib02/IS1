package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "estado_usuario_obra")
public class EstadoUsuarioObra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long usuarioId;

	private Long obraId;

	@Enumerated(EnumType.STRING)
	private EstadoObra estado;

	public EstadoUsuarioObra() {
	}

	public EstadoUsuarioObra(Long usuarioId, Long obraId, EstadoObra estado) {

		this.usuarioId = usuarioId;
		this.obraId = obraId;
		this.estado = estado;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(long usuario) {
		this.usuarioId = usuario;
	}

	public Long getObraId() {
		return this.obraId;
	}

	public void setObraId(long obra) {
		this.obraId = obra;
	}

	public EstadoObra getEstado() {
		return this.estado;
	}

	public void setEstado(EstadoObra estadoObra) {
		this.estado = estadoObra;
	}

	@Column(name = "paginas_leidas")
	private Integer paginasLeidas;

	@Column(name = "temporada_actual")
	private Integer temporadaActual;

	@Column(name = "capitulo_actual")
	private Integer capituloActual;

	@Column(name = "fecha_actualizacion")
	private LocalDateTime fechaActualizacion;

	// Se rellena al pasar a COMPLETADA. Sirve para las estadísticas por periodo.
	@Column(name = "fecha_completada")
	private LocalDateTime fechaCompletada;

	public Integer getPaginasLeidas() {
		return paginasLeidas;
	}

	public void setPaginasLeidas(Integer paginasLeidas) {
		this.paginasLeidas = paginasLeidas;
	}

	public Integer getTemporadaActual() {
		return temporadaActual;
	}

	public void setTemporadaActual(Integer temporadaActual) {
		this.temporadaActual = temporadaActual;
	}

	public Integer getCapituloActual() {
		return capituloActual;
	}

	public void setCapituloActual(Integer capituloActual) {
		this.capituloActual = capituloActual;
	}

	public LocalDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(LocalDateTime f) {
		this.fechaActualizacion = f;
	}

	public LocalDateTime getFechaCompletada() {
		return fechaCompletada;
	}

	public void setFechaCompletada(LocalDateTime f) {
		this.fechaCompletada = f;
	}

}