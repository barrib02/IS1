package com.example.demo.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "obras")
public class Obra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// API de origen de la obra: TMDB, GOOGLE_BOOKS o RAWG.
	// Es null para las obras registradas manualmente.
	@Column(name = "fuente_api")
	private String fuenteApi;

	// Identificador original de la obra en su API.
	// Se almacena como String para admitir IDs de distintas APIs.
	@Column(name = "id_externo")
	private String idExterno;

	// URL de la portada, utilizada principalmente para obras manuales.
	// Las obras externas obtendrán sus portadas directamente de la API.
	@Column(name = "imagen_url", columnDefinition = "TEXT")
	private String imagenUrl;

	private String titulo;
	private String autor;

	private Long autorId;

	@Column(columnDefinition = "TEXT")
	private String descripcion;

	@Enumerated(EnumType.STRING)
	private TipoObra tipo;

	// Año de publicación o estreno
	private Integer anio;

	@ManyToOne
	@JoinColumn(name = "creador_id")
	private Creador creador;

	@ElementCollection(targetClass = GeneroObra.class)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "obra_generos", joinColumns = @JoinColumn(name = "obra_id"))
	@Column(name = "genero")
	private Set<GeneroObra> generos = new HashSet<>();

	@Column(nullable = false)
	private boolean validada = false;

	@Enumerated(EnumType.STRING)
	private EstadoValidacion estadoValidacion = EstadoValidacion.PENDIENTE;

	public Obra() {
	}

	public Obra(Long id, String titulo, String autor, String descripcion, TipoObra tipo, Integer anio,
			Set<GeneroObra> generos) {
		this.id = id;
		this.titulo = titulo;
		this.autor = autor;
		this.descripcion = descripcion;
		this.tipo = tipo;
		this.generos = generos;
		this.anio = anio;
	}

	public EstadoValidacion getEstadoValidacion() {
		return estadoValidacion;
	}

	public void setEstadoValidacion(EstadoValidacion estadoValidacion) {

		this.estadoValidacion = estadoValidacion;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public TipoObra getTipo() {
		return tipo;
	}

	public void setTipo(TipoObra tipo) {
		this.tipo = tipo;
	}

	public Set<GeneroObra> getGeneros() {
		return generos;
	}

	public void setGeneros(Set<GeneroObra> generos) {
		this.generos = generos;
	}

	public Creador getCreador() { // Para asociar una obra a un creador
		return creador;
	}

	public void setCreador(Creador creador) { // Para asociar una obra a un creador
		this.creador = creador;
	}

	public Integer getAnio() {
		return anio;
	}

	public void setAnio(Integer anio) {
		this.anio = anio;
	}

	public boolean isValidada() {
		return validada;
	}

	public void setValidada(boolean validada) {
		this.validada = validada;
	}

	public Long getAutorId() {
		return autorId;
	}

	public void setAutorId(Long autorId) {
		this.autorId = autorId;
	}

	// Motivo escrito por el moderador al validar o rechazar.
	@Column(length = 1000)
	private String justificacionModeracion;

	@JsonIgnore
	public String getJustificacionModeracion() {
		return justificacionModeracion;
	}

	public void setJustificacionModeracion(String justificacionModeracion) {
		this.justificacionModeracion = justificacionModeracion;
	}

	// Obtiene la API de origen de la obra.
	public String getFuenteApi() {
		return fuenteApi;
	}

	public void setFuenteApi(String fuenteApi) {
		this.fuenteApi = fuenteApi;
	}

	// Obtiene el identificador original de la obra en la API.
	public String getIdExterno() {
		return idExterno;
	}

	public void setIdExterno(String idExterno) {
		this.idExterno = idExterno;
	}

	// Obtiene la URL de la portada.
	public String getImagenUrl() {
		return imagenUrl;
	}

	public void setImagenUrl(String imagenUrl) {
		this.imagenUrl = imagenUrl;
	}

}
