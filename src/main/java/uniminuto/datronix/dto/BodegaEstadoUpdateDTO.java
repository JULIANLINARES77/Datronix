package uniminuto.datronix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BodegaEstadoUpdateDTO {

    @NotBlank(message = "El estado de la remisión es obligatorio")
    @Pattern(
            regexp = "(?i)PENDIENTE|EN_PREPARACION|DESPACHADA|ENTREGADA",
            message = "El estado debe ser PENDIENTE, EN_PREPARACION, DESPACHADA o ENTREGADA")
    private String estadoBodega;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observacionesBodega;
}
