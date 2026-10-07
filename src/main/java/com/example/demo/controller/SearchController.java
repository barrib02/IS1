package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dto.SearchResultDTO;

@RestController
public class SearchController {
	
@GetMapping("/search")
public List<SearchResultDTO> buscar() {

List<SearchResultDTO> resultados = new ArrayList<>();

resultados.add(
new SearchResultDTO(
1L,
"Harry Potter",
"LIBRO"));

resultados.add(
new SearchResultDTO(
2L,
"Star Wars",
"PELICULA"));

return resultados;
}
}