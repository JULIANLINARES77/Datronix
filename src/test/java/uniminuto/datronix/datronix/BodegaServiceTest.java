package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.BodegaEstadoUpdateDTO;
import uniminuto.datronix.entity.Cliente;
import uniminuto.datronix.entity.DetalleVenta;
import uniminuto.datronix.entity.EstadoRemisionBodega;
import uniminuto.datronix.entity.Producto;
import uniminuto.datronix.entity.Remision;
import uniminuto.datronix.entity.Venta;
import uniminuto.datronix.exception.EstadoRemisionBodegaInvalidoException;
import uniminuto.datronix.repository.RemisionRepository;
import uniminuto.datronix.service.BodegaService;

class BodegaServiceTest {
    private final RemisionRepository remisionRepository = Mockito.mock(RemisionRepository.class);
    private final BodegaService service = new BodegaService(remisionRepository);

    @Test
    void actualizarEstadoAllowsNextStepAndDoesNotChangeStock() {
        Producto producto = Producto.builder()
                .idProducto(7L)
                .nombreProducto("Teclado")
                .skuProducto("KEY-7")
                .stockProducto(12L)
                .build();
        Cliente cliente = Cliente.builder().nombreCliente("Cliente Uno").build();
        Venta venta = Venta.builder().idVenta(22L).cliente(cliente).build();
        venta.addDetalle(DetalleVenta.builder().producto(producto).cantidad(2L).build());
        Remision remision = Remision.builder()
                .idRemision(55L)
                .venta(venta)
                .cliente(cliente)
                .observaciones("Entregar en recepción")
                .estadoBodega(EstadoRemisionBodega.PENDIENTE)
                .build();
        BodegaEstadoUpdateDTO request = new BodegaEstadoUpdateDTO();
        request.setEstadoBodega("EN_PREPARACION");
        request.setObservacionesBodega("Pedido alistado");
        when(remisionRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(remision));
        when(remisionRepository.save(any(Remision.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.actualizarEstado(55L, request);

        assertEquals("EN_PREPARACION", response.getEstadoBodega());
        assertEquals("Pedido alistado", response.getObservacionesBodega());
        assertEquals("Entregar en recepción", response.getObservacionesVenta());
        assertEquals(12L, producto.getStockProducto());
        verify(remisionRepository).save(remision);
    }

    @Test
    void actualizarEstadoRejectsSkippingAWorkflowStep() {
        Remision remision = Remision.builder()
                .idRemision(55L)
                .venta(Venta.builder().idVenta(22L).build())
                .cliente(Cliente.builder().nombreCliente("Cliente Uno").build())
                .estadoBodega(EstadoRemisionBodega.PENDIENTE)
                .build();
        BodegaEstadoUpdateDTO request = new BodegaEstadoUpdateDTO();
        request.setEstadoBodega("DESPACHADA");
        when(remisionRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(remision));

        assertThrows(
                EstadoRemisionBodegaInvalidoException.class,
                () -> service.actualizarEstado(55L, request));
        verify(remisionRepository, never()).save(any(Remision.class));
    }
}
