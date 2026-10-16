package uniminuto.datronix.exception;

public class UltimoAdministradorException extends RuntimeException {
    public UltimoAdministradorException() {
        super("No se puede eliminar el último administrador del sistema.");
    }
}
