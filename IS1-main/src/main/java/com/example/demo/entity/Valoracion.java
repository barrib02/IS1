
package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "valoraciones")
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long obraId;
    private Integer puntuacion;
    private Long usuarioId;

    // Constructor vacío necesario para JPA
    public Valoracion() {
    }

    // Constructor con parámetros
    public Valoracion(Long id, Long obraId, Integer puntuacion, Long usuario) {
        this.id = id;
        this.obraId = obraId;
        this.puntuacion = puntuacion;
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

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }
    
    public Long getUsuarioId() {
    	return this.usuarioId;
    }
    
    public void setUsuarioId(Long usuario) {
    	this.usuarioId=usuario;
    }
    
}
