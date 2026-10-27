package com.example.demo.repository;

import com.example.demo.entity.EstadoUsuarioObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EstadoUsuarioObraRepository extends JpaRepository<EstadoUsuarioObra, Long> {
    
    Optional<EstadoUsuarioObra> findByUsuarioIdAndObraId(Long usuarioId, Long obraId);
}