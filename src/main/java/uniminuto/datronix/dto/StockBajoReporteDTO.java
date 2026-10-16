package uniminuto.datronix.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Value;

@Value
public class StockBajoReporteDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idProducto;
    String nombreProducto;
    Long stockActual;
    Long stockMinimo;
}
