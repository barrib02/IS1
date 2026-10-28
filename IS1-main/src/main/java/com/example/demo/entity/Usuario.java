
package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

	// Identificador único del usuario.
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Nombre del usuario.
	@Column(nullable = false)
	private String nombre;

	// Correo electrónico único.
	@Column(nullable = false, unique = true)
	private String email;

	// Contraseña cifrada con BCrypt.
	@Column(nullable = false)
	private String password;

	// H6.1 - Indica si el correo está verificado.
	@Column(nullable = false)
	private boolean emailVerificado = false;

	// H6.1 - Token para verificar el correo.
	@Column(unique = true)
	private String tokenVerificacion;

	// H6.1 - Fecha de caducidad del token.
	@Column(name = "token_expiracion")
	private LocalDateTime tokenExpiracion;

	@Column(length = 500)
	private String biografia;

	// Constructor vacío necesario para JPA.
	public Usuario() {
	}

	// Constructor para registrar usuarios.
	public Usuario(String nombre, String email, String password) {
		this.nombre = nombre;
		this.email = email;
		this.password = password;
		this.emailVerificado = false;
	}

	// GETTERS Y SETTERS

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEmailVerificado() {
		return emailVerificado;
	}

	public void setEmailVerificado(boolean emailVerificado) {
		this.emailVerificado = emailVerificado;
	}

	public String getTokenVerificacion() {
		return tokenVerificacion;
	}

	public void setTokenVerificacion(String tokenVerificacion) {
		this.tokenVerificacion = tokenVerificacion;
	}

	public LocalDateTime getTokenExpiracion() {
		return tokenExpiracion;
	}

	public void setTokenExpiracion(LocalDateTime tokenExpiracion) {
		this.tokenExpiracion = tokenExpiracion;
	}

	public String getBiografia() {
		return this.biografia;
	}

	public void setBiografia(String bio) {
		this.biografia = bio;
	}

}
