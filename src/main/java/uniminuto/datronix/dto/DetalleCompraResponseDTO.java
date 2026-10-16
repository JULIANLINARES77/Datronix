package uniminuto.datronix.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DetalleCompraResponseDTO {
    Long idDetalleCompra;
    Long idProducto;
    String nombreProducto;
    Long cantidad;
    BigDecimal precioUnitario;
    BigDecimal subtotal;
}
