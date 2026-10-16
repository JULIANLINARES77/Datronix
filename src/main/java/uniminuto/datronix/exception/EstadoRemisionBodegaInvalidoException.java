package uniminuto.datronix.exception;

public class EstadoRemisionBodegaInvalidoException extends RuntimeException {
    public EstadoRemisionBodegaInvalidoException(String estadoActual, String estadoSolicitado) {
        super("No se puede cambiar la remisión de " + estadoActual + " a " + estadoSolicitado
                + ". Avanza un estado a la vez: PENDIENTE, EN_PREPARACION, DESPACHADA, ENTREGADA.");
    }
}
