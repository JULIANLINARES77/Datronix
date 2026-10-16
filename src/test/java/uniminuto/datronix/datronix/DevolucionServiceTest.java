package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.DevolucionCreateDTO;
import uniminuto.datronix.entity.Devolucion;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.exception.StockLimiteExcedidoException;
import uniminuto.datronix.repository.DevolucionRepository;
import uniminuto.datronix.repository.ProductoRepository;
import uniminuto.datronix.service.DevolucionService;

class DevolucionServiceTest {
    private final DevolucionRepository devolucionRepository = Mockito.mock(DevolucionRepository.class);
    private final ProductoRepository productoRepository = Mockito.mock(ProductoRepository.class);
    private final DevolucionService service = new DevolucionService(devolucionRepository, productoRepository);

    @Test
    void registrarDevolucionIncrementsStockAndPersistsReturn() {
        Producto producto = Producto.builder()
                .idProducto(7L)
                .nombreProducto("Teclado")
                .stockProducto(5L)
                .build();
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(producto));
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(invocation -> {
            Devolucion devolucion = invocation.getArgument(0);
            devolucion.setIdDevolucion(31L);
            return devolucion;
        });

        var response = service.registrarDevolucion(request(7L, 2L));

        assertEquals(7L, producto.getStockProducto());
        assertEquals(31L, response.getIdDevolucion());
        assertEquals("Teclado", response.getNombreProducto());
        assertEquals(2L, response.getCantidad());
        verify(productoRepository).save(producto);
    }

    @Test
    void registrarDevolucionRejectsStockOverflowBeforePersisting() {
        Producto producto = Producto.builder()
                .idProducto(7L)
                .nombreProducto("Teclado")
                .stockProducto(Long.MAX_VALUE)
                .build();
        when(productoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(producto));

        assertThrows(StockLimiteExcedidoException.class,
                () -> service.registrarDevolucion(request(7L, 1L)));

        verify(productoRepository, never()).save(any(Producto.class));
        verify(devolucionRepository, never()).save(any(Devolucion.class));
    }

    private DevolucionCreateDTO request(Long idProducto, Long cantidad) {
        DevolucionCreateDTO request = new DevolucionCreateDTO();
        request.setIdProducto(idProducto);
        request.setNombreCliente(" Cliente Uno ");
        request.setCantidad(cantidad);
        request.setMotivo(" Motivo ");
        request.setFechaDevolucion(LocalDateTime.of(2026, 9, 5, 16, 0));
        return request;
    }
}
