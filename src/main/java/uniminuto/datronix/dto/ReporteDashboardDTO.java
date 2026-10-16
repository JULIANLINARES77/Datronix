package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReporteDashboardDTO {
    String periodoActual;
    Long ventasMes;
    BigDecimal ingresosMes;
    BigDecimal variacionIngresosPorcentual;
    BigDecimal carteraCredito;
    Long productosStockCritico;
    List<StockBajoReporteDTO> stockBajo;
    List<ProductoVendidoReporteDTO> productosMasVendidos;
    List<ClienteFrecuenteReporteDTO> clientesFrecuentes;
    List<VentasMesReporteDTO> ventasPorMes;
    List<CreditoPendienteReporteDTO> creditosPendientes;
}
