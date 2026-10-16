package uniminuto.datronix.exception;

public class StockLimiteExcedidoException extends RuntimeException {
    public StockLimiteExcedidoException(Long idProducto) {
        super("La cantidad de inventario del producto " + idProducto + " supera el máximo permitido.");
    }
}
