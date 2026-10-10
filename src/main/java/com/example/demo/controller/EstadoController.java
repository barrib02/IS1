package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.entity.EstadoObra;
import com.example.demo.service.ObraService;

@RestController
public class EstadoController {

    @Autowired
    private ObraService obraService;

    @GetMapping("/obras/{id}/estado")
    public EstadoObra obtenerEstado(@PathVariable Long id) {
        return obraService.obtenerEstado(id);
    }
    
    @PutMapping("/obras/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestBody EstadoObra nuevoEstado) {

        boolean actualizado = obraService.cambiarEstado(id, nuevoEstado);

        if (actualizado) {
            return "Estado actualizado";
        }

        return "Obra no encontrada";
    }
}