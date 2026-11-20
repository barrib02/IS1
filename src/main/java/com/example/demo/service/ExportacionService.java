package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ArchivoExportado;
import com.example.demo.entity.Lista;
import com.example.demo.entity.ListaObra;
import com.example.demo.entity.Obra;
import com.example.demo.entity.Resena;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.Valoracion;
import com.example.demo.repository.ListaObraRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.repository.ObraRepository;
import com.example.demo.repository.ResenaRepository;
import com.example.demo.repository.ValoracionRepository;

@Service
public class ExportacionService {

	// Excel en español usa ";" como separador de columnas.
	private static final String SEPARADOR = ";";

	private final PerfilService perfilService;
	private final ListaRepository listaRepository;
	private final ListaObraRepository listaObraRepository;
	private final ValoracionRepository valoracionRepository;
	private final ResenaRepository resenaRepository;
	private final ObraRepository obraRepository;

	// Tabla genérica: de ella salen tanto el CSV como el JSON.
	private record Tabla(List<String> columnas, List<List<String>> filas) {
	}

	public ExportacionService(PerfilService perfilService, ListaRepository listaRepository,
			ListaObraRepository listaObraRepository, ValoracionRepository valoracionRepository,
			ResenaRepository resenaRepository, ObraRepository obraRepository) {
		this.perfilService = perfilService;
		this.listaRepository = listaRepository;
		this.listaObraRepository = listaObraRepository;
		this.valoracionRepository = valoracionRepository;
		this.resenaRepository = resenaRepository;
		this.obraRepository = obraRepository;
	}

	// ---------- Listas: una fila por obra de cada lista ----------
	@Transactional(readOnly = true)
	public ArchivoExportado exportarListas(String email, String formato) {

		boolean json = esJson(formato);
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		List<Lista> listas = listaRepository.findByUsuarioId(usuario.getId());

		Map<Long, List<ListaObra>> itemsPorLista = new LinkedHashMap<>();
		for (Lista l : listas) {
			itemsPorLista.put(l.getId(), listaObraRepository.findByListaId(l.getId()));
		}

		Set<Long> idsObras = itemsPorLista.values().stream().flatMap(List::stream).map(ListaObra::getObraId)
				.collect(Collectors.toSet());
		Map<Long, Obra> obras = cargarObras(idsObras);

		List<List<String>> filas = new ArrayList<>();
		for (Lista l : listas) {
			String publica = l.isPublica() ? "Sí" : "No";
			List<ListaObra> items = itemsPorLista.get(l.getId());

			if (items.isEmpty()) { // las listas vacías también se exportan
				filas.add(List.of(txt(l.getNombre()), txt(l.getDescripcion()), publica, "", "", ""));
			}
			for (ListaObra item : items) {
				Obra o = obras.get(item.getObraId());
				filas.add(List.of(txt(l.getNombre()), txt(l.getDescripcion()), publica,
						String.valueOf(item.getObraId()), o == null ? "" : txt(o.getTitulo()), tipo(o)));
			}
		}

		return construir("shelfie-listas",
				new Tabla(List.of("lista", "descripcion", "publica", "obraId", "titulo", "tipo"), filas), json);
	}

	// ---------- Valoraciones ----------
	@Transactional(readOnly = true)
	public ArchivoExportado exportarValoraciones(String email, String formato) {

		boolean json = esJson(formato);
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		List<Valoracion> valoraciones = valoracionRepository.findByUsuarioId(usuario.getId());
		Map<Long, Obra> obras = cargarObras(
				valoraciones.stream().map(Valoracion::getObraId).collect(Collectors.toSet()));

		List<List<String>> filas = new ArrayList<>();
		for (Valoracion v : valoraciones) {
			Obra o = obras.get(v.getObraId());
			filas.add(List.of(String.valueOf(v.getObraId()), o == null ? "" : txt(o.getTitulo()), tipo(o),
					String.valueOf(v.getPuntuacion())));
		}

		return construir("shelfie-valoraciones",
				new Tabla(List.of("obraId", "titulo", "tipo", "puntuacion"), filas), json);
	}

