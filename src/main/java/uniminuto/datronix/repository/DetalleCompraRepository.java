package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import uniminuto.datronix.entity.DetalleCompra;

// Spring Data ofrece aquí el CRUD de detalles de compra.
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra,Long> {
    boolean existsByProducto_IdProducto(Long idProducto);
}