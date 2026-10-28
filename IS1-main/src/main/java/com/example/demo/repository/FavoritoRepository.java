package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Favorito;
import com.example.demo.entity.TipoObra;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioIdOrderByTipoAscPosicionAsc(Long usuarioId);

    List<Favorito> findByUsuarioIdAndTipoOrderByPosicionAsc(
            Long usuarioId, TipoObra tipo);

    long countByUsuarioIdAndTipo(Long usuarioId, TipoObra tipo);

    Optional<Favorito> findByUsuarioIdAndObraId(Long usuarioId, Long obraId);
}