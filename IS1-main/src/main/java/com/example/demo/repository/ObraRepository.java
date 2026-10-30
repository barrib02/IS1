package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Obra;

import com.example.demo.entity.TipoObra;

public interface ObraRepository extends JpaRepository<Obra, Long> {

	List<Obra> findByTituloContainingIgnoreCase(String titulo);

	List<Obra> findByTipo(TipoObra tipo);

	List<Obra> findByTituloContainingIgnoreCaseAndTipo(String titulo, TipoObra tipo);

	List<Obra> findTop5ByTituloStartingWithIgnoreCase(String titulo);
}