package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NominaResponseDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idNomina;
    String idEmpleado;
    String nombreEmpleado;
    LocalDate fechaPago;
    BigDecimal salarioPagado;
    String periodo;
}
