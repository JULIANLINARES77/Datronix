package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RemisionResponseDTO {
    Long idRemision;
    LocalDateTime fechaEmision;
    BigDecimal totalRemision;
    String observaciones;
    String estadoBodega;
    String observacionesBodega;
}
