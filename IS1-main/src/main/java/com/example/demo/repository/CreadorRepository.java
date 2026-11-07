package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Creador;

public interface CreadorRepository extends JpaRepository<Creador, Long> {

}