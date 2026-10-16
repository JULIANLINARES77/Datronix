package uniminuto.datronix.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BodegaDetalleDTO {
    Long idProducto;
    String skuProducto;
    String nombreProducto;
    Long cantidad;
}
