package uniminuto.datronix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @Column(name = "idusuario", nullable = false, length = 20, updatable = false)
    private String idUsuario; // Cédula / documento — lo envía el cliente, no se genera

    @Column(name = "nombreusuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Column(name = "correousuario", nullable = false, length = 255, unique = true)
    private String correoUsuario;

    @Column(name = "contrasenausuario", nullable = false, length = 255)
    private String contrasenaUsuario;
}
