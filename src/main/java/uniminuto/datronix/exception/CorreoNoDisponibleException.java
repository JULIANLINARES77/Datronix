package uniminuto.datronix.exception;

public class CorreoNoDisponibleException extends RuntimeException {
    public CorreoNoDisponibleException(Throwable cause) {
        super("No fue posible enviar el correo de verificación. Inténtalo nuevamente más tarde.", cause);
    }
}
