package uniminuto.datronix.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SoporteResponseDTO {
    Long idSoporte;
    String empresa;
    String idRemitente;
    String nombreRemitente;
    String telefonoRemitente;
    String asunto;
    String descripcion;
    LocalDateTime fechaCreacion;
}
