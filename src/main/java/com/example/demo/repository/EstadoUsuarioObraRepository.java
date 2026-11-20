package com.example.demo.repository;

import com.example.demo.entity.EstadoObra;
import com.example.demo.entity.EstadoUsuarioObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoUsuarioObraRepository extends JpaRepository<EstadoUsuarioObra, Long> {
    
    Optional<EstadoUsuarioObra> findByUsuarioIdAndObraId(Long usuarioId, Long obraId);
 // Imports: java.util.List y com.example.demo.entity.EstadoObra
    List<EstadoUsuarioObra> findByUsuarioIdAndEstadoOrderByFechaActualizacionDesc(Long usuarioId, EstadoObra estado);
    
	// Nº de obras con ese estado por tipo (libros, películas...) en el periodo.
	@Query("""
			SELECT o.tipo, COUNT(e)
			FROM EstadoUsuarioObra e JOIN Obra o ON o.id = e.obraId
			WHERE e.usuarioId = :usuarioId AND e.estado = :estado
			  AND (:todo = true OR e.fechaCompletada >= :desde)
			GROUP BY o.tipo
			""")
	List<Object[]> contarPorTipo(@Param("usuarioId") Long usuarioId, @Param("estado") EstadoObra estado,
			@Param("todo") boolean todo, @Param("desde") LocalDateTime desde);

	// Géneros más frecuentes entre esas obras.
	@Query("""
			SELECT g, COUNT(DISTINCT o.id)
			FROM EstadoUsuarioObra e JOIN Obra o ON o.id = e.obraId JOIN o.generos g
			WHERE e.usuarioId = :usuarioId AND e.estado = :estado
			  AND (:todo = true OR e.fechaCompletada >= :desde)
			GROUP BY g
			ORDER BY COUNT(DISTINCT o.id) DESC
			""")
	List<Object[]> contarGeneros(@Param("usuarioId") Long usuarioId, @Param("estado") EstadoObra estado,
			@Param("todo") boolean todo, @Param("desde") LocalDateTime desde);
	
	// Obras con ese estado, con su fecha de finalización, entre dos fechas.
	@Query("""
			SELECT e, o
			FROM EstadoUsuarioObra e JOIN Obra o ON o.id = e.obraId
			WHERE e.usuarioId = :usuarioId AND e.estado = :estado
			  AND e.fechaCompletada >= :desde AND e.fechaCompletada < :hasta
			ORDER BY e.fechaCompletada DESC
			""")
	List<Object[]> completadasEntre(@Param("usuarioId") Long usuarioId, @Param("estado") EstadoObra estado,
			@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
	
	void deleteByUsuarioId(Long usuarioId);
}