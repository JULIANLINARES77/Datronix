package uniminuto.datronix.exception;

public class VentaTotalExcedidoException extends RuntimeException {
    public VentaTotalExcedidoException() {
        super("El total de la venta supera el máximo permitido.");
    }
}
