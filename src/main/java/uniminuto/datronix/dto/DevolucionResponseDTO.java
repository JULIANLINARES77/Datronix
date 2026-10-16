package uniminuto.datronix.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DevolucionResponseDTO {
    Long idDevolucion;
    Long idProducto;
    String nombreProducto;
    String nombreCliente;
    Long cantidad;
    String motivo;
    LocalDateTime fechaDevolucion;
}
