
package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.EstadoValidacion;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;
import com.example.demo.repository.ObraRepository;

@Service
public class ObraExternaPersistenciaService {

    private final ObraRepository obraRepository;

    public ObraExternaPersistenciaService(
            ObraRepository obraRepository) {
        this.obraRepository = obraRepository;
    }

    // Guarda una referencia externa en una transacción independiente.
    // Si Neon rechaza un duplicado, esta transacción se revierte
    // sin afectar a una posible consulta posterior.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Obra guardarReferencia(
            String fuenteApi,
            String idExterno,
            TipoObra tipo,
            String titulo) {

        Obra obra = new Obra();

        obra.setFuenteApi(fuenteApi);
        obra.setIdExterno(idExterno);
        obra.setTipo(tipo);
        obra.setTitulo(titulo.trim());

        // Las referencias procedentes de APIs no requieren
        // la moderación de las obras enviadas manualmente.
        obra.setValidada(true);
        obra.setEstadoValidacion(EstadoValidacion.VALIDADA);

        // Fuerza la inserción antes de finalizar el método.
        return obraRepository.saveAndFlush(obra);
    }
}
