package uniminuto.datronix.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VentasMesReporteDTO {
    String periodo;
    String nombreMes;
    int anio;
    Long cantidadVentas;
    BigDecimal totalMes;
    BigDecimal promedioVenta;
}
