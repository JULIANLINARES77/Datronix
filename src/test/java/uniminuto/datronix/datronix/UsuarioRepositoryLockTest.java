package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import uniminuto.datronix.entity.Usuario;
import uniminuto.datronix.repository.UsuarioRepository;

@DataJpaTest
class UsuarioRepositoryLockTest {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findsAndLocksTheFirstAdministrator() {
        usuarioRepository.save(Usuario.builder()
                .idUsuario("10000001")
                .nombreUsuario("Administrador")
                .correoUsuario("admin@example.test")
                .contrasenaUsuario("hash")
                .rolUsuario("ADMINISTRADOR")
                .build());

        var administrator = usuarioRepository
                .findFirstByRolUsuarioIgnoreCaseOrderByIdUsuarioAsc("ADMINISTRADOR");

        assertTrue(administrator.isPresent());
        assertEquals("10000001", administrator.get().getIdUsuario());
    }
}
