package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.Obra;
import com.example.demo.repository.ObraRepository;

@Service
public class ObraService {

    @Autowired
    private ObraRepository obraRepository;

    public List<Obra> getObras() {
        return obraRepository.findAll();
    }

    public List<Obra> buscarPorTitulo(String texto) {
        return obraRepository
                .findByTituloContainingIgnoreCase(texto);
    }

    public EstadoObra obtenerEstado(Long id) {

        Obra obra =
            obraRepository.findById(id)
            .orElse(null);

        if (obra == null) {
            return null;
        }

        return obra.getEstado();
    }

    public boolean cambiarEstado(
            Long id,
            EstadoObra nuevoEstado) {

        Obra obra =
            obraRepository.findById(id)
            .orElse(null);

        if (obra == null) {
            return false;
        }

        obra.setEstado(nuevoEstado);

        obraRepository.save(obra);

        return true;
    }
}