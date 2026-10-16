package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VentaRecienteResponseDTO {
    Long idVenta;
    String nombreCliente;
    LocalDate fechaVenta;
    BigDecimal totalVenta;
    String estadoPago;
}
