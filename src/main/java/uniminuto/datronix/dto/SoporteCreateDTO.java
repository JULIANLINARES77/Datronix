package uniminuto.datronix.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SoporteCreateDTO {
    @NotBlank(message = "La empresa es obligatoria")
    @Size(max = 100, message = "La empresa no puede superar 100 caracteres")
    private String empresa;

    @NotBlank(message = "El documento del remitente es obligatorio")
    @Size(max = 20, message = "El documento no puede superar 20 caracteres")
    private String idRemitente;

    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombreRemitente;

    @NotBlank(message = "El teléfono del remitente es obligatorio")
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefonoRemitente;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 100, message = "El asunto no puede superar 100 caracteres")
    private String asunto;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha de creación es obligatoria")
    private LocalDateTime fechaCreacion;
}
