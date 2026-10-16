package uniminuto.datronix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.dto.StockBajoReporteDTO;

// Spring Data crea las operaciones básicas para consultar y guardar productos.
public interface ProductoRepository extends JpaRepository<Producto,Long>{
    
    @Query("SELECT p FROM Producto p JOIN FETCH p.proveedor")
    List<Producto> findAllWithProveedor();

    boolean existsBySkuProductoIgnoreCase(String skuProducto);

    boolean existsBySkuProductoIgnoreCaseAndIdProductoNot(String skuProducto, Long idProducto);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.idProducto = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT new uniminuto.datronix.dto.StockBajoReporteDTO(
                p.idProducto, p.nombreProducto, p.stockProducto, p.stockMinimoProducto)
            FROM Producto p
            WHERE p.stockProducto <= p.stockMinimoProducto
            ORDER BY p.stockProducto ASC, p.nombreProducto ASC
            """)
    List<StockBajoReporteDTO> findStockBajo();
    
}
