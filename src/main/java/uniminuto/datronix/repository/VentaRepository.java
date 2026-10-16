package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import uniminuto.datronix.dto.CreditoPendienteReporteDTO;
import uniminuto.datronix.dto.VentasAggregateDTO;
import uniminuto.datronix.entity.Venta;

// Spring Data crea las operaciones básicas para las ventas.
public interface VentaRepository extends JpaRepository<Venta,Long> {
    boolean existsByCliente_IdCliente(String idCliente);

    @Query("""
            SELECT DISTINCT v FROM Venta v
            LEFT JOIN FETCH v.cliente
            LEFT JOIN FETCH v.detalleVentaList d
            LEFT JOIN FETCH d.producto
            ORDER BY v.fechaVenta DESC, v.idVenta DESC
            """)
    List<Venta> findAllWithDetails();

    @Query("""
            SELECT DISTINCT v FROM Venta v
            LEFT JOIN FETCH v.cliente
            LEFT JOIN FETCH v.detalleVentaList d
            LEFT JOIN FETCH d.producto
            WHERE v.idVenta = :id
            """)
    Optional<Venta> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT v FROM Venta v
            LEFT JOIN FETCH v.cliente
            ORDER BY v.fechaVenta DESC, v.idVenta DESC
            """)
    List<Venta> findLatestSales(Pageable pageable);

    @Query("""
            SELECT new uniminuto.datronix.dto.VentasAggregateDTO(
                COUNT(v), SUM(v.totalVenta))
            FROM Venta v
            WHERE v.fechaVenta >= :desde AND v.fechaVenta < :hasta
            """)
    VentasAggregateDTO resumirVentas(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("""
            SELECT new uniminuto.datronix.dto.VentasAggregateDTO(
                COUNT(v), SUM(v.totalVenta))
            FROM Venta v
            WHERE v.estadoPago = 'CREDITO'
            """)
    VentasAggregateDTO resumirCarteraCredito();

    @Query("""
            SELECT new uniminuto.datronix.dto.CreditoPendienteReporteDTO(
                v.idVenta, c.nombreCliente, v.fechaVenta, v.totalVenta)
            FROM Venta v
            JOIN v.cliente c
            WHERE v.estadoPago = 'CREDITO'
            ORDER BY v.fechaVenta ASC, v.idVenta ASC
            """)
    List<CreditoPendienteReporteDTO> findCreditosPendientes();
}
