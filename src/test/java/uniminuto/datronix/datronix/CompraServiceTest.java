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

import uniminuto.datronix.dto.CompraCreateDTO;
import uniminuto.datronix.dto.DetalleCompraRequestDTO;
import uniminuto.datronix.entity.Compra;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.entity.Proveedor;
import uniminuto.datronix.exception.CompraTotalExcedidoException;
import uniminuto.datronix.exception.StockLimiteExcedidoException;
import uniminuto.datronix.repository.CompraRepository;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.repository.ProveedorRepository;
import uniminuto.datronix.service.CompraService;

class CompraServiceTest {
    private final CompraRepository compraRepository = Mockito.mock(CompraRepository.class);
    private final ProveedorRepository proveedorRepository = Mockito.mock(ProveedorRepository.class);
    private final ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
    private final CompraService service = new CompraService(
            compraRepository, proveedorRepository, productoRepository);
    private final Proveedor proveedor = Proveedor.builder()
            .idProveedor("PROV-1")
            .nombreEmpresa("Proveedor Uno")
            .build();

    @Test
    void registrarCompraIncrementsStockAndReturnsCalculatedTotals() {
        Producto producto = Producto.builder()
                .idProducto(7L)
                .nombreProducto("Teclado")
                .stockProducto(5L)
                .build();
        when(proveedorRepository.findById("PROV-1")).thenReturn(Optional.of(proveedor));
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(producto));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            compra.setIdCompra(12L);
            return compra;
        });

        var response = service.registrarCompra(request(7L, 3L, "10.25"));

        assertEquals(8L, producto.getStockProducto());
        assertEquals(new BigDecimal("30.75"), response.getTotalCompra());
        assertEquals(12L, response.getIdCompra());
        assertEquals("Proveedor Uno", response.getNombreEmpresa());
        assertEquals(new BigDecimal("30.75"), response.getDetalles().get(0).getSubtotal());
        verify(productoRepository).save(producto);
    }

    @Test
    void registrarCompraRejectsStockOverflowBeforeSavingAnything() {
        Producto producto = Producto.builder()
                .idProducto(7L)
                .nombreProducto("Teclado")
                .stockProducto(Long.MAX_VALUE)
                .build();
        when(proveedorRepository.findById("PROV-1")).thenReturn(Optional.of(proveedor));
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(producto));

        assertThrows(StockLimiteExcedidoException.class,
                () -> service.registrarCompra(request(7L, 1L, "10.00")));

        verify(productoRepository, never()).save(any(Producto.class));
        verify(compraRepository, never()).save(any(Compra.class));
    }

    @Test
    void registrarCompraRejectsTotalOutsideDatabasePrecision() {
        Producto producto = Producto.builder()
                .idProducto(7L)
                .nombreProducto("Teclado")
                .stockProducto(5L)
                .build();
        when(proveedorRepository.findById("PROV-1")).thenReturn(Optional.of(proveedor));
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(producto));

        assertThrows(CompraTotalExcedidoException.class,
                () -> service.registrarCompra(request(7L, 1L, "100000000.00")));

        verify(productoRepository, never()).save(any(Producto.class));
        verify(compraRepository, never()).save(any(Compra.class));
    }

    private CompraCreateDTO request(Long idProducto, Long cantidad, String precio) {
        DetalleCompraRequestDTO detalle = new DetalleCompraRequestDTO();
        detalle.setIdProducto(idProducto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(new BigDecimal(precio));

        CompraCreateDTO request = new CompraCreateDTO();
        request.setIdProveedor("PROV-1");
        request.setFechaCompra(LocalDate.of(2026, 9, 5));
        request.setDetalles(List.of(detalle));
        return request;
    }
}
