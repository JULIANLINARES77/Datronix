package uniminuto.datronix.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uniminuto.datronix.entity.Usuario;

// Spring Data crea la implementación; aquí solo se declara la búsqueda adicional por correo.
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByCorreoUsuario(String correoUsuario);

    boolean existsByCorreoUsuario(String correoUsuario);

    boolean existsByIdUsuario(String idUsuario);
}
