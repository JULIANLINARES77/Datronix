package uniminuto.datronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import uniminuto.datronix.entity.Cliente;
import uniminuto.datronix.dto.ClienteFrecuenteReporteDTO;

// Spring Data crea automáticamente las consultas básicas de clientes.
public interface ClienteRepository  extends JpaRepository<Cliente,String> {
    List<Cliente> findByIdClienteContainingIgnoreCaseOrNombreClienteContainingIgnoreCase(
            String idCliente,
            String nombreCliente);

    @Query("""
            SELECT new uniminuto.datronix.dto.ClienteFrecuenteReporteDTO(
                c.idCliente, c.nombreCliente, COUNT(v), SUM(v.totalVenta), MAX(v.fechaVenta))
            FROM Venta v
            JOIN v.cliente c
            GROUP BY c.idCliente, c.nombreCliente
            ORDER BY COUNT(v) DESC, SUM(v.totalVenta) DESC
            """)
    List<ClienteFrecuenteReporteDTO> findClientesFrecuentes();
}