package uniminuto.datronix.dto;

import lombok.Value;

@Value
public class StockBajoReporteDTO {
    Long idProducto;
    String nombreProducto;
    Long stockActual;
    Long stockMinimo;
}
