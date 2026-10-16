package uniminuto.datronix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Describe la cantidad y el valor de un producto incluido en una venta.
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_venta", nullable = false)
    private Long idDetalleVenta;

    @ManyToOne
    @JoinColumn(name = "id_venta", referencedColumnName = "id_venta")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private Long cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Builder.Default
    @Column(name = "descuento_item", precision = 10, scale = 2)
    private BigDecimal descuentoItem = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "subtotal_item", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotalItem = BigDecimal.ZERO;
}