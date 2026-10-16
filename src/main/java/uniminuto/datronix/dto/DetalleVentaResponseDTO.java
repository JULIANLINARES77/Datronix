package uniminuto.datronix.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DetalleVentaResponseDTO {
    Long idDetalleVenta;
    Long idProducto;
    String nombreProducto;
    Long cantidad;
    BigDecimal precioUnitario;
    BigDecimal descuento;
    BigDecimal subtotal;
}
