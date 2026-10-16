package uniminuto.datronix.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public final class CreditoPendienteReporteDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private final Long idVenta;
    private final String nombreCliente;
    private final LocalDate fechaVenta;
    private final BigDecimal total;
    private final long diasTranscurridos;

    public CreditoPendienteReporteDTO(
            Long idVenta,
            String nombreCliente,
            LocalDate fechaVenta,
            BigDecimal total) {
        this.idVenta = idVenta;
        this.nombreCliente = nombreCliente;
        this.fechaVenta = fechaVenta;
        this.total = total;
        this.diasTranscurridos = Math.max(0, ChronoUnit.DAYS.between(fechaVenta, LocalDate.now()));
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public LocalDate getFechaVenta() {
        return fechaVenta;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public long getDiasTranscurridos() {
        return diasTranscurridos;
    }
}
