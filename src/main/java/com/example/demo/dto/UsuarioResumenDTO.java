package com.example.demo.dto;

public class UsuarioResumenDTO {

    private Long id;
    private String username;

    public UsuarioResumenDTO(
            Long id,
            String username) {

        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}