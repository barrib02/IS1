package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

	// Las 50 notificaciones más recientes de un usuario.
	List<Notificacion> findTop50ByUsuarioIdOrderByFechaDesc(Long usuarioId);

	// Cuántas no ha leído todavía (número del globo rojo de la campana).
	long countByUsuarioIdAndLeidaFalse(Long usuarioId);

	// Buscar una notificación SOLO si pertenece a ese usuario
	// (así nadie puede marcar como leídas las de otra persona).
	Optional<Notificacion> findByIdAndUsuarioId(Long id, Long usuarioId);

	// Marcar todas como leídas con una sola sentencia SQL.
	@Modifying
	@Query("update Notificacion n set n.leida = true where n.usuarioId = :usuarioId and n.leida = false")
	int marcarTodasLeidas(@Param("usuarioId") Long usuarioId);
	
	void deleteByUsuarioId(Long usuarioId);
}
