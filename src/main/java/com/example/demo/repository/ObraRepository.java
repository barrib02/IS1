package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Obra;

public interface ObraRepository extends JpaRepository<Obra, Long> {

    List<Obra> findByTituloContainingIgnoreCase(String titulo);

}