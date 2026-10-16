package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.DetalleVentaRequestDTO;
import uniminuto.datronix.dto.VentaCreateDTO;
import uniminuto.datronix.entity.Cliente;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.entity.Remision;
import uniminuto.datronix.entity.Venta;
import uniminuto.datronix.exception.StockInsuficienteException;
import uniminuto.datronix.repository.ClienteRepository;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.repository.RemisionRepository;
import uniminuto.datronix.repository.VentaRepository;
import uniminuto.datronix.service.VentaService;

class VentaServiceTest {
    private final VentaRepository ventaRepository = Mockito.mock(VentaRepository.class);
    private final ClienteRepository clienteRepository = Mockito.mock(ClienteRepository.class);
    private final ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
    private final RemisionRepository remisionRepository = Mockito.mock(RemisionRepository.class);
    private final VentaService service = new VentaService(
            ventaRepository, clienteRepository, productoRepository, remisionRepository);
    private final Cliente cliente = Cliente.builder()
            .idCliente("CLI-1")
            .nombreCliente("Cliente Uno")
            .build();

    @Test
    void registrarVentaDecrementsStockAndCreatesRemisionFromServerPrices() {
        Producto producto = Producto.builder()
                .idProducto(7L)
                .nombreProducto("Teclado")
                .precioVentaProducto(new BigDecimal("25.50"))
                .stockProducto(5L)
                .build();
        when(clienteRepository.findById("CLI-1")).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta venta = invocation.getArgument(0);
            venta.setIdVenta(22L);
            return venta;
        });
        when(remisionRepository.save(any(Remision.class))).thenAnswer(invocation -> {
            Remision remision = invocation.getArgument(0);
            remision.setIdRemision(55L);
            return remision;
        });

        var response = service.registrarVenta(request(7L, 2L));

        assertEquals(3L, producto.getStockProducto());
        assertEquals(new BigDecimal("51.00"), response.getTotalVenta());
        assertEquals(22L, response.getIdVenta());
        assertEquals(55L, response.getRemision().getIdRemision());
        assertEquals(new BigDecimal("51.00"), response.getRemision().getTotalRemision());
        assertEquals("PENDIENTE", response.getRemision().getEstadoBodega());
        assertEquals("Cliente Uno", response.getNombreCliente());
        verify(productoRepository).save(producto);
    }

    @Test
    void registrarVentaRejectsInsufficientStockWithoutPersistingSaleOrRemision() {
        Producto producto = Producto.builder()
                .idProducto(7L)
                .nombreProducto("Teclado")
                .precioVentaProducto(new BigDecimal("25.50"))
                .stockProducto(1L)
                .build();
        when(clienteRepository.findById("CLI-1")).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(producto));

        assertThrows(StockInsuficienteException.class, () -> service.registrarVenta(request(7L, 2L)));

        verify(productoRepository, never()).save(any(Producto.class));
        verify(ventaRepository, never()).save(any(Venta.class));
        verify(remisionRepository, never()).save(any(Remision.class));
    }

    private VentaCreateDTO request(Long idProducto, Long cantidad) {
        DetalleVentaRequestDTO detalle = new DetalleVentaRequestDTO();
        detalle.setIdProducto(idProducto);
        detalle.setCantidad(cantidad);

        VentaCreateDTO request = new VentaCreateDTO();
        request.setIdCliente("CLI-1");
        request.setFechaVenta(LocalDate.of(2026, 9, 5));
        request.setEstadoPago("CONTADO");
        request.setDireccionEntrega("Dirección de entrega");
        request.setObservacionesRemision("Entregar en horario laboral");
        request.setDetalles(List.of(detalle));
        return request;
    }
}
