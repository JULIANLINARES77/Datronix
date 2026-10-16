package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import uniminuto.datronix.entity.Empleado;

// Spring Data ofrece aquí el CRUD de empleados.
public interface EmpleadoRepository extends JpaRepository<Empleado,String>{

}
