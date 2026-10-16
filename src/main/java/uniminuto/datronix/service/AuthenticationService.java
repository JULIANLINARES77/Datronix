package uniminuto.datronix.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.EmailAddressRequestDTO;
import uniminuto.datronix.dto.EmailCodeVerificationDTO;
import uniminuto.datronix.dto.PasswordResetRequestDTO;
import uniminuto.datronix.entity.EmailVerificationCode;
import uniminuto.datronix.entity.Usuario;
import uniminuto.datronix.exception.CodigoVerificacionInvalidoException;
import uniminuto.datronix.exception.CorreoNoDisponibleException;
import uniminuto.datronix.exception.CredencialesInvalidasException;
import uniminuto.datronix.exception.CuentaBloqueadaException;
import uniminuto.datronix.mapper.UsuarioMapper;
import uniminuto.datronix.dto.UsuarioResponseDTO;
import uniminuto.datronix.repository.EmailVerificationCodeRepository;
import uniminuto.datronix.repository.UsuarioRepository;

@Service
public class AuthenticationService {
    private static final int MAX_INTENTOS_INICIALES = 3;
    private static final int MAX_INTENTOS_CODIGO = 5;
    private static final Duration VIGENCIA_CODIGO = Duration.ofMinutes(2);
    private static final Duration FRECUENCIA_ENVIO_CODIGO = Duration.ofMinutes(2);
    private static final Duration[] DURACIONES_BLOQUEO = {
            Duration.ofMinutes(5),
            Duration.ofMinutes(30),
            Duration.ofHours(1),
            Duration.ofHours(24)
    };
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String MENSAJE_SOLICITUD =
            "Si el correo corresponde a una cuenta y puede recibir un código, recibirás instrucciones en breve.";

    private final UsuarioRepository usuarioRepository;
    private final EmailVerificationCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String mailFrom;

