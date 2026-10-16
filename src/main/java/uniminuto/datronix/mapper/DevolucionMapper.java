package uniminuto.datronix.mapper;

import uniminuto.datronix.dto.DevolucionResponseDTO;
import uniminuto.datronix.entity.Devolucion;

public final class DevolucionMapper {
    private DevolucionMapper() {
    }

    public static DevolucionResponseDTO toDto(Devolucion devolucion) {
        return DevolucionResponseDTO.builder()
                .idDevolucion(devolucion.getIdDevolucion())
                .idProducto(devolucion.getProducto().getIdProducto())
                .nombreProducto(devolucion.getProducto().getNombreProducto())
                .nombreCliente(devolucion.getNombreCliente())
                .cantidad(devolucion.getCantidad())
                .motivo(devolucion.getMotivo())
                .fechaDevolucion(devolucion.getFechaDevolucion())
                .build();
    }
}
