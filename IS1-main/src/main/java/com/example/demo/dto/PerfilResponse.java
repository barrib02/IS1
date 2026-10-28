package com.example.demo.dto;

public class PerfilResponse {
    private Long id;
    private String nombre;
    private String biografia;
    
    public PerfilResponse(Long id, String nombre, String biografia) {
        this.id = id;
        this.nombre = nombre;
        this.biografia = biografia;
    }
    
    public Long getId(){
    	return this.id;
    }
    
    public void setId(Long Id) {
    	this.id=Id;
    }
    
    public String getNombre() {
    	return this.nombre;
    }
    
    public void setNombre(String nom) {
    	this.nombre=nom;
    }
    
    public String getBiografia() {
    	return this.biografia;
    }
    
    public void setBiografia(String bio) {
    	this.biografia=bio;
    }
    
    
    
}
