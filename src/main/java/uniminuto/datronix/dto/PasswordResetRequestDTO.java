package uniminuto.datronix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequestDTO {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 255, message = "El correo no puede superar 255 caracteres")
    private String correoUsuario;

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "\\d{6}", message = "El código debe tener 6 dígitos")
    private String codigo;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "La contraseña debe incluir al menos una mayúscula y un número")
    private String nuevaContrasena;
}