    public AuthenticationService(
            UsuarioRepository usuarioRepository,
            EmailVerificationCodeRepository codeRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String mailFrom) {
        this.usuarioRepository = usuarioRepository;
        this.codeRepository = codeRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    @Transactional(noRollbackFor = {CredencialesInvalidasException.class, CuentaBloqueadaException.class})
    public Usuario autenticar(String correo, String contrasena) {
        Usuario usuario = usuarioRepository.findByCorreoUsuarioForUpdate(normalizarCorreo(correo))
                .orElseThrow(CredencialesInvalidasException::new);
        Instant now = Instant.now();
        if (usuario.getLoginLockedUntil() != null && usuario.getLoginLockedUntil().isAfter(now)) {
            throw new CuentaBloqueadaException(usuario.getLoginLockedUntil());
        }

        if (!contrasenaValida(usuario, contrasena)) {
            registrarFallo(usuario, now);
            usuarioRepository.save(usuario);
            if (usuario.getLoginLockedUntil() != null && usuario.getLoginLockedUntil().isAfter(now)) {
                throw new CuentaBloqueadaException(usuario.getLoginLockedUntil());
            }
            throw new CredencialesInvalidasException();
        }

        usuario.setLoginFailedAttempts(0);
        usuario.setLoginLockLevel(0);
        usuario.setLoginLockedUntil(null);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public String solicitarDesbloqueo(EmailAddressRequestDTO request) {
        usuarioRepository.findByCorreoUsuarioForUpdate(normalizarCorreo(request.getCorreoUsuario()))
                .filter(usuario -> usuario.getLoginLockLevel() > 0)
                .ifPresent(usuario -> emitirCodigo(
                        usuario, EmailVerificationCode.ACCOUNT_UNLOCK, "verificar el acceso a tu cuenta"));
        return MENSAJE_SOLICITUD;
    }

    @Transactional(noRollbackFor = CodigoVerificacionInvalidoException.class)
    public UsuarioResponseDTO verificarDesbloqueo(EmailCodeVerificationDTO request) {
        Usuario usuario = usuarioRepository.findByCorreoUsuarioForUpdate(normalizarCorreo(request.getCorreoUsuario()))
                .orElseThrow(CodigoVerificacionInvalidoException::new);
        consumirCodigo(usuario, EmailVerificationCode.ACCOUNT_UNLOCK, request.getCodigo());
        usuario.setLoginFailedAttempts(0);
        usuario.setLoginLockLevel(0);
        usuario.setLoginLockedUntil(null);
        return UsuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public String solicitarRestablecimiento(EmailAddressRequestDTO request) {
        usuarioRepository.findByCorreoUsuarioForUpdate(normalizarCorreo(request.getCorreoUsuario()))
                .ifPresent(usuario -> emitirCodigo(
                        usuario, EmailVerificationCode.PASSWORD_RESET, "restablecer la contraseña de tu cuenta"));
        return MENSAJE_SOLICITUD;
    }

    @Transactional(noRollbackFor = CodigoVerificacionInvalidoException.class)
    public void restablecerContrasena(PasswordResetRequestDTO request) {
        Usuario usuario = usuarioRepository.findByCorreoUsuarioForUpdate(normalizarCorreo(request.getCorreoUsuario()))
                .orElseThrow(CodigoVerificacionInvalidoException::new);
        consumirCodigo(usuario, EmailVerificationCode.PASSWORD_RESET, request.getCodigo());
        usuario.setContrasenaUsuario(passwordEncoder.encode(request.getNuevaContrasena()));
        usuario.setLoginFailedAttempts(0);
        usuario.setLoginLockLevel(0);
        usuario.setLoginLockedUntil(null);
        usuarioRepository.save(usuario);
    }

    private void emitirCodigo(Usuario usuario, String purpose, String operation) {
        Instant now = Instant.now();
        Optional<EmailVerificationCode> active = codeRepository
                .findFirstByUsuario_IdUsuarioAndPurposeOrderByCreatedAtDesc(usuario.getIdUsuario(), purpose);
        if (active.isPresent() && active.get().getCreatedAt().isAfter(now.minus(FRECUENCIA_ENVIO_CODIGO))) {
            return;
        }

        codeRepository.deleteAllByUsuario_IdUsuarioAndPurpose(usuario.getIdUsuario(), purpose);
        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        EmailVerificationCode verification = new EmailVerificationCode();
        verification.setUsuario(usuario);
        verification.setPurpose(purpose);
        verification.setCodeHash(passwordEncoder.encode(code));
        verification.setCreatedAt(now);
        verification.setExpiresAt(now.plus(VIGENCIA_CODIGO));
        verification.setAttempts(0);
        codeRepository.save(verification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(usuario.getCorreoUsuario());
        message.setSubject("Código de seguridad de Datronix");
        message.setText("Tu código para " + operation + " es: " + code
                + "\n\nVence en 2 minutos. Si no solicitaste este código, ignora este mensaje.");
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new CorreoNoDisponibleException(ex);
        }
    }

    private void consumirCodigo(Usuario usuario, String purpose, String candidate) {
        EmailVerificationCode verification = codeRepository
                .findLatestForUpdate(usuario.getIdUsuario(), purpose)
                .orElseThrow(CodigoVerificacionInvalidoException::new);
        if (!verification.getExpiresAt().isAfter(Instant.now())
                || verification.getAttempts() >= MAX_INTENTOS_CODIGO) {
            codeRepository.delete(verification);
            throw new CodigoVerificacionInvalidoException();
        }
        if (!passwordEncoder.matches(candidate, verification.getCodeHash())) {
            verification.setAttempts(verification.getAttempts() + 1);
            codeRepository.save(verification);
            throw new CodigoVerificacionInvalidoException();
        }
        codeRepository.delete(verification);
    }

    private boolean contrasenaValida(Usuario usuario, String candidate) {
        String stored = usuario.getContrasenaUsuario();
        if (stored == null) {
            return false;
        }
        return passwordEncoder.matches(candidate, stored);
    }

    private void registrarFallo(Usuario usuario, Instant now) {
        if (usuario.getLoginLockLevel() == 0) {
            usuario.setLoginFailedAttempts(usuario.getLoginFailedAttempts() + 1);
            if (usuario.getLoginFailedAttempts() < MAX_INTENTOS_INICIALES) {
                return;
            }
            usuario.setLoginFailedAttempts(0);
            usuario.setLoginLockLevel(1);
        } else {
            usuario.setLoginLockLevel(Math.min(usuario.getLoginLockLevel() + 1, DURACIONES_BLOQUEO.length));
        }
        Duration duration = DURACIONES_BLOQUEO[usuario.getLoginLockLevel() - 1];
        usuario.setLoginLockedUntil(now.plus(duration));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
