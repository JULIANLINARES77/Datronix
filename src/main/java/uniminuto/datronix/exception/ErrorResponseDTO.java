package uniminuto.datronix.exception;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Es la respuesta sencilla que el frontend recibe cuando una operación falla.
public class ErrorResponseDTO {
    private String codigo;      // Ej. "CLI-001"
    private String mensaje;     // Descripción amigable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Instant bloqueadaHasta;
}