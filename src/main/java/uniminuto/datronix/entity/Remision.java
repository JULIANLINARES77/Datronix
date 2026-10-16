package uniminuto.datronix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "remision")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Representa el documento que acompaña o entrega una operación de mercancía.
public class Remision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_remision", nullable = false)
    private Long idRemision;

    @OneToOne
    @JoinColumn(name = "id_venta", referencedColumnName = "id_venta", nullable = false)
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "total_remision", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalRemision;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_bodega", length = 20)
    private EstadoRemisionBodega estadoBodega = EstadoRemisionBodega.PENDIENTE;

    @Column(name = "observaciones_bodega", length = 500)
    private String observacionesBodega;
}