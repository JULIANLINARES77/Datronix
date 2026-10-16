package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import uniminuto.datronix.entity.Devolucion;

// Spring Data ofrece aquí el CRUD de devoluciones.
public interface DevolucionRepository extends JpaRepository<Devolucion,Long>{
    boolean existsByProducto_IdProducto(Long idProducto);

    List<Devolucion> findAllByOrderByFechaDevolucionDescIdDevolucionDesc();
}
