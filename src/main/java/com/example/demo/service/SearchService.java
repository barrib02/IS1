package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.dto.SearchResultDTO;

public class SearchService {
	public List<SearchResultDTO> BuscarPorTitulo(String texto) {
		List<SearchResultDTO> resultados = new ArrayList<>();
		resultados.add(
		new SearchResultDTO(
		1L,
		"Harry Potter",
		"LIBRO"));
		resultados.add(
		new SearchResultDTO(
		2L,
		"Breaking Bad",
		"SERIE"));
		return resultados;
		}
}
