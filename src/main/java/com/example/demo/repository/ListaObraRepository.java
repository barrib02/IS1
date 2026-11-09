package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ListaObra;

public interface ListaObraRepository extends JpaRepository<ListaObra, Long> {

	List<ListaObra> findByListaId(Long listaId);

	long countByListaId(Long listaId);

	boolean existsByListaIdAndObraId(Long listaId, Long obraId);

	void deleteByListaIdAndObraId(Long listaId, Long obraId);

	void deleteByListaId(Long listaId);
}