
package com.example.demo.service;

import com.example.demo.entity.Resena;
import com.example.demo.repository.ResenaRepository;
import org.springframework.stereotype.Service;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;

    public ResenaService(ResenaRepository resenaRepository) {
        this.resenaRepository = resenaRepository;
    }

    // Crear una reseña
    public Resena crearResena(Resena resena) {
        validarTexto(resena.getTexto());
        resena.setId(null);
        return resenaRepository.save(resena);
    }

    // Editar una reseña
    public Resena editarResena(Long id, String texto) {
        validarTexto(texto);

        Resena resena = resenaRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Reseña no encontrada"));

        resena.setTexto(texto);
        return resenaRepository.save(resena);
    }

    // Eliminar una reseña
    public void eliminarResena(Long id) {
        if (!resenaRepository.existsById(id)) {
            throw new IllegalArgumentException("Reseña no encontrada");
        }

        resenaRepository.deleteById(id);
    }

    // Validar el límite de 200 palabras
    private void validarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "La reseña no puede estar vacía");
        }

        String[] palabras = texto.trim().split("\\s+");

        if (palabras.length > 200) {
            throw new IllegalArgumentException(
                "La reseña no puede superar las 200 palabras");
        }
    }
}
