package uniminuto.datronix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Representa una venta, su cliente y la lista de productos vendidos.
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id_cliente")
    private Cliente cliente;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta;

    @Column(name = "total_venta", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalVenta;

    @Column(name = "estado_pago", length = 20)
    private String estadoPago;

    @Column(name = "direccion_entrega", length = 255)
    private String direccionEntrega;

    @Builder.Default
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DetalleVenta> detalleVentaList = new ArrayList<>();


    // Métodos helper para sincronizar la relación bidireccional
    public void addDetalle(DetalleVenta detalle) {
        // Mantener ambos lados evita que el detalle quede desconectado de la venta.
        detalleVentaList.add(detalle);
        detalle.setVenta(this);
    }

    public void removeDetalle(DetalleVenta detalle) {
        // Al retirar el detalle también se limpia su referencia a esta venta.
        detalleVentaList.remove(detalle);
        detalle.setVenta(null);
    }
}