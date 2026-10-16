package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class ClienteFrecuenteReporteDTO {
    private final String idCliente;
    private final String nombreCliente;
    private final Long numeroCompras;
    private final BigDecimal totalInvertido;
    private final BigDecimal promedioCompra;
    private final LocalDate ultimaCompra;

    public ClienteFrecuenteReporteDTO(
            String idCliente,
            String nombreCliente,
            Long numeroCompras,
            BigDecimal totalInvertido,
            LocalDate ultimaCompra) {
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.numeroCompras = numeroCompras;
        this.totalInvertido = totalInvertido;
        this.promedioCompra = numeroCompras == null || numeroCompras == 0 || totalInvertido == null
                ? BigDecimal.ZERO
                : totalInvertido.divide(BigDecimal.valueOf(numeroCompras), 2, RoundingMode.HALF_UP);
        this.ultimaCompra = ultimaCompra;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public Long getNumeroCompras() {
        return numeroCompras;
    }

    public BigDecimal getTotalInvertido() {
        return totalInvertido;
    }

    public BigDecimal getPromedioCompra() {
        return promedioCompra;
    }

    public LocalDate getUltimaCompra() {
        return ultimaCompra;
    }
}
