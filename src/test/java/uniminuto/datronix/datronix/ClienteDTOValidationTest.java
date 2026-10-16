package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import uniminuto.datronix.dto.ClienteDTO;

class ClienteDTOValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsMissingRequiredFieldsAndInvalidEmail() {
        ClienteDTO cliente = ClienteDTO.builder()
                .idCliente(" ")
                .nombreCliente("")
                .telefonoCliente(-1L)
                .emailCliente("no-es-correo")
                .build();

        Set<ConstraintViolation<ClienteDTO>> violations = validator.validate(cliente);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idCliente")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nombreCliente")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("telefonoCliente")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("emailCliente")));
    }

    @Test
    void acceptsValidClientDataAndOptionalEmptyEmail() {
        ClienteDTO cliente = ClienteDTO.builder()
                .idCliente("900123456")
                .nombreCliente("Distribuciones del Centro")
                .telefonoCliente(3001234567L)
                .emailCliente("")
                .direccionCliente("Bogotá")
                .tipoCliente("EMPRESA")
                .build();

        assertFalse(validator.validate(cliente).iterator().hasNext());
    }
}
