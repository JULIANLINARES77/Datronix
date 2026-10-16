package uniminuto.datronix.exception;

public class CodigoVerificacionInvalidoException extends RuntimeException {
    public CodigoVerificacionInvalidoException() {
        super("El código no es válido, expiró o alcanzó el límite de intentos.");
    }
}
