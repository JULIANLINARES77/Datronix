package uniminuto.datronix.exception;

public class ProductoSkuDuplicadoException extends RuntimeException {
    public ProductoSkuDuplicadoException(String sku) {
        super("Ya existe un producto con el SKU " + sku);
    }
}
