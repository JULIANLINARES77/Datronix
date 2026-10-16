package uniminuto.datronix.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponseDTO {
    UsuarioResponseDTO usuario;
}
