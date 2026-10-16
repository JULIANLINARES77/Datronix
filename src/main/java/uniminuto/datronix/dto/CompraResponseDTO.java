package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompraResponseDTO {
    Long idCompra;
    String idProveedor;
    String nombreEmpresa;
    LocalDate fechaCompra;
    BigDecimal totalCompra;
    List<DetalleCompraResponseDTO> detalles;
}
