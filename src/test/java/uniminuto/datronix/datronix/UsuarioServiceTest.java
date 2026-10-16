package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import uniminuto.datronix.dto.UsuarioCreateDTO;
import uniminuto.datronix.entity.Usuario;
import uniminuto.datronix.exception.RolUsuarioInvalidoException;
import uniminuto.datronix.exception.UltimoAdministradorException;
import uniminuto.datronix.repository.UsuarioRepository;
import uniminuto.datronix.repository.EmailVerificationCodeRepository;
import uniminuto.datronix.service.UsuarioService;

class UsuarioServiceTest {
    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
    private final EmailVerificationCodeRepository codeRepository =
            Mockito.mock(EmailVerificationCodeRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    private final UsuarioService service = new UsuarioService(
            usuarioRepository, codeRepository, passwordEncoder);

    @Test
    void eliminarUsuarioRejectsRemovingLastAdministrator() {
        Usuario admin = Usuario.builder()
                .idUsuario("ADMIN-1")
                .rolUsuario("ADMINISTRADOR")
                .build();
        when(usuarioRepository.findById("ADMIN-1")).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRolUsuarioIgnoreCase("ADMINISTRADOR")).thenReturn(1L);

        assertThrows(UltimoAdministradorException.class, () -> service.eliminarUsuario("ADMIN-1"));
        verify(usuarioRepository, never()).delete(admin);
    }

    @Test
    void guardarUsuarioCreatesWarehouseRoleWhenRequested() {
        UsuarioCreateDTO request = new UsuarioCreateDTO();
        request.setIdUsuario("12345678");
        request.setNombreUsuario("Usuario Bodega");
        request.setCorreoUsuario("bodega@empresa.test");
        request.setContrasenaUsuario("contraseña-segura");
        request.setRolUsuario("bodega");
        when(usuarioRepository.count()).thenReturn(1L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("contraseña-segura")).thenReturn("hash");

        var response = service.guardarUsuario(request);

        assertEquals("BODEGA", response.getRolUsuario());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void guardarUsuarioCreatesOperatorRoleWhenRequested() {
        UsuarioCreateDTO request = new UsuarioCreateDTO();
        request.setIdUsuario("12345678");
        request.setNombreUsuario("Usuario Operador");
        request.setCorreoUsuario("operador@empresa.test");
        request.setContrasenaUsuario("Contraseña123");
        request.setRolUsuario("operador");
        when(usuarioRepository.count()).thenReturn(1L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("Contraseña123")).thenReturn("hash");

        var response = service.guardarUsuario(request);

        assertEquals("OPERADOR", response.getRolUsuario());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void guardarPrimerUsuarioAlwaysCreatesAdministrator() {
        UsuarioCreateDTO request = new UsuarioCreateDTO();
        request.setIdUsuario("12345678");
        request.setNombreUsuario("Administrador");
        request.setCorreoUsuario("admin@empresa.test");
        request.setContrasenaUsuario("contraseña-segura");
        request.setRolUsuario("BODEGA");
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("contraseña-segura")).thenReturn("hash");

        var response = service.guardarUsuario(request);

        assertEquals("ADMINISTRADOR", response.getRolUsuario());
    }

    @Test
    void guardarUsuarioRejectsUnknownRole() {
        UsuarioCreateDTO request = new UsuarioCreateDTO();
        request.setIdUsuario("12345678");
        request.setNombreUsuario("Usuario");
        request.setCorreoUsuario("usuario@empresa.test");
        request.setContrasenaUsuario("contraseña-segura");
        request.setRolUsuario("SUPERUSUARIO");
        when(usuarioRepository.count()).thenReturn(1L);

        assertThrows(RolUsuarioInvalidoException.class, () -> service.guardarUsuario(request));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarRolRejectsDemotingLastAdministrator() {
        Usuario admin = Usuario.builder().idUsuario("ADMIN-1").rolUsuario("ADMINISTRADOR").build();
        when(usuarioRepository.findById("ADMIN-1")).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRolUsuarioIgnoreCase("ADMINISTRADOR")).thenReturn(1L);

        assertThrows(UltimoAdministradorException.class, () -> service.actualizarRol("ADMIN-1", "BODEGA"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarRolNormalizesAndSavesSupportedRole() {
        Usuario operator = Usuario.builder().idUsuario("USER-1").rolUsuario("OPERADOR").build();
        when(usuarioRepository.findById("USER-1")).thenReturn(Optional.of(operator));
        when(usuarioRepository.save(operator)).thenReturn(operator);

        var response = service.actualizarRol("USER-1", "bodega");

        assertEquals("BODEGA", response.getRolUsuario());
    }

}
