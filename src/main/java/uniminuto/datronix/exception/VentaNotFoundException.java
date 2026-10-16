package uniminuto.datronix.exception;

public class VentaNotFoundException extends RuntimeException {
    public VentaNotFoundException(Long id) {
        super("Venta con ID " + id + " no encontrada");
    }
}
