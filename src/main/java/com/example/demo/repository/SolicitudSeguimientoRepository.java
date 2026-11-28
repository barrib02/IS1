package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.SolicitudSeguimiento;

public interface SolicitudSeguimientoRepository extends JpaRepository<SolicitudSeguimiento, Long> {

	Optional<SolicitudSeguimiento> findBySolicitanteIdAndDestinatarioId(Long solicitanteId, Long destinatarioId);

	boolean existsBySolicitanteIdAndDestinatarioId(Long solicitanteId, Long destinatarioId);

	List<SolicitudSeguimiento> findByDestinatarioIdOrderByFechaDesc(Long destinatarioId);
}
