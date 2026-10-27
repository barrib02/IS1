package com.example.demo.repository;

import com.example.demo.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    
    Optional<Resena> findByValoracionId(Long valoracionId);
    
    Optional<Resena> findByUsuarioIdAndObraId(Long usuarioId, Long obraId);
}