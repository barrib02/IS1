
package com.example.demo.controller;

import com.example.demo.entity.Valoracion;
import com.example.demo.service.ValoracionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/valoraciones")
public class ValoracionController {

    private final ValoracionService valoracionService;

    public ValoracionController(ValoracionService valoracionService) {
        this.valoracionService = valoracionService;
    }
    
 // Buscar la valoración más reciente de una obra
    @GetMapping("/obra/{obraId}")
    public Valoracion obtenerValoracionPorObra(@PathVariable Long obraId) {
        return valoracionService.obtenerValoracionPorObra(obraId);
    }
    

    // Crear valoración
    @PostMapping
    public Valoracion crearValoracion(@RequestBody Valoracion valoracion) {
        return valoracionService.crearValoracion(valoracion);
    }

 // Consultar una valoración por su ID
    @GetMapping("/{id}")
    public Valoracion obtenerValoracion(@PathVariable Long id) {
        return valoracionService.obtenerValoracion(id);
    }
    
    // Editar valoración
    @PutMapping("/{id}")
    public Valoracion editarValoracion(
            @PathVariable Long id,
            @RequestBody Valoracion valoracion) {

        return valoracionService.editarValoracion(
                id, valoracion.getPuntuacion());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarError(IllegalArgumentException error) {
        return error.getMessage();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarValoracion(@PathVariable Long id) {
        valoracionService.eliminarValoracion(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/obra/{obraId}/usuario/{usuarioId}")
    public ResponseEntity<Valoracion> obtenerValoracionPorUsuarioYObra(
            @PathVariable Long obraId, 
            @PathVariable Long usuarioId) {
        
        Valoracion valoracion = valoracionService.obtenerValoracionPorUsuarioYObra(usuarioId, obraId);
        if (valoracion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(valoracion);
    }
}
