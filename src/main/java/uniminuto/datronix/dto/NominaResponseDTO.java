package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NominaResponseDTO {
    Long idNomina;
    String idEmpleado;
    String nombreEmpleado;
    LocalDate fechaPago;
    BigDecimal salarioPagado;
    String periodo;
}
