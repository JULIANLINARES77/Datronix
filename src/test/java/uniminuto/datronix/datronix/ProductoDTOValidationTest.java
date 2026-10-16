package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import uniminuto.datronix.dto.ProductoDTO;

class ProductoDTOValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsNegativeInventoryAndMissingSupplier() {
        ProductoDTO producto = ProductoDTO.builder()
                .nombreProducto("Router")
                .precioCostoProducto(new BigDecimal("20.00"))
                .precioVentaProducto(new BigDecimal("30.00"))
                .stockProducto(-1L)
                .stockMinimoProducto(0L)
                .build();

        assertTrue(validator.validate(producto).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("stockProducto")));
        assertTrue(validator.validate(producto).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("idProveedor")));
    }
}
