package uniminuto.datronix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "soporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Registra una solicitud o caso de soporte dentro del sistema.
public class Soporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_soporte", nullable = false)
    private Long idSoporte;

    @Column(name = "empresa", length = 100, nullable = false)
    private String empresa;

    @Column(name = "id_remitente", length = 20, nullable = false)
    private String idRemitente; // Cédula

    @Column(name = "nombre_remitente", length = 100)
    private String nombreRemitente;

    @Column(name = "telefono_remitente", length = 20, nullable = false)
    private String telefonoRemitente;

    @Column(name = "asunto", length = 100, nullable = false)
    private String asunto;

    @Column(name = "descripcion", length = 500, nullable = false)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}