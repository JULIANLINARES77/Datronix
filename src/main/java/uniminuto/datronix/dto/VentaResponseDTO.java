package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VentaResponseDTO {
    Long idVenta;
    String idCliente;
    String nombreCliente;
    LocalDate fechaVenta;
    BigDecimal totalVenta;
    String estadoPago;
    String direccionEntrega;
    List<DetalleVentaResponseDTO> detalles;
    RemisionResponseDTO remision;
}
