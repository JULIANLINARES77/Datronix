package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import uniminuto.datronix.entity.Soporte;

// Spring Data ofrece aquí el CRUD de solicitudes de soporte.
public interface SoporteRepository extends JpaRepository<Soporte,Long> {
    List<Soporte> findAllByOrderByFechaCreacionDescIdSoporteDesc();
}
