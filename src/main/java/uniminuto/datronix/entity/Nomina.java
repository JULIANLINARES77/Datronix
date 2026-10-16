package uniminuto.datronix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "nomina")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Guarda los datos de pago o nómina relacionados con un empleado.
public class Nomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nomina", nullable = false)
    private Long idNomina;

    @ManyToOne
    @JoinColumn(name = "id_empleado", referencedColumnName = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "salario_pagado", precision = 10, scale = 2, nullable = false)
    private BigDecimal salarioPagado;

    @Column(name = "periodo", length = 7, nullable = false) // Ej. "2026-08"
    private String periodo;
}