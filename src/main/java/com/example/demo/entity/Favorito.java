package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "favoritos", uniqueConstraints = @UniqueConstraint(columnNames = { "usuario_id", "obra_id" }))
public class Favorito {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id", nullable = false)
	private Long usuarioId;

	@Column(name = "obra_id", nullable = false)
	private Long obraId;

	// Copia del tipo de la obra, para contar favoritos por categoría.
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoObra tipo;

	// Posición dentro de la categoría (1 a 5).
	@Column(nullable = false)
	private Integer posicion;

	public Favorito() {
	}

	public Favorito(Long usuarioId, Long obraId, TipoObra tipo, Integer posicion) {
		this.usuarioId = usuarioId;
		this.obraId = obraId;
		this.tipo = tipo;
		this.posicion = posicion;
	}

	public Long getId() {
		return id;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public Long getObraId() {
		return obraId;
	}

	public void setObraId(Long obraId) {
		this.obraId = obraId;
	}

	public TipoObra getTipo() {
		return tipo;
	}

	public void setTipo(TipoObra tipo) {
		this.tipo = tipo;
	}

	public Integer getPosicion() {
		return posicion;
	}

	public void setPosicion(Integer posicion) {
		this.posicion = posicion;
	}
}