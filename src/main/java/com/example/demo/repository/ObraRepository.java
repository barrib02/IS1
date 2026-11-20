package com.example.demo.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.EstadoValidacion;
import com.example.demo.entity.GeneroObra;
import com.example.demo.entity.Obra;
import com.example.demo.entity.TipoObra;

public interface ObraRepository extends JpaRepository<Obra, Long> {

	List<Obra> findByTituloContainingIgnoreCase(String titulo);

	List<Obra> findByTipo(TipoObra tipo);

	List<Obra> findByTituloContainingIgnoreCaseAndTipo(String titulo, TipoObra tipo);

	List<Obra> findTop5ByTituloStartingWithIgnoreCase(String titulo);

	// Devuelve todas las obras de un creador
	List<Obra> findByCreadorId(Long creadorId);

	// Devuelve las obras de un creador ordenadas por año
	List<Obra> findByCreadorIdOrderByAnioAsc(Long creadorId);

	// ---------- NUEVO (Sprint 5: rendimiento) ----------

	// Versiones con Pageable: la base de datos solo devuelve las filas pedidas
	// (por ejemplo 50) en vez de todas las obras.
	List<Obra> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);

	List<Obra> findByTipo(TipoObra tipo, Pageable pageable);

	List<Obra> findByTituloContainingIgnoreCaseAndTipo(String titulo, TipoObra tipo, Pageable pageable);

	// Las 12 obras más recientes, para mostrar contenido antes de buscar.
	List<Obra> findTop12ByOrderByIdDesc();

	// Obra + géneros en UNA sola consulta (antes eran 2: la obra y luego sus géneros).
	@Query("select o from Obra o left join fetch o.generos where o.id = :id")
	Optional<Obra> findConGenerosById(@Param("id") Long id);
	
	// Obras que tengan CUALQUIERA de los géneros indicados (más recientes primero).
	@Query("select o from Obra o where o.id in "
			+ "(select x.id from Obra x join x.generos g where g in :generos) order by o.id desc")
	List<Obra> buscarPorGeneros(@Param("generos") Collection<GeneroObra> generos, Pageable pageable);

	// Lo mismo, pero limitado a un tipo (libro, película…).
	@Query("select o from Obra o where o.tipo = :tipo and o.id in "
			+ "(select x.id from Obra x join x.generos g where g in :generos) order by o.id desc")
	List<Obra> buscarPorGenerosYTipo(@Param("generos") Collection<GeneroObra> generos,
			@Param("tipo") TipoObra tipo, Pageable pageable);
	
	// Obras enviadas por un usuario (Mis obras).
	List<Obra> findByAutorIdOrderByIdDesc(Long autorId);

	// Obras por estado de validación (panel de moderación).
	List<Obra> findByEstadoValidacionOrderByIdAsc(EstadoValidacion estado);
	
	@Modifying
	@Query("update Obra o set o.autorId = null where o.autorId = :usuarioId")
	void desvincularAutor(@Param("usuarioId") Long usuarioId);

}
