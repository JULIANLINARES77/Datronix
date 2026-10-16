package uniminuto.datronix.dto;

import java.math.BigDecimal;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RemisionResponseDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idRemision;
    LocalDateTime fechaEmision;
    BigDecimal totalRemision;
    String observaciones;
    String estadoBodega;
    String observacionesBodega;
}
