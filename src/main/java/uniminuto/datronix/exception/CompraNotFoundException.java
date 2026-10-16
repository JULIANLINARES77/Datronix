package uniminuto.datronix.exception;

public class CompraNotFoundException extends RuntimeException {
    public CompraNotFoundException(Long id) {
        super("Compra con ID " + id + " no encontrada");
    }
}
