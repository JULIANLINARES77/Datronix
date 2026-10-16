package uniminuto.datronix.exception;

public class EmpleadoDuplicadoException extends RuntimeException {
    public EmpleadoDuplicadoException(String id) {
        super("Ya existe un empleado con documento " + id);
    }
}
