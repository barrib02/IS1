package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Seguimiento;
import com.example.demo.entity.Usuario;

public interface SeguimientoRepository
        extends JpaRepository<Seguimiento, Long> {

    boolean existsBySeguidorAndSeguido(
            Usuario seguidor,
            Usuario seguido);

    Optional<Seguimiento>
    findBySeguidorAndSeguido(
            Usuario seguidor,
            Usuario seguido);

    List<Seguimiento>
    findBySeguido(Usuario seguido);

    List<Seguimiento>
    findBySeguidor(Usuario seguidor);

}