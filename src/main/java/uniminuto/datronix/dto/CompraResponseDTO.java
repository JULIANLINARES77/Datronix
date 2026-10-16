package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompraResponseDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    Long idCompra;
    String idProveedor;
    String nombreEmpresa;
    LocalDate fechaCompra;
    BigDecimal totalCompra;
    List<DetalleCompraResponseDTO> detalles;
}
