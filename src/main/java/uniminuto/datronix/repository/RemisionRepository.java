package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.List;

import uniminuto.datronix.entity.EstadoRemisionBodega;
import uniminuto.datronix.entity.Remision;

// Spring Data ofrece aquí el CRUD de remisiones.
public interface RemisionRepository extends JpaRepository<Remision,Long> {
    @Query("SELECT r FROM Remision r WHERE r.venta.idVenta IN :ventaIds")
    List<Remision> findByVentaIds(@Param("ventaIds") Collection<Long> ventaIds);

    @Query("""
            SELECT DISTINCT r FROM Remision r
            JOIN FETCH r.venta v
            JOIN FETCH r.cliente
            LEFT JOIN FETCH v.detalleVentaList d
            LEFT JOIN FETCH d.producto
            WHERE r.estadoBodega IS NULL OR r.estadoBodega <> :entregada
            ORDER BY r.fechaEmision DESC, r.idRemision DESC
            """)
    List<Remision> findRemisionesPendientesBodega(
            @Param("entregada") EstadoRemisionBodega estadoEntregada);

    @Query("""
            SELECT DISTINCT r FROM Remision r
            JOIN FETCH r.venta v
            JOIN FETCH r.cliente
            LEFT JOIN FETCH v.detalleVentaList d
            LEFT JOIN FETCH d.producto
            WHERE r.idRemision = :id
            """)
    java.util.Optional<Remision> findByIdWithDetails(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Remision r WHERE r.idRemision = :id")
    java.util.Optional<Remision> findByIdForUpdate(@Param("id") Long id);
}
