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
// Define los datos de proveedor que necesita el formulario y la API.
public class ProveedorDTO {
    @NotBlank(message = "El documento del proveedor es obligatorio")
    @Size(max = 20, message = "El documento no puede superar 20 caracteres")
    private String idProveedor;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 100, message = "El nombre de la empresa no puede superar 100 caracteres")
    private String nombreEmpresa;

    @Size(max = 100, message = "El contacto no puede superar 100 caracteres")
    private String contactoProveedor;

    @NotNull(message = "El teléfono del proveedor es obligatorio")
    @Positive(message = "El teléfono debe ser un número positivo")
    @Digits(integer = 15, fraction = 0, message = "El teléfono debe tener máximo 15 dígitos")
    private Long telefonoProveedor;

    @Email(message = "El correo del proveedor debe tener un formato válido")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    private String emailProveedor;

    @Size(max = 150, message = "La dirección no puede superar 150 caracteres")
    private String direccionProveedor;
}
