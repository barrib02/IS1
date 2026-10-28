package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "lista_obras",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"lista_id", "obra_id"}))
public class ListaObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lista_id", nullable = false)
    private Long listaId;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    public ListaObra() {
    }

    public ListaObra(Long listaId, Long obraId) {
        this.listaId = listaId;
        this.obraId = obraId;
    }

    public Long getId() {
        return id;
    }

    public Long getListaId() {
        return listaId;
    }

    public void setListaId(Long listaId) {
        this.listaId = listaId;
    }

    public Long getObraId() {
        return obraId;
    }

    public void setObraId(Long obraId) {
        this.obraId = obraId;
    }
}