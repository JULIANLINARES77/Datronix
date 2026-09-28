package uniminuto.datronix.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Manejador global de errores.
 * Captura todas las excepciones de los controladores y devuelve respuestas
 * bonitas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // Usuario no encontrado
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsuarioNotFound(UsuarioNotFoundException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("USU-001")
                .mensaje(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

    }

    // 1. Cliente no encontrado → 404
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleClienteNotFound(ClienteNotFoundException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("CLI-001")
                .mensaje(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Si el cliente existe al momento de craerlo -> 409

    @ExceptionHandler(ClienteDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleClienteDuplicado(ClienteDuplicadoException ex) {

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("CLI-002")
                .mensaje(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

    }

    // 2. Producto no encontrado → 404
    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleProductoNotFound(ProductoNotFoundException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("PROV-001")
                .mensaje(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Si el Id proveedor existe antes de crearlo -> 409

    @ExceptionHandler(ProveedorIdDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handdleProveedorIdDuplicado(ProveedorIdDuplicadoException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("PROV-002")
                .mensaje(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

    }

    // Si la empresa existe al momento de craerlo -> 409
    @ExceptionHandler(ProveedorDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handdleProveedorDuplicado(ProveedorDuplicadoException ex) {

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("PROV-003")
                .mensaje(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

    }

    @ExceptionHandler(ProveedorTelefonoDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handdleProveedorTelefonoDuplicado(ProveedorTelefonoDuplicadoException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("PROV-004")
                .mensaje(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

    }

    @ExceptionHandler(ProveedorEmailDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handdleProveedorEmailDuplicado(ProveedorEmailDuplicadoException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("PROV-005")
                .mensaje(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

    }

    // 3. Proveedor no encontrado → 404
    @ExceptionHandler(ProveedorNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleProveedorNotFound(ProveedorNotFoundException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("PROV-001")
                .mensaje(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 4. No se puede eliminar porque tiene dependencias (ej. proveedor con compras)
    // → 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("INTEG-001")
                .mensaje(
                        "Operación bloqueada por una restricción de la base de datos. Verifica que los IDs relacionados existan y que no haya duplicados.")
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 5. Cualquier otro error no controlado → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .codigo("GEN-500")
                .mensaje("Ocurrió un error interno en el servidor. Contacta al administrador.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(UsuarioIdDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsuarioIdDuplicado(UsuarioIdDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponseDTO.builder()
                        .codigo("USU-002")
                        .mensaje(ex.getMessage())
                        .build());

    }

    @ExceptionHandler(UsuarioCorreoDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsuarioCorreoDuplicado(UsuarioCorreoDuplicadoException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(

                ErrorResponseDTO.builder()
                        .codigo("USU-003")
                        .mensaje(ex.getMessage())
                        .build()

        );

    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponseDTO> handleCredencialesInvalidas(CredencialesInvalidasException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO.builder()
                        .codigo("USU-004")
                        .mensaje(ex.getMessage())
                        .build()

        );

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponseDTO.builder()
                        .codigo("VAL-001")
                        .mensaje(mensaje)
                        .build()

        );

    }

}