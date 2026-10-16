package uniminuto.datronix.mapper;

import java.math.BigDecimal;
import java.util.List;

import uniminuto.datronix.dto.CompraResponseDTO;
import uniminuto.datronix.dto.DetalleCompraResponseDTO;
import uniminuto.datronix.entity.Compra;

public final class CompraMapper {
    private CompraMapper() {
    }

    public static CompraResponseDTO toDto(Compra compra) {
        if (compra == null) {
            return null;
        }
        List<DetalleCompraResponseDTO> detalles = compra.getDetalles().stream()
                .map(detalle -> DetalleCompraResponseDTO.builder()
                        .idDetalleCompra(detalle.getIdDetalleCompra())
                        .idProducto(detalle.getProducto().getIdProducto())
                        .nombreProducto(detalle.getProducto().getNombreProducto())
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subtotal(detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad())))
                        .build())
                .toList();

        return CompraResponseDTO.builder()
                .idCompra(compra.getIdCompra())
                .idProveedor(compra.getProveedor().getIdProveedor())
                .nombreEmpresa(compra.getProveedor().getNombreEmpresa())
                .fechaCompra(compra.getFechaCompra())
                .totalCompra(compra.getTotalCompra())
                .detalles(detalles)
                .build();
    }
}
