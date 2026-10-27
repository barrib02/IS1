package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.EstadoObra;
import com.example.demo.service.EstadoUsuarioObraService;

@RestController
public class EstadoUsuarioObraController {

    private final EstadoUsuarioObraService service;

    public EstadoUsuarioObraController(
            EstadoUsuarioObraService service) {

        this.service = service;
    }

    @PutMapping("/users/{usuarioId}/works/{obraId}/estado")
    public String cambiarEstado(
            @PathVariable Long usuarioId,
            @PathVariable Long obraId,
            @RequestParam String estado) {

        service.cambiarEstado(
                usuarioId,
                obraId,
                EstadoObra.valueOf(estado));

        return "Estado actualizado";
    }

    @GetMapping("/users/{usuarioId}/works/{obraId}/estado")
    public EstadoObra obtenerEstado(
            @PathVariable Long usuarioId,
            @PathVariable Long obraId) {

        return service.obtenerEstado(usuarioId, obraId);
    }
}