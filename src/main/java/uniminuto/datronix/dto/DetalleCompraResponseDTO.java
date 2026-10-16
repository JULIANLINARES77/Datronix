package uniminuto.datronix.dto;

import java.math.BigDecimal;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DetalleCompraResponseDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idDetalleCompra;
    @JsonSerialize(using = ToStringSerializer.class)
    Long idProducto;
    String nombreProducto;
    Long cantidad;
    BigDecimal precioUnitario;
    BigDecimal subtotal;
}
