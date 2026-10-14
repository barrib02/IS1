
package com.example.demo.repository;

import com.example.demo.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResenaRepository
        extends JpaRepository<Resena, Long> {
	java.util.Optional<Resena> findByValoracionId(Long valoracionId);
}
