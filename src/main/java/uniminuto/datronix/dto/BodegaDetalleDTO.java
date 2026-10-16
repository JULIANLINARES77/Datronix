package uniminuto.datronix.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BodegaDetalleDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idProducto;
    String skuProducto;
    String nombreProducto;
    Long cantidad;
}
