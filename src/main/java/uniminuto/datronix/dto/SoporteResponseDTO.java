package uniminuto.datronix.dto;

import java.time.LocalDateTime;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SoporteResponseDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idSoporte;
    String empresa;
    String idRemitente;
    String nombreRemitente;
    String telefonoRemitente;
    String asunto;
    String descripcion;
    LocalDateTime fechaCreacion;
}
