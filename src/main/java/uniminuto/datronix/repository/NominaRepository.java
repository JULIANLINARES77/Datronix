package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import uniminuto.datronix.entity.Nomina;

// Spring Data ofrece aquí el CRUD de registros de nómina.
public interface NominaRepository extends JpaRepository<Nomina, Long> {
    @Query("""
            SELECT n FROM Nomina n
            JOIN FETCH n.empleado
            ORDER BY n.fechaPago DESC, n.idNomina DESC
            """)
    List<Nomina> findAllWithEmpleado();
}