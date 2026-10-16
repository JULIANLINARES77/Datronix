package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import uniminuto.datronix.dto.EmailAddressRequestDTO;
import uniminuto.datronix.dto.EmailCodeVerificationDTO;
import uniminuto.datronix.dto.PasswordResetRequestDTO;
import uniminuto.datronix.entity.EmailVerificationCode;
import uniminuto.datronix.entity.Usuario;
import uniminuto.datronix.exception.CodigoVerificacionInvalidoException;
import uniminuto.datronix.exception.CredencialesInvalidasException;
import uniminuto.datronix.exception.CuentaBloqueadaException;
import uniminuto.datronix.repository.EmailVerificationCodeRepository;
import uniminuto.datronix.repository.UsuarioRepository;
import uniminuto.datronix.service.AuthenticationService;

class AuthenticationServiceTest {
    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);
    private final EmailVerificationCodeRepository codeRepository =
            Mockito.mock(EmailVerificationCodeRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    private final JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);
    private final AuthenticationService service = new AuthenticationService(
            usuarioRepository, codeRepository, passwordEncoder, mailSender, "no-reply@datronix.test");

    @Test
    void locksAfterThreeIncorrectPasswordsAndRejectsLoginDuringLock() {
        Usuario usuario = usuario("user@example.test", "$2a$bcrypt-hash");
        when(usuarioRepository.findByCorreoUsuarioForUpdate("user@example.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "$2a$bcrypt-hash")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(CredencialesInvalidasException.class, () -> service.autenticar("user@example.test", "incorrecta"));
        assertThrows(CredencialesInvalidasException.class, () -> service.autenticar("user@example.test", "incorrecta"));
        CuentaBloqueadaException blocked = assertThrows(CuentaBloqueadaException.class,
                () -> service.autenticar("user@example.test", "incorrecta"));

        assertEquals(1, usuario.getLoginLockLevel());
        assertEquals(0, usuario.getLoginFailedAttempts());
        assertNotNull(usuario.getLoginLockedUntil());
        assertEquals(usuario.getLoginLockedUntil(), blocked.getBloqueadaHasta());
        assertLockDuration(blocked.getBloqueadaHasta(), 5);
        assertThrows(CuentaBloqueadaException.class, () -> service.autenticar("user@example.test", "correcta"));
        verify(passwordEncoder, never()).matches("correcta", "$2a$bcrypt-hash");
    }

    @Test
    void eachIncorrectPasswordAfterAnExpiredLockAdvancesTheLockDuration() {
        Usuario usuario = usuario("user@example.test", "$2a$bcrypt-hash");
        usuario.setLoginLockLevel(1);
        usuario.setLoginLockedUntil(Instant.now().minusSeconds(1));
        when(usuarioRepository.findByCorreoUsuarioForUpdate("user@example.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "$2a$bcrypt-hash")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(CuentaBloqueadaException.class, () -> service.autenticar("user@example.test", "incorrecta"));
        assertEquals(2, usuario.getLoginLockLevel());
        assertLockDuration(usuario.getLoginLockedUntil(), 30);

        usuario.setLoginLockedUntil(Instant.now().minusSeconds(1));
        assertThrows(CuentaBloqueadaException.class, () -> service.autenticar("user@example.test", "incorrecta"));
        assertEquals(3, usuario.getLoginLockLevel());
        assertLockDuration(usuario.getLoginLockedUntil(), 60);

        usuario.setLoginLockedUntil(Instant.now().minusSeconds(1));
        assertThrows(CuentaBloqueadaException.class, () -> service.autenticar("user@example.test", "incorrecta"));
        assertEquals(4, usuario.getLoginLockLevel());
        assertLockDuration(usuario.getLoginLockedUntil(), 1440);
    }

    @Test
    void successfulLoginResetsFailureAndEscalationCounters() {
        Usuario usuario = usuario("user@example.test", "$2a$bcrypt-hash");
        usuario.setLoginFailedAttempts(2);
        when(usuarioRepository.findByCorreoUsuarioForUpdate("user@example.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("correcta", "$2a$bcrypt-hash")).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.autenticar("USER@example.test", "correcta");

        assertEquals(0, usuario.getLoginFailedAttempts());
        assertEquals(0, usuario.getLoginLockLevel());
        assertNull(usuario.getLoginLockedUntil());
    }

    @Test
    void plaintextStoredPasswordsAreNotAccepted() {
        Usuario usuario = usuario("user@example.test", "plain-text-password");
        when(usuarioRepository.findByCorreoUsuarioForUpdate("user@example.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("plain-text-password", "plain-text-password")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(CredencialesInvalidasException.class,
                () -> service.autenticar("user@example.test", "plain-text-password"));

        verify(passwordEncoder).matches("plain-text-password", "plain-text-password");
        assertEquals(1, usuario.getLoginFailedAttempts());
    }

    @Test
    void unlockCodeIsSentAndVerificationClearsTheLock() {
        Usuario usuario = usuario("user@example.test", "bcrypt-hash");
        usuario.setLoginLockLevel(1);
        when(usuarioRepository.findByCorreoUsuarioForUpdate("user@example.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(any())).thenReturn("code-hash");
        when(codeRepository.save(any(EmailVerificationCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(codeRepository.findLatestForUpdate("USER-1", EmailVerificationCode.ACCOUNT_UNLOCK))
                .thenAnswer(invocation -> code("USER-1", EmailVerificationCode.ACCOUNT_UNLOCK, "code-hash"));
        when(passwordEncoder.matches("123456", "code-hash")).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailAddressRequestDTO request = new EmailAddressRequestDTO();
        request.setCorreoUsuario("user@example.test");
        service.solicitarDesbloqueo(request);

        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mail.capture());
        assertTrue(mail.getValue().getText().matches("(?s).*\\b\\d{6}\\b.*"));

        EmailCodeVerificationDTO verification = new EmailCodeVerificationDTO();
        verification.setCorreoUsuario("user@example.test");
        verification.setCodigo("123456");
        service.verificarDesbloqueo(verification);

        assertEquals(0, usuario.getLoginLockLevel());
        assertNull(usuario.getLoginLockedUntil());
        verify(codeRepository).deleteAllByUsuario_IdUsuarioAndPurpose("USER-1",
                EmailVerificationCode.ACCOUNT_UNLOCK);
    }

    @Test
    void passwordResetConsumesCodeAndReplacesThePasswordHash() {
        Usuario usuario = usuario("user@example.test", "old-hash");
        EmailVerificationCode code = verificationCode(
                "USER-1", EmailVerificationCode.PASSWORD_RESET, "code-hash");
        when(usuarioRepository.findByCorreoUsuarioForUpdate("user@example.test")).thenReturn(Optional.of(usuario));
        when(codeRepository.findLatestForUpdate("USER-1", EmailVerificationCode.PASSWORD_RESET))
                .thenReturn(Optional.of(code));
        when(passwordEncoder.matches("123456", "code-hash")).thenReturn(true);
        when(passwordEncoder.encode("NuevaClave8")).thenReturn("new-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetRequestDTO request = new PasswordResetRequestDTO();
        request.setCorreoUsuario("user@example.test");
        request.setCodigo("123456");
        request.setNuevaContrasena("NuevaClave8");
        service.restablecerContrasena(request);

        assertEquals("new-hash", usuario.getContrasenaUsuario());
        assertEquals(0, usuario.getLoginLockLevel());
        verify(codeRepository).delete(code);
    }

    @Test
    void wrongVerificationCodeConsumesAnAttempt() {
        Usuario usuario = usuario("user@example.test", "bcrypt-hash");
        EmailVerificationCode code = verificationCode(
                "USER-1", EmailVerificationCode.ACCOUNT_UNLOCK, "code-hash");
        when(usuarioRepository.findByCorreoUsuarioForUpdate("user@example.test")).thenReturn(Optional.of(usuario));
        when(codeRepository.findLatestForUpdate("USER-1", EmailVerificationCode.ACCOUNT_UNLOCK))
                .thenReturn(Optional.of(code));
        when(passwordEncoder.matches("000000", "code-hash")).thenReturn(false);

        EmailCodeVerificationDTO request = new EmailCodeVerificationDTO();
        request.setCorreoUsuario("user@example.test");
        request.setCodigo("000000");

        assertThrows(CodigoVerificacionInvalidoException.class, () -> service.verificarDesbloqueo(request));
        assertEquals(1, code.getAttempts());
        verify(codeRepository).save(code);
    }

    private Usuario usuario(String correo, String passwordHash) {
        return Usuario.builder()
                .idUsuario("USER-1")
                .nombreUsuario("Usuario de prueba")
                .correoUsuario(correo)
                .contrasenaUsuario(passwordHash)
                .rolUsuario("OPERADOR")
                .build();
    }

    private Optional<EmailVerificationCode> code(String userId, String purpose, String hash) {
        return Optional.of(verificationCode(userId, purpose, hash));
    }

    private EmailVerificationCode verificationCode(String userId, String purpose, String hash) {
        Usuario usuario = usuario("user@example.test", "bcrypt-hash");
        usuario.setIdUsuario(userId);
        EmailVerificationCode code = new EmailVerificationCode();
        code.setUsuario(usuario);
        code.setPurpose(purpose);
        code.setCodeHash(hash);
        code.setCreatedAt(Instant.now());
        code.setExpiresAt(Instant.now().plusSeconds(120));
        return code;
    }

    private void assertLockDuration(Instant lockedUntil, long expectedMinutes) {
        Instant now = Instant.now();
        assertTrue(lockedUntil.isAfter(now.plusSeconds(expectedMinutes * 60 - 1)));
        assertTrue(lockedUntil.isBefore(now.plusSeconds(expectedMinutes * 60 + 1)));
    }
}
