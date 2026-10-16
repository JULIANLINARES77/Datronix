package uniminuto.datronix.exception;

public class CompraTotalExcedidoException extends RuntimeException {
    public CompraTotalExcedidoException() {
        super("El total de la compra supera el máximo permitido.");
    }
}
