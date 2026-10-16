package uniminuto.datronix.mapper;

import java.math.BigDecimal;

import uniminuto.datronix.dto.DetalleVentaResponseDTO;
import uniminuto.datronix.dto.RemisionResponseDTO;
import uniminuto.datronix.dto.VentaResponseDTO;
import uniminuto.datronix.entity.Remision;
import uniminuto.datronix.entity.EstadoRemisionBodega;
import uniminuto.datronix.entity.Venta;
import uniminuto.datronix.entity.Cliente;

public final class VentaMapper {
    private VentaMapper() {
    }

    public static VentaResponseDTO toDto(Venta venta, Remision remision) {
        Cliente cliente = venta.getCliente();
        return VentaResponseDTO.builder()
                .idVenta(venta.getIdVenta())
                .idCliente(cliente == null ? null : cliente.getIdCliente())
                .nombreCliente(cliente == null ? null : cliente.getNombreCliente())
                .fechaVenta(venta.getFechaVenta())
                .totalVenta(venta.getTotalVenta())
                .estadoPago(venta.getEstadoPago())
                .direccionEntrega(venta.getDireccionEntrega())
                .detalles(venta.getDetalleVentaList().stream()
                        .map(detalle -> DetalleVentaResponseDTO.builder()
                                .idDetalleVenta(detalle.getIdDetalleVenta())
                                .idProducto(detalle.getProducto() == null
                                        ? null : detalle.getProducto().getIdProducto())
                                .nombreProducto(detalle.getProducto() == null
                                        ? null : detalle.getProducto().getNombreProducto())
                                .cantidad(detalle.getCantidad())
                                .precioUnitario(detalle.getPrecioUnitario())
                                .descuento(detalle.getDescuentoItem() == null
                                        ? BigDecimal.ZERO : detalle.getDescuentoItem())
                                .subtotal(detalle.getSubtotalItem() == null && detalle.getPrecioUnitario() != null
                                        ? detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()))
                                        : detalle.getSubtotalItem())
                                .build())
                        .toList())
                .remision(remision == null ? null : RemisionResponseDTO.builder()
                        .idRemision(remision.getIdRemision())
                        .fechaEmision(remision.getFechaEmision())
                        .totalRemision(remision.getTotalRemision())
                        .observaciones(remision.getObservaciones())
                        .estadoBodega(EstadoRemisionBodega.actual(remision.getEstadoBodega()).name())
                        .observacionesBodega(remision.getObservacionesBodega())
                        .build())
                .build();
    }
}
