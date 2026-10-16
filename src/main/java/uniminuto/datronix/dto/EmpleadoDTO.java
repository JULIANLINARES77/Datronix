package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmpleadoDTO {
    @NotBlank(message = "El documento del empleado es obligatorio")
    @Size(max = 20, message = "El documento no puede superar 20 caracteres")
    private String idEmpleado;

    @NotBlank(message = "El nombre del empleado es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombreEmpleado;

    @NotBlank(message = "El cargo es obligatorio")
    @Size(max = 50, message = "El cargo no puede superar 50 caracteres")
    private String cargo;

    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    private String correoEmpleado;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefonoEmpleado;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;

    @NotNull(message = "El salario base es obligatorio")
    @DecimalMin(value = "0.01", message = "El salario base debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "El salario admite máximo 8 enteros y 2 decimales")
    private BigDecimal salarioBase;
}
