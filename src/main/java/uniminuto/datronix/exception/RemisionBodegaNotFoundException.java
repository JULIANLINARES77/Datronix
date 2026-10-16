package uniminuto.datronix.exception;

public class RemisionBodegaNotFoundException extends RuntimeException {
    public RemisionBodegaNotFoundException(Long id) {
        super("No se encontró la remisión " + id + ".");
    }
}
