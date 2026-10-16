package uniminuto.datronix.mapper;

import java.util.List;

import uniminuto.datronix.dto.BodegaDetalleDTO;
import uniminuto.datronix.dto.BodegaRemisionDTO;
import uniminuto.datronix.entity.DetalleVenta;
import uniminuto.datronix.entity.EstadoRemisionBodega;
import uniminuto.datronix.entity.Remision;

public final class BodegaMapper {

    private BodegaMapper() {
    }

    public static BodegaRemisionDTO toDto(Remision remision) {
        List<BodegaDetalleDTO> detalles = remision.getVenta().getDetalleVentaList().stream()
                .map(BodegaMapper::toDetalleDto)
                .toList();
        return BodegaRemisionDTO.builder()
                .idRemision(remision.getIdRemision())
                .idVenta(remision.getVenta().getIdVenta())
                .nombreCliente(remision.getCliente().getNombreCliente())
                .fechaEmision(remision.getFechaEmision())
                .direccionEntrega(remision.getVenta().getDireccionEntrega())
                .observacionesVenta(remision.getObservaciones())
                .estadoBodega(EstadoRemisionBodega.actual(remision.getEstadoBodega()).name())
                .observacionesBodega(remision.getObservacionesBodega())
                .detalles(detalles)
                .build();
    }

    private static BodegaDetalleDTO toDetalleDto(DetalleVenta detalle) {
        return BodegaDetalleDTO.builder()
                .idProducto(detalle.getProducto().getIdProducto())
                .skuProducto(detalle.getProducto().getSkuProducto())
                .nombreProducto(detalle.getProducto().getNombreProducto())
                .cantidad(detalle.getCantidad())
                .build();
    }
}
