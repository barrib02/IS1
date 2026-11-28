package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seguimientos")
public class Seguimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "seguidor_id")
	private Usuario seguidor;

	@ManyToOne
	@JoinColumn(name = "seguido_id")
	private Usuario seguido;

	public Seguimiento() {
	}

	public Seguimiento(Usuario seguidor, Usuario seguido) {
		this.seguidor = seguidor;
		this.seguido = seguido;
	}

	public Long getId() {
		return id;
	}

	public Usuario getSeguidor() {
		return seguidor;
	}

	public void setSeguidor(Usuario seguidor) {
		this.seguidor = seguidor;
	}

	public Usuario getSeguido() {
		return seguido;
	}

	public void setSeguido(Usuario seguido) {
		this.seguido = seguido;
	}
}