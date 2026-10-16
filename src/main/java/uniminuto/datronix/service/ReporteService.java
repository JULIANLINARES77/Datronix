package uniminuto.datronix.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.ClienteFrecuenteReporteDTO;
import uniminuto.datronix.dto.CreditoPendienteReporteDTO;
import uniminuto.datronix.dto.ProductoVendidoReporteDTO;
import uniminuto.datronix.dto.ReporteDashboardDTO;
import uniminuto.datronix.dto.StockBajoReporteDTO;
import uniminuto.datronix.dto.VentasAggregateDTO;
import uniminuto.datronix.dto.VentasMesReporteDTO;
import uniminuto.datronix.repository.ClienteRepository;
import uniminuto.datronix.repository.DetalleVentaRepository;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.repository.VentaRepository;

@Service
public class ReporteService {
    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ClienteRepository clienteRepository;

    public ReporteService(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            DetalleVentaRepository detalleVentaRepository,
            ClienteRepository clienteRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public ReporteDashboardDTO obtenerDashboard() {
        YearMonth actual = YearMonth.now();
        List<VentasMesReporteDTO> ventasPorMes = IntStream.range(0, 12)
                .mapToObj(mesesAtras -> resumirMes(actual.minusMonths(mesesAtras)))
                .toList();
        VentasMesReporteDTO resumenMes = ventasPorMes.get(0);
        BigDecimal ingresosMesAnterior = ventasPorMes.get(1).getTotalMes();
        BigDecimal variacionIngresosPorcentual = ingresosMesAnterior.compareTo(CERO) == 0
                ? null
                : resumenMes.getTotalMes().subtract(ingresosMesAnterior)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(ingresosMesAnterior, 1, RoundingMode.HALF_UP);
        VentasAggregateDTO cartera = ventaRepository.resumirCarteraCredito();
        List<StockBajoReporteDTO> stockBajo = productoRepository.findStockBajo();
        List<ProductoVendidoReporteDTO> productos = detalleVentaRepository.findProductosMasVendidos()
                .stream().limit(10).toList();
        List<ClienteFrecuenteReporteDTO> clientes = clienteRepository.findClientesFrecuentes()
                .stream().limit(10).toList();
        List<CreditoPendienteReporteDTO> creditos = ventaRepository.findCreditosPendientes();

        return ReporteDashboardDTO.builder()
                .periodoActual(actual.toString())
                .ventasMes(resumenMes.getCantidadVentas())
                .ingresosMes(resumenMes.getTotalMes())
                .variacionIngresosPorcentual(variacionIngresosPorcentual)
                .carteraCredito(cartera.getTotal() == null ? CERO : cartera.getTotal())
                .productosStockCritico((long) stockBajo.size())
                .stockBajo(stockBajo)
                .productosMasVendidos(productos)
                .clientesFrecuentes(clientes)
                .ventasPorMes(ventasPorMes)
                .creditosPendientes(creditos)
                .build();
    }

    private VentasAggregateDTO consultarMes(YearMonth mes) {
        return ventaRepository.resumirVentas(mes.atDay(1), mes.plusMonths(1).atDay(1));
    }

    private VentasMesReporteDTO resumirMes(YearMonth mes) {
        VentasAggregateDTO agregado = consultarMes(mes);
        long cantidad = agregado.getCantidad() == null ? 0 : agregado.getCantidad();
        BigDecimal total = agregado.getTotal() == null ? CERO : agregado.getTotal();
        Locale locale = Locale.forLanguageTag("es-CO");
        String nombreMes = mes.getMonth().getDisplayName(TextStyle.FULL, locale);
        nombreMes = nombreMes.substring(0, 1).toUpperCase(locale) + nombreMes.substring(1);
        BigDecimal promedio = cantidad == 0 ? CERO
                : total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
        return VentasMesReporteDTO.builder()
                .periodo(mes.toString())
                .nombreMes(nombreMes)
                .anio(mes.getYear())
                .cantidadVentas(cantidad)
                .totalMes(total)
                .promedioVenta(promedio)
                .build();
    }
}
