package com.example.demo.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ArchivoExportado;
import com.example.demo.service.ExportacionService;

@RestController
@RequestMapping("/api/exportar")
public class ExportacionController {

	private final ExportacionService exportacionService;

	public ExportacionController(ExportacionService exportacionService) {
		this.exportacionService = exportacionService;
	}

	// GET /api/exportar/listas?formato=csv|json
	@GetMapping("/listas")
	public ResponseEntity<byte[]> listas(@RequestParam(defaultValue = "csv") String formato, Authentication auth) {
		return descarga(exportacionService.exportarListas(auth.getName(), formato));
	}

	@GetMapping("/valoraciones")
	public ResponseEntity<byte[]> valoraciones(@RequestParam(defaultValue = "csv") String formato,
			Authentication auth) {
		return descarga(exportacionService.exportarValoraciones(auth.getName(), formato));
	}

	@GetMapping("/resenas")
	public ResponseEntity<byte[]> resenas(@RequestParam(defaultValue = "csv") String formato, Authentication auth) {
		return descarga(exportacionService.exportarResenas(auth.getName(), formato));
	}

	// "attachment" hace que el navegador lo descargue en vez de mostrarlo.
	private ResponseEntity<byte[]> descarga(ArchivoExportado archivo) {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(archivo.nombre()).build().toString())
				.contentType(MediaType.parseMediaType(archivo.contentType())).body(archivo.contenido());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}
}