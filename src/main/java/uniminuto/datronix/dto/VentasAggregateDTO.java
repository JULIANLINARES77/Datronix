package uniminuto.datronix.dto;

import java.math.BigDecimal;

import lombok.Value;

@Value
public class VentasAggregateDTO {
    Long cantidad;
    BigDecimal total;
}
