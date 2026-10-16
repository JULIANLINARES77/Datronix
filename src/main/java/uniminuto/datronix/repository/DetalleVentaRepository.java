package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import uniminuto.datronix.entity.DetalleVenta;
import uniminuto.datronix.dto.ProductoVendidoReporteDTO;

// Spring Data ofrece aquí el CRUD de detalles de venta.
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta,Long>{
    @Query("""
            SELECT new uniminuto.datronix.dto.ProductoVendidoReporteDTO(
                p.idProducto, p.nombreProducto, SUM(d.cantidad),
                SUM(COALESCE(d.subtotalItem, d.precioUnitario * d.cantidad)))
            FROM DetalleVenta d
            JOIN d.producto p
            GROUP BY p.idProducto, p.nombreProducto
            ORDER BY SUM(d.cantidad) DESC
            """)
    List<ProductoVendidoReporteDTO> findProductosMasVendidos();
}
