package uniminuto.datronix.exception;

public class EmpleadoNotFoundException extends RuntimeException {
    public EmpleadoNotFoundException(String id) {
        super("Empleado con documento " + id + " no encontrado");
    }
}
