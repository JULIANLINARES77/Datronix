package uniminuto.datronix.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Contiene la información de las personas que trabajan en la empresa.
public class Empleado {

    @Id
    @Column(name = "id_empleado", nullable = false)
    private String idEmpleado; // Cédula

    @Column(name = "nombre_empleado", length = 100, nullable = false)
    private String nombreEmpleado;

    @Column(name = "cargo", length = 50, nullable = false)
    private String cargo;

    @Column(name = "correo_empleado", length = 100)
    private String correoEmpleado;

    @Column(name = "telefono_empleado", length = 20)
    private String telefonoEmpleado;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "salario_base", precision = 10, scale = 2, nullable = false)
    private BigDecimal salarioBase;
}