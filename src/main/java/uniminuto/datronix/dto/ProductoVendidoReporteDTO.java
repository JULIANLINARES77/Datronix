package uniminuto.datronix.dto;

import java.math.BigDecimal;

import lombok.Value;

@Value
public class ProductoVendidoReporteDTO {
    Long idProducto;
    String nombreProducto;
    Long unidadesVendidas;
    BigDecimal ingresosGenerados;
}
