package uniminuto.datronix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmailCodeVerificationDTO {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 255, message = "El correo no puede superar 255 caracteres")
    private String correoUsuario;

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "\\d{6}", message = "El código debe tener 6 dígitos")
    private String codigo;
}
