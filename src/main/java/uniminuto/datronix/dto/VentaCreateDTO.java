package uniminuto.datronix.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VentaCreateDTO {
    @NotBlank(message = "El cliente es obligatorio")
    @Size(max = 20, message = "El ID del cliente no puede superar 20 caracteres")
    private String idCliente;

    @NotNull(message = "La fecha de venta es obligatoria")
    private LocalDate fechaVenta;

    @NotBlank(message = "El estado de pago es obligatorio")
    @Pattern(regexp = "CONTADO|CREDITO", message = "El estado de pago debe ser CONTADO o CREDITO")
    private String estadoPago;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    private String direccionEntrega;

    @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
    private String observacionesRemision;

    @NotEmpty(message = "La venta debe incluir al menos un producto")
    @Size(max = 100, message = "Una venta no puede incluir más de 100 líneas")
    @Valid
    private List<DetalleVentaRequestDTO> detalles;
}
