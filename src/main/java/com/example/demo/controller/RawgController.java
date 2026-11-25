package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.RawgService;

@RestController
@RequestMapping("/api/rawg")
public class RawgController {

    private final RawgService rawgService;

    public RawgController(RawgService rawgService) {
        this.rawgService = rawgService;
    }

    @GetMapping(
        value = "/buscar",
        produces = "application/json"
    )
    public String buscarVideojuegos(
            @RequestParam("titulo") String titulo) {

        return rawgService.buscarVideojuegos(titulo);
    }

    @GetMapping(
        value = "/videojuego/{idExterno}",
        produces = "application/json"
    )
    public String obtenerVideojuego(
            @PathVariable("idExterno") String idExterno) {

        return rawgService.obtenerVideojuego(idExterno);
    }

    @GetMapping(
        value = "/videojuego/{idExterno}/tiendas",
        produces = "application/json"
    )
    public String obtenerTiendasVideojuego(
            @PathVariable("idExterno") String idExterno) {

        return rawgService.obtenerTiendasVideojuego(idExterno);
    }
}