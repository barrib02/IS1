
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Actividad;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    // Obtener las actividades de un usuario,
    // ordenadas desde la más reciente.
    List<Actividad> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

}
