package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Seguimiento;

// Consultas sobre la tabla "seguimientos" (cada fila = un seguimiento ya aceptado).
public interface SeguimientoConsultasRepository extends JpaRepository<Seguimiento, Long> {

	boolean existsBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

	Optional<Seguimiento> findBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

	long countBySeguidoId(Long seguidoId);

	long countBySeguidorId(Long seguidorId);

	List<Seguimiento> findBySeguidoId(Long seguidoId);

	List<Seguimiento> findBySeguidorId(Long seguidorId);
}
