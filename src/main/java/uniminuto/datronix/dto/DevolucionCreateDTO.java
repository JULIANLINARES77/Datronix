package uniminuto.datronix.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DevolucionCreateDTO {
    @NotNull(message = "El producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Long idProducto;

    @Size(max = 100, message = "El nombre del cliente no puede superar 100 caracteres")
    private String nombreCliente;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Long cantidad;

    @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
    private String motivo;

    @NotNull(message = "La fecha de devolución es obligatoria")
    private LocalDateTime fechaDevolucion;
}
