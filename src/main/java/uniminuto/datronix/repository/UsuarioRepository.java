package uniminuto.datronix.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import uniminuto.datronix.entity.Usuario;

// Spring Data crea la implementación; aquí solo se declara la búsqueda adicional por correo.
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByCorreoUsuario(String correoUsuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where lower(u.correoUsuario) = lower(:correo)")
    Optional<Usuario> findByCorreoUsuarioForUpdate(@Param("correo") String correoUsuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Usuario> findFirstByRolUsuarioIgnoreCaseOrderByIdUsuarioAsc(String rolUsuario);

    boolean existsByCorreoUsuario(String correoUsuario);

    boolean existsByIdUsuario(String idUsuario);

    long countByRolUsuarioIgnoreCase(String rolUsuario);
}
