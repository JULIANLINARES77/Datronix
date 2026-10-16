package uniminuto.datronix.dto;

import java.time.LocalDateTime;
import java.util.List;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BodegaRemisionDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idRemision;
    @JsonSerialize(using = ToStringSerializer.class)
    Long idVenta;
    String nombreCliente;
    LocalDateTime fechaEmision;
    String direccionEntrega;
    String observacionesVenta;
    String estadoBodega;
    String observacionesBodega;
    List<BodegaDetalleDTO> detalles;
}
