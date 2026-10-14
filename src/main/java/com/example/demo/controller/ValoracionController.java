
package com.example.demo.controller;

import com.example.demo.entity.Valoracion;
import com.example.demo.service.ValoracionService;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/valoraciones")
public class ValoracionController {

    private final ValoracionService valoracionService;

    public ValoracionController(ValoracionService valoracionService) {
        this.valoracionService = valoracionService;
    }

    // Crear valoración
    @PostMapping
    public Valoracion crearValoracion(@RequestBody Valoracion valoracion) {
        return valoracionService.crearValoracion(valoracion);
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
}
