package uniminuto.datronix.dto;

import java.time.LocalDateTime;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DevolucionResponseDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idDevolucion;
    @JsonSerialize(using = ToStringSerializer.class)
    Long idProducto;
    String nombreProducto;
    String nombreCliente;
    Long cantidad;
    String motivo;
    LocalDateTime fechaDevolucion;
}
