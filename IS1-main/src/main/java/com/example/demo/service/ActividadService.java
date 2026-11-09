
package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Actividad;
import com.example.demo.repository.ActividadRepository;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;

    public ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    // Registrar una nueva actividad del usuario.
    public Actividad registrarActividad(
            Long usuarioId,
            Long obraId,
            String tipo,
            String descripcion) {

        Actividad actividad = new Actividad(
                usuarioId, obraId, tipo, descripcion);

        return actividadRepository.save(actividad);
    }

    // Consultar las actividades de un usuario,
    // ordenadas de más reciente a más antigua.
    public List<Actividad> obtenerActividadReciente(Long usuarioId) {

        return actividadRepository
                .findByUsuarioIdOrderByFechaDesc(usuarioId);
    }
}
