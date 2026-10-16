package uniminuto.datronix.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Define los datos de producto que viajan entre la pantalla y el backend.
public class ProductoDTO {
    private Long idProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombreProducto;

    @Size(max = 50, message = "La categoría no puede superar 50 caracteres")
    private String categoriaProducto;

    @Size(max = 50, message = "La marca no puede superar 50 caracteres")
    private String marcaProducto;

    @Size(max = 50, message = "El modelo no puede superar 50 caracteres")
    private String modeloProducto;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcionProducto;

    @Size(max = 255, message = "Las especificaciones no pueden superar 255 caracteres")
    private String especificacionesProducto;

    @NotNull(message = "El precio de costo es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio de costo no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio de costo admite máximo 8 enteros y 2 decimales")
    private BigDecimal precioCostoProducto;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio de venta no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio de venta admite máximo 8 enteros y 2 decimales")
    private BigDecimal precioVentaProducto;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Long stockProducto;

    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Long stockMinimoProducto;

    @NotBlank(message = "El proveedor es obligatorio")
    @Size(max = 20, message = "El ID del proveedor no puede superar 20 caracteres")
    private String idProveedor;

    @Size(max = 50, message = "El SKU no puede superar 50 caracteres")
    private String skuProducto;
}