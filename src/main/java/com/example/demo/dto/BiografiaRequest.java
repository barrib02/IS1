package com.example.demo.dto;

import jakarta.validation.constraints.Size;

public class BiografiaRequest {
    @Size(max = 500, message = "La biografía no puede superar los 500 caracteres")
    private String biografia;

    public String getBiografia() {
    	return this.biografia;
    }
    
    public void setBiografia(String bio) {
    	this.biografia=bio;
    }
    
}

