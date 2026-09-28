package uniminuto.datronix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioCreateDTO {

    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "\\d{6,20}", message = "El documento debe contener entre 6 y 20 dígitos numéricos")
    private String idUsuario;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 a 100 caracteres")
    private String nombreUsuario;

    @Email(message = "El correo debe tener un formato válido")
    @NotBlank(message = "El correo es obligatorio")
    @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
    private String correoUsuario;

    @Size(min = 8, max = 100, message = "La contraseña debe tener almenos 8 caracteres")
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenaUsuario;

}