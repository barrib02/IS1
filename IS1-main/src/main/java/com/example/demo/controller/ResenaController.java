
package com.example.demo.controller;

import com.example.demo.entity.Resena;
import com.example.demo.service.ResenaService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    // Crear reseña
    @PostMapping
    public Resena crearResena(@RequestBody Resena resena) {
        return resenaService.crearResena(resena);
    }

    // Editar reseña
    @PutMapping("/{id}")
    public Resena editarResena(
            @PathVariable Long id,
            @RequestBody Resena resena) {

        return resenaService.editarResena(
                id, resena.getTexto());
    }

    // Eliminar reseña
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarResena(@PathVariable Long id) {
        resenaService.eliminarResena(id);
    }

    // Gestionar errores de validación
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarError(IllegalArgumentException error) {
        return error.getMessage();
    }
 // Consultar la reseña asociada a una valoración
    @GetMapping("/valoracion/{valoracionId}")
    public ResponseEntity<Resena> obtenerResenaPorValoracion(
            @PathVariable Long valoracionId) {

        Resena resena = resenaService.obtenerResenaPorValoracion(valoracionId);

        if (resena == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resena);
    }
    
    @GetMapping("/obra/{obraId}/usuario/{usuarioId}")
    public ResponseEntity<Resena> obtenerResenaPorUsuarioYObra(
            @PathVariable Long obraId, 
            @PathVariable Long usuarioId) {
        
        Resena resena = resenaService.obtenerResenaPorUsuarioYObra(usuarioId, obraId);
        if (resena == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resena);
    }
}
