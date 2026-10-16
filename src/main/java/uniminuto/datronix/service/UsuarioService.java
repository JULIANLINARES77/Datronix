package uniminuto.datronix.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.UsuarioCreateDTO;
import uniminuto.datronix.dto.UsuarioResponseDTO;
import uniminuto.datronix.entity.Usuario;
import uniminuto.datronix.exception.CredencialesInvalidasException;
import uniminuto.datronix.exception.UsuarioCorreoDuplicadoException;
import uniminuto.datronix.exception.UsuarioIdDuplicadoException;
import uniminuto.datronix.exception.UsuarioNotFoundException;
import uniminuto.datronix.exception.UltimoAdministradorException;
import uniminuto.datronix.exception.RolUsuarioInvalidoException;
import uniminuto.datronix.mapper.UsuarioMapper;
import uniminuto.datronix.repository.UsuarioRepository;

@Service
// Contiene las consultas de usuarios y la comprobación usada por el inicio de
// sesión.
public class UsuarioService {
    private static final Set<String> ROLES_PERMITIDOS = Set.of("ADMINISTRADOR", "OPERADOR", "BODEGA");
    private static final long BLOQUEO_ADMINISTRADORES_ID = 78246109L;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UsuarioResponseDTO> listarUsuarios() {
        // Devuelve todos los usuarios que conoce el repositorio.
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioMapper::toResponseDTO)
                .collect(Collectors.toList());

    }

    public UsuarioResponseDTO buscarUsuarioPorId(String id) {
        // La búsqueda termina con un mensaje de error si el usuario no existe.
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        return UsuarioMapper.toResponseDTO(usuario);

    }

    @Transactional
    public UsuarioResponseDTO guardarUsuario(UsuarioCreateDTO dto) {
        // Serializa los registros para garantizar un solo administrador inicial.
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(" + BLOQUEO_ADMINISTRADORES_ID + ")");
        boolean primerUsuario = usuarioRepository.count() == 0;

        if (usuarioRepository.existsByIdUsuario(dto.getIdUsuario())) {
            throw new UsuarioIdDuplicadoException(dto.getIdUsuario());
        }

        if (usuarioRepository.existsByCorreoUsuario(dto.getCorreoUsuario())) {
            throw new UsuarioCorreoDuplicadoException(dto.getCorreoUsuario());
        }

        Usuario usuario = UsuarioMapper.toEntity(dto);
        usuario.setContrasenaUsuario(passwordEncoder.encode(dto.getContrasenaUsuario()));
        usuario.setRolUsuario(primerUsuario
                ? "ADMINISTRADOR"
                : normalizarRol(dto.getRolUsuario(), "OPERADOR"));

        Usuario creado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(creado);
    }

    @Transactional
    public UsuarioResponseDTO actualizarRol(String id, String rolSolicitado) {
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(" + BLOQUEO_ADMINISTRADORES_ID + ")");
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        String nuevoRol = normalizarRol(rolSolicitado, null);
        if ("ADMINISTRADOR".equalsIgnoreCase(usuario.getRolUsuario())
                && !"ADMINISTRADOR".equals(nuevoRol)
                && usuarioRepository.countByRolUsuarioIgnoreCase("ADMINISTRADOR") <= 1) {
            throw new UltimoAdministradorException();
        }
        usuario.setRolUsuario(nuevoRol);
        return UsuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminarUsuario(String id) {
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(" + BLOQUEO_ADMINISTRADORES_ID + ")");
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        if ("ADMINISTRADOR".equalsIgnoreCase(usuario.getRolUsuario())
                && usuarioRepository.countByRolUsuarioIgnoreCase("ADMINISTRADOR") <= 1) {
            throw new UltimoAdministradorException();
        }
        usuarioRepository.delete(usuario);
    }

    public Usuario autenticarUsuario(String correo, String contrasena) {
        return usuarioRepository.findByCorreoUsuario(correo)
            .filter(u -> {
                String stored = u.getContrasenaUsuario();
                if (stored == null) return false;

                if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
                    // Ya es un hash BCrypt
                    return passwordEncoder.matches(contrasena, stored);
                } else {
                    // Migra una contraseña heredada al primer inicio de sesión válido.
                    boolean ok = stored.equals(contrasena);
                    if (ok) {
                        u.setContrasenaUsuario(passwordEncoder.encode(contrasena));
                        usuarioRepository.save(u);
                    }
                    return ok;
                }
            })
            .orElseThrow(CredencialesInvalidasException::new);
    }

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioMapper.toResponseDTO(usuario);
    }

    private String normalizarRol(String rol, String rolPredeterminado) {
        if (rol == null || rol.isBlank()) {
            if (rolPredeterminado != null) {
                return rolPredeterminado;
            }
            throw new RolUsuarioInvalidoException(rol);
        }
        String normalizado = rol.trim().toUpperCase(Locale.ROOT);
        if (!ROLES_PERMITIDOS.contains(normalizado)) {
            throw new RolUsuarioInvalidoException(rol);
        }
        return normalizado;
    }

}