	// ---------- Reseñas (con la puntuación de esa obra si existe) ----------
	@Transactional(readOnly = true)
	public ArchivoExportado exportarResenas(String email, String formato) {

		boolean json = esJson(formato);
		Usuario usuario = perfilService.obtenerUsuarioPorEmail(email);

		List<Resena> resenas = resenaRepository.findByUsuarioId(usuario.getId());
		Map<Long, Obra> obras = cargarObras(resenas.stream().map(Resena::getObraId).collect(Collectors.toSet()));

		Map<Long, Integer> puntuaciones = new LinkedHashMap<>();
		for (Valoracion v : valoracionRepository.findByUsuarioId(usuario.getId())) {
			puntuaciones.put(v.getObraId(), v.getPuntuacion());
		}

		List<List<String>> filas = new ArrayList<>();
		for (Resena r : resenas) {
			Obra o = obras.get(r.getObraId());
			Integer punt = puntuaciones.get(r.getObraId());
			filas.add(List.of(String.valueOf(r.getObraId()), o == null ? "" : txt(o.getTitulo()), tipo(o),
					punt == null ? "" : String.valueOf(punt), txt(r.getTexto())));
		}

		return construir("shelfie-resenas",
				new Tabla(List.of("obraId", "titulo", "tipo", "puntuacion", "texto"), filas), json);
	}

	// ---------- auxiliares ----------

	// true = json, false = csv. Cualquier otra cosa es un error (400).
	private boolean esJson(String formato) {
		if (formato == null || "csv".equalsIgnoreCase(formato)) {
			return false;
		}
		if ("json".equalsIgnoreCase(formato)) {
			return true;
		}
		throw new IllegalArgumentException("Formato no válido. Usa csv o json");
	}

	private Map<Long, Obra> cargarObras(Set<Long> ids) {
		return obraRepository.findAllById(ids).stream().collect(Collectors.toMap(Obra::getId, o -> o));
	}

	private String txt(String valor) {
		return valor == null ? "" : valor;
	}

	private String tipo(Obra obra) {
		return obra == null || obra.getTipo() == null ? "" : obra.getTipo().name();
	}

	private ArchivoExportado construir(String base, Tabla tabla, boolean json) {

		String nombre = base + "-" + LocalDate.now();

		if (json) {
			StringBuilder sb = new StringBuilder("[\n");
			for (int i = 0; i < tabla.filas().size(); i++) {
				List<String> fila = tabla.filas().get(i);
				sb.append("  {");
				for (int j = 0; j < tabla.columnas().size(); j++) {
					sb.append(j == 0 ? "\n" : ",\n").append("    ").append(cadenaJson(tabla.columnas().get(j)))
							.append(": ").append(cadenaJson(fila.get(j)));
				}
				sb.append("\n  }").append(i < tabla.filas().size() - 1 ? ",\n" : "\n");
			}
			sb.append("]");
			return new ArchivoExportado(nombre + ".json", "application/json; charset=UTF-8",
					sb.toString().getBytes(StandardCharsets.UTF_8));
		}

		StringBuilder csv = new StringBuilder("\uFEFF"); // BOM: Excel reconoce las tildes
		csv.append(tabla.columnas().stream().map(this::celda).collect(Collectors.joining(SEPARADOR))).append("\r\n");
		for (List<String> fila : tabla.filas()) {
			csv.append(fila.stream().map(this::celda).collect(Collectors.joining(SEPARADOR))).append("\r\n");
		}
		return new ArchivoExportado(nombre + ".csv", "text/csv; charset=UTF-8",
				csv.toString().getBytes(StandardCharsets.UTF_8));
	}

	// Entrecomilla, duplica comillas y neutraliza fórmulas (=, +, -, @).
	private String celda(String valor) {
		String v = valor == null ? "" : valor;
		if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
			v = "'" + v;
		}
		return "\"" + v.replace("\"", "\"\"") + "\"";
	}
	
	// Convierte un texto en una cadena JSON válida (escapa comillas, saltos de línea, etc.).
	private String cadenaJson(String valor) {
		String v = valor == null ? "" : valor;
		StringBuilder sb = new StringBuilder("\"");
		for (char c : v.toCharArray()) {
			switch (c) {
			case '"' -> sb.append("\\\"");
			case '\\' -> sb.append("\\\\");
			case '\n' -> sb.append("\\n");
			case '\r' -> sb.append("\\r");
			case '\t' -> sb.append("\\t");
			default -> {
				if (c < 0x20) {
					sb.append(String.format("\\u%04x", (int) c));
				} else {
					sb.append(c);
				}
			}
			}
		}
		return sb.append('"').toString();
	}
}