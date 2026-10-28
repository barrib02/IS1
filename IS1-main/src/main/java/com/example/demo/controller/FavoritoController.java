package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.FavoritoResponse;
import com.example.demo.entity.TipoObra;
import com.example.demo.service.FavoritoService;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    // Mis favoritos (ordenados por tipo y posición).
    @GetMapping
    public List<FavoritoResponse> misFavoritos(Authentication auth) {
        return favoritoService.misFavoritos(auth.getName());
    }

    // Fijar una obra como favorita.
    @PostMapping("/{obraId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoritoResponse anadir(
            @PathVariable Long obraId,
            Authentication auth) {

        return favoritoService.anadir(auth.getName(), obraId);
    }

    // Quitar un favorito.
    @DeleteMapping("/{obraId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(
            @PathVariable Long obraId,
            Authentication auth) {

        favoritoService.quitar(auth.getName(), obraId);
    }

    // Sustituir un favorito por otra obra del mismo tipo.
    @PutMapping("/{obraId}/sustituir/{nuevaObraId}")
    public FavoritoResponse sustituir(
            @PathVariable Long obraId,
            @PathVariable Long nuevaObraId,
            Authentication auth) {

        return favoritoService.sustituir(
                auth.getName(), obraId, nuevaObraId);
    }

    // Reordenar los favoritos de una categoría.
    // Ejemplo: PUT /api/favoritos/orden?tipo=LIBRO
    // Body: [3, 1, 2]  (ids de las obras en el nuevo orden)
    @PutMapping("/orden")
    public List<FavoritoResponse> reordenar(
            @RequestParam TipoObra tipo,
            @RequestBody List<Long> obraIds,
            Authentication auth) {

        return favoritoService.reordenar(auth.getName(), tipo, obraIds);
    }

    // Errores de negocio.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarError(IllegalArgumentException error) {
        return error.getMessage();
    }
}