package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Notificacion;

public interface NotificacionSeguimientoRepository extends JpaRepository<Notificacion, Long> {

	List<Notificacion> findByUsuarioIdAndEmisorIdAndTipo(Long usuarioId, Long emisorId, String tipo);
}
