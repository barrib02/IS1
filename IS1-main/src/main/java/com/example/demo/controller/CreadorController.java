package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Creador;
import com.example.demo.service.CreadorService;

@RestController
public class CreadorController {

    private final CreadorService creadorService;

    public CreadorController(
            CreadorService creadorService) {

        this.creadorService = creadorService;
    }

    @GetMapping("/creador/{id}")
    public ResponseEntity<Creador> obtenerCreador(
            @PathVariable Long id) {

        Creador creador =
                creadorService.obtenerCreador(id);

        return ResponseEntity.ok(creador);
    }
}