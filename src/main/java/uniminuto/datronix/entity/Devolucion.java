package uniminuto.datronix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "devolucion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Registra la devolución de productos asociada a una operación comercial.
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion", nullable = false)
    private Long idDevolucion;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;  // Relación con la entidad Producto

    @Column(name = "nombre_cliente", length = 100)
    private String nombreCliente;  // Nombre del cliente que devuelve (solo para registro)

    @Column(name = "cantidad", nullable = false)
    private Long cantidad;  // Cantidad devuelta

    @Column(name = "motivo", length = 255)
    private String motivo;  // Motivo de la devolución

    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDateTime fechaDevolucion;  // Fecha y hora de la devolución

    // @Column(name = "usuario_registro", length = 100)
    // private String usuarioRegistro;
}