package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VentaRecienteResponseDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idVenta;
    String nombreCliente;
    LocalDate fechaVenta;
    BigDecimal totalVenta;
    String estadoPago;
}
