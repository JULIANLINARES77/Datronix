package uniminuto.datronix.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Es el formato ligero con el que el frontend recibe y envía clientes.
public class ClienteDTO {
    @NotBlank(message = "El documento del cliente es obligatorio")
    @Size(max = 20, message = "El documento no puede superar 20 caracteres")
    private String idCliente;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombreCliente;

    @NotNull(message = "El teléfono del cliente es obligatorio")
    @Positive(message = "El teléfono debe ser un número positivo")
    @Digits(integer = 15, fraction = 0, message = "El teléfono debe tener máximo 15 dígitos")
    private Long telefonoCliente;

    @Email(message = "El correo del cliente debe tener un formato válido")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    private String emailCliente;

    @Size(max = 150, message = "La dirección no puede superar 150 caracteres")
    private String direccionCliente;

    @Size(max = 20, message = "El tipo de cliente no puede superar 20 caracteres")
    private String tipoCliente;
}