package uniminuto.datronix.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompraCreateDTO {
    @NotBlank(message = "El proveedor es obligatorio")
    @Size(max = 20, message = "El ID del proveedor no puede superar 20 caracteres")
    private String idProveedor;

    @NotNull(message = "La fecha de compra es obligatoria")
    private LocalDate fechaCompra;

    @NotEmpty(message = "La compra debe incluir al menos un producto")
    @Size(max = 100, message = "Una compra no puede incluir más de 100 líneas")
    @Valid
    private List<DetalleCompraRequestDTO> detalles;
}
