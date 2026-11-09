
package com.example.demo.service;

import com.example.demo.entity.Valoracion;
import com.example.demo.repository.ValoracionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValoracionService {

	private final ValoracionRepository valoracionRepository;
	private final ActividadService actividadService;

	public ValoracionService(ValoracionRepository valoracionRepository, ActividadService actividadService) {

		this.valoracionRepository = valoracionRepository;
		this.actividadService = actividadService;
	}

	// Crear una valoración
	// Crear una valoración y registrar la actividad
	@Transactional
	public Valoracion crearValoracion(Valoracion valoracion) {
		validarPuntuacion(valoracion.getPuntuacion());
		valoracion.setId(null);

		Valoracion valoracionGuardada = valoracionRepository.save(valoracion);

		actividadService.registrarActividad(valoracionGuardada.getUsuarioId(), valoracionGuardada.getObraId(),
				"VALORACION", "Ha valorado una obra con " + valoracionGuardada.getPuntuacion() + " estrellas");

		return valoracionGuardada;
	}

	// Editar una valoración existente
	public Valoracion editarValoracion(Long id, Integer puntuacion) {

		validarPuntuacion(puntuacion);

		Valoracion valoracion = valoracionRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Valoración no encontrada"));

		valoracion.setPuntuacion(puntuacion);

		return valoracionRepository.save(valoracion);
	}

	// Consultar una valoración por su ID
	public Valoracion obtenerValoracion(Long id) {
		return valoracionRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Valoración no encontrada"));
	}

	// Buscar la valoración más reciente de una obra
	public Valoracion obtenerValoracionPorObra(Long obraId) {
		return valoracionRepository.findFirstByObraIdOrderByIdDesc(obraId).orElse(null);
	}

	// Validar puntuación entre 0 y 5
	private void validarPuntuacion(Integer puntuacion) {

		if (puntuacion == null || puntuacion < 0 || puntuacion > 5) {
			throw new IllegalArgumentException("La puntuación debe estar entre 0 y 5 estrellas");
		}
	}

	public void eliminarValoracion(Long id) {
		if (!valoracionRepository.existsById(id)) {
			throw new IllegalArgumentException("La valoración no existe.");
		}

		valoracionRepository.deleteById(id);
	}

	public Valoracion obtenerValoracionPorUsuarioYObra(Long usuarioId, Long obraId) {
		return valoracionRepository.findByUsuarioIdAndObraId(usuarioId, obraId).orElse(null);
	}

	// Obtener ranking de obras por puntuación media
	public List<Object[]> obtenerRanking() {
		return valoracionRepository.obtenerRankingPorValoracion();
	}
}
