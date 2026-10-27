package com.example.demo.repository;

import com.example.demo.entity.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    
    Optional<Valoracion> findFirstByObraIdOrderByIdDesc(Long obraId);
    
    Optional<Valoracion> findByUsuarioIdAndObraId(Long usuarioId, Long obraId);
}