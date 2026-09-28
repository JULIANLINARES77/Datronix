package uniminuto.datronix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
// Agrupa el correo y la contraseña que llegan desde el formulario de acceso.
public class LoginRequest {

    
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    private String correoUsuario;

    @NotBlank(message = "La contraseña es obligatoriaG")
    private String contrasenaUsuario;

}
