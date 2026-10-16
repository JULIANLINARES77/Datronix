package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NominaCreateDTO {
    @NotBlank(message = "El empleado es obligatorio")
    @Size(max = 20, message = "El documento del empleado no puede superar 20 caracteres")
    private String idEmpleado;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate fechaPago;

    @NotNull(message = "El salario pagado es obligatorio")
    @DecimalMin(value = "0.01", message = "El salario pagado debe ser mayor que cero")
    @Digits(integer = 8, fraction = 2, message = "El salario admite máximo 8 enteros y 2 decimales")
    private BigDecimal salarioPagado;

    @NotBlank(message = "El período es obligatorio")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "El período debe tener formato YYYY-MM")
    private String periodo;
}
