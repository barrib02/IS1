
package com.example.demo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.demo.entity.GeneroObra;

// Convierte los identificadores de generos de TMDB
// a los generos definidos en Shelfie.
@Component
public class TmdbGeneroMapper {

	// Recibe los identificadores de generos de TMDB
	// y devuelve los generos equivalentes de Shelfie.
	public Set<GeneroObra> convertirGeneros(List<Integer> ids) {

		Set<GeneroObra> generos = new HashSet<>();

		// Evita errores si TMDB no proporciona generos.
		if (ids == null) {
			return generos;
		}

		for (Integer id : ids) {

			if (id == null) {
				continue;
			}

			// Relaciona cada identificador con su genero.
			// Los identificadores no reconocidos se omiten.
			switch (id) {
			case 28 -> generos.add(GeneroObra.ACCION);
			case 12 -> generos.add(GeneroObra.AVENTURA);
			case 16 -> generos.add(GeneroObra.ANIMACION);
			case 35 -> generos.add(GeneroObra.COMEDIA);
			case 80 -> generos.add(GeneroObra.CRIMEN);
			case 99 -> generos.add(GeneroObra.DOCUMENTAL);
			case 18 -> generos.add(GeneroObra.DRAMA);
			case 10751 -> generos.add(GeneroObra.INFANTIL);
			case 14 -> generos.add(GeneroObra.FANTASIA);
			case 36 -> generos.add(GeneroObra.HISTORICO);
			case 27 -> generos.add(GeneroObra.TERROR);
			case 10402 -> generos.add(GeneroObra.MUSICAL);
			case 9648 -> generos.add(GeneroObra.MISTERIO);
			case 10749 -> generos.add(GeneroObra.ROMANCE);
			case 878 -> generos.add(GeneroObra.CIENCIA_FICCION);
			case 53 -> generos.add(GeneroObra.THRILLER);
			case 10752 -> generos.add(GeneroObra.BELICO);
			case 37 -> generos.add(GeneroObra.WESTERN);

			// Generos especificos de las series.
			case 10759 -> {
				generos.add(GeneroObra.ACCION);
				generos.add(GeneroObra.AVENTURA);
			}
			case 10762 -> generos.add(GeneroObra.INFANTIL);
			case 10763 -> generos.add(GeneroObra.DOCUMENTAL);
			case 10764 -> generos.add(GeneroObra.REALITY);
			case 10765 -> generos.add(GeneroObra.CIENCIA_FICCION);
			case 10766 -> generos.add(GeneroObra.TELENOVELA);
			case 10767 -> generos.add(GeneroObra.REALITY);
			case 10768 -> generos.add(GeneroObra.BELICO);

			default -> {
				// No se asigna un genero si no hay equivalente.
			}
			}
		}

		return generos;
	}
}
