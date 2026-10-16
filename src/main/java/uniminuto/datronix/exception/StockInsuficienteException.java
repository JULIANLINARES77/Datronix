package uniminuto.datronix.exception;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(Long idProducto) {
        super("No hay existencias suficientes del producto " + idProducto + " para completar la venta.");
    }
}
