package uniminuto.datronix.dto;

import java.math.BigDecimal;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Value;

@Value
public class ProductoVendidoReporteDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idProducto;
    String nombreProducto;
    Long unidadesVendidas;
    BigDecimal ingresosGenerados;
}
