
package com.example.demo.controller;

import com.example.demo.service.GoogleBooksService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/googlebooks")
public class GoogleBooksController {

    private final GoogleBooksService googleBooksService;

    public GoogleBooksController(GoogleBooksService googleBooksService) {
        this.googleBooksService = googleBooksService;
    }

    // Busca libros por título.
    @GetMapping(value = "/buscar", produces = MediaType.APPLICATION_JSON_VALUE)
    public String buscarLibros(@RequestParam String titulo) {
        return googleBooksService.buscarLibros(titulo);
    }

    // Obtiene la ficha de un libro por su identificador externo.
    @GetMapping(value = "/libro/{idExterno}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String obtenerLibro(@PathVariable String idExterno) {
        return googleBooksService.obtenerLibro(idExterno);
    }
}
