package uniminuto.datronix.exception;

import java.time.Instant;

public class CuentaBloqueadaException extends RuntimeException {
    private final Instant bloqueadaHasta;

    public CuentaBloqueadaException(Instant bloqueadaHasta) {
        super("La cuenta está bloqueada. Verifica tu correo para recuperar el acceso.");
        this.bloqueadaHasta = bloqueadaHasta;
    }

    public Instant getBloqueadaHasta() {
        return bloqueadaHasta;
    }
}
