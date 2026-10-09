package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Obra;
import com.example.demo.service.ObraService;

@RestController
public class SearchController {

	@Autowired
	private ObraService obraService;

	@GetMapping("/search")
	public List<Obra> buscar(@RequestParam String titulo) {

		return obraService.buscarPorTitulo(titulo);
	}
}