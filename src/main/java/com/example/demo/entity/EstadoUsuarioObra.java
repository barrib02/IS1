package com.example.demo.entity;

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

}