package uniminuto.datronix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UsuarioRoleUpdateDTO {

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "(?i)ADMINISTRADOR|OPERADOR|BODEGA", message = "El rol debe ser ADMINISTRADOR, OPERADOR o BODEGA")
    private String rolUsuario;
}
