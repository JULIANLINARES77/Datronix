package uniminuto.datronix.exception;

public class RolUsuarioInvalidoException extends RuntimeException {
    public RolUsuarioInvalidoException(String rol) {
        super("El rol '" + rol + "' no está permitido. Usa ADMINISTRADOR, OPERADOR o BODEGA.");
    }
}
