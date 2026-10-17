package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.EstadoUsuarioObra;

public interface EstadoUsuarioObraRepository extends JpaRepository<EstadoUsuarioObra, Long> {

    Optional<EstadoUsuarioObra> findByUsuarioIdAndObraId(Long usuarioId, Long obraId);

}