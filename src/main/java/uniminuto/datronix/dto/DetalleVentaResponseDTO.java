package uniminuto.datronix.dto;

import java.math.BigDecimal;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DetalleVentaResponseDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idDetalleVenta;
    @JsonSerialize(using = ToStringSerializer.class)
    Long idProducto;
    String nombreProducto;
    Long cantidad;
    BigDecimal precioUnitario;
    BigDecimal descuento;
    BigDecimal subtotal;
}
