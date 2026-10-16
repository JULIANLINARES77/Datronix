package uniminuto.datronix.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BodegaRemisionDTO {
    Long idRemision;
    Long idVenta;
    String nombreCliente;
    LocalDateTime fechaEmision;
    String direccionEntrega;
    String observacionesVenta;
    String estadoBodega;
    String observacionesBodega;
    List<BodegaDetalleDTO> detalles;
}
