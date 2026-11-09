
package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long obraId;
    private Long valoracionId;
    private Long usuarioId;

    @Column(columnDefinition = "TEXT")
    private String texto;

    // Constructor vacío necesario para JPA
    public Resena() {
    }

    // Constructor con parámetros
    public Resena(Long id, Long obraId, String texto, Long usuario) {
        this.id = id;
        this.obraId = obraId;
        this.texto = texto;
        this.usuarioId=usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObraId() {
        return obraId;
    }

    public void setObraId(Long obraId) {
        this.obraId = obraId;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
    
    public Long getValoracionId() {
        return valoracionId;
    }

    public void setValoracionId(Long valoracionId) {
        this.valoracionId = valoracionId;
    }
    public Long getUsuarioId() {
    	return this.usuarioId;
    }
    
    public void setUsuarioId(Long usuario) {
    	this.usuarioId=usuario;
    }
    
}
