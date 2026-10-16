package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.ClienteFrecuenteReporteDTO;
import uniminuto.datronix.dto.CreditoPendienteReporteDTO;
import uniminuto.datronix.dto.ProductoVendidoReporteDTO;
import uniminuto.datronix.dto.StockBajoReporteDTO;
import uniminuto.datronix.dto.VentasAggregateDTO;
import uniminuto.datronix.repository.ClienteRepository;
import uniminuto.datronix.repository.DetalleVentaRepository;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.repository.VentaRepository;
import uniminuto.datronix.service.ReporteService;

class ReporteServiceTest {
    private final VentaRepository ventaRepository = Mockito.mock(VentaRepository.class);
    private final ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
    private final DetalleVentaRepository detalleVentaRepository = Mockito.mock(DetalleVentaRepository.class);
    private final ClienteRepository clienteRepository = Mockito.mock(ClienteRepository.class);
    private final ReporteService service = new ReporteService(
            ventaRepository, productoRepository, detalleVentaRepository, clienteRepository);

    @Test
    void dashboardAggregatesSalesInventoryProductsAndCustomers() {
        when(ventaRepository.resumirVentas(any(LocalDate.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    YearMonth month = YearMonth.from(invocation.getArgument(0));
                    BigDecimal total = month.equals(YearMonth.now().minusMonths(1))
                            ? new BigDecimal("1000.00")
                            : new BigDecimal("1200.00");
                    return new VentasAggregateDTO(3L, total);
                });
        when(ventaRepository.resumirCarteraCredito())
                .thenReturn(new VentasAggregateDTO(2L, new BigDecimal("750.00")));
        when(ventaRepository.findCreditosPendientes()).thenReturn(List.of(
                new CreditoPendienteReporteDTO(1L, "Cliente Uno", LocalDate.now(), new BigDecimal("750.00"))));
        when(productoRepository.findStockBajo()).thenReturn(List.of(
                new StockBajoReporteDTO(1L, "Producto Uno", 2L, 5L)));
        when(detalleVentaRepository.findProductosMasVendidos()).thenReturn(List.of(
                new ProductoVendidoReporteDTO(1L, "Producto Uno", 4L, new BigDecimal("1000.00"))));
        when(clienteRepository.findClientesFrecuentes()).thenReturn(List.of(
                new ClienteFrecuenteReporteDTO("CLI-1", "Cliente Uno", 2L,
                        new BigDecimal("1200.00"), LocalDate.now())));

        var reporte = service.obtenerDashboard();

        assertEquals(3L, reporte.getVentasMes());
        assertEquals(new BigDecimal("1200.00"), reporte.getIngresosMes());
        assertEquals(new BigDecimal("20.0"), reporte.getVariacionIngresosPorcentual());
        assertEquals(new BigDecimal("750.00"), reporte.getCarteraCredito());
        assertEquals(1L, reporte.getProductosStockCritico());
        assertEquals(new BigDecimal("600.00"), reporte.getClientesFrecuentes().get(0).getPromedioCompra());
        assertEquals(12, reporte.getVentasPorMes().size());
        verify(ventaRepository, Mockito.times(12))
                .resumirVentas(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void dashboardDoesNotCalculateIncomeChangeWithoutPreviousMonthSales() {
        when(ventaRepository.resumirVentas(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new VentasAggregateDTO(0L, BigDecimal.ZERO));
        when(ventaRepository.resumirCarteraCredito()).thenReturn(new VentasAggregateDTO(0L, BigDecimal.ZERO));
        when(ventaRepository.findCreditosPendientes()).thenReturn(List.of());
        when(productoRepository.findStockBajo()).thenReturn(List.of());
        when(detalleVentaRepository.findProductosMasVendidos()).thenReturn(List.of());
        when(clienteRepository.findClientesFrecuentes()).thenReturn(List.of());

        var reporte = service.obtenerDashboard();

        assertNull(reporte.getVariacionIngresosPorcentual());
    }
}
