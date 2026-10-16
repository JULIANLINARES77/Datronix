package uniminuto.datronix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

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

    @Column(name = "rol_usuario", length = 20)
    private String rolUsuario;

    @Builder.Default
    @Column(name = "login_failed_attempts", nullable = false, columnDefinition = "integer default 0")
    private int loginFailedAttempts = 0;

    @Builder.Default
    @Column(name = "login_lock_level", nullable = false, columnDefinition = "integer default 0")
    private int loginLockLevel = 0;

    @Column(name = "login_locked_until")
    private Instant loginLockedUntil;
}
