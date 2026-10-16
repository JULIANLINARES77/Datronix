package uniminuto.datronix.entity;

public enum EstadoRemisionBodega {
    PENDIENTE,
    EN_PREPARACION,
    DESPACHADA,
    ENTREGADA;

    public static EstadoRemisionBodega actual(EstadoRemisionBodega estado) {
        return estado == null ? PENDIENTE : estado;
    }

    public boolean permite(EstadoRemisionBodega solicitado) {
        return solicitado != null && (solicitado == this || solicitado == siguiente());
    }

    private EstadoRemisionBodega siguiente() {
        return switch (this) {
            case PENDIENTE -> EN_PREPARACION;
            case EN_PREPARACION -> DESPACHADA;
            case DESPACHADA -> ENTREGADA;
            case ENTREGADA -> null;
        };
    }
}
