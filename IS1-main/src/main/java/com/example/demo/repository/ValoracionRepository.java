
package com.example.demo.repository;

import com.example.demo.entity.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

	Optional<Valoracion> findFirstByObraIdOrderByIdDesc(Long obraId);

	Optional<Valoracion> findByUsuarioIdAndObraId(Long usuarioId, Long obraId);

	// Ranking de obras según su puntuación media
	@Query("""
			SELECT v.obraId, AVG(v.puntuacion), COUNT(v)
			FROM Valoracion v
			GROUP BY v.obraId
			ORDER BY AVG(v.puntuacion) DESC
			""")
	List<Object[]> obtenerRankingPorValoracion();

}
