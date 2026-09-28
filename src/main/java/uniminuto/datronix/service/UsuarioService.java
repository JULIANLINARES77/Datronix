package uniminuto.datronix.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import uniminuto.datronix.dto.UsuarioCreateDTO;
import uniminuto.datronix.dto.UsuarioResponseDTO;
import uniminuto.datronix.entity.Usuario;
import uniminuto.datronix.exception.CredencialesInvalidasException;
import uniminuto.datronix.exception.UsuarioCorreoDuplicadoException;
import uniminuto.datronix.exception.UsuarioIdDuplicadoException;
import uniminuto.datronix.exception.UsuarioNotFoundException;
import uniminuto.datronix.mapper.UsuarioMapper;
import uniminuto.datronix.repository.UsuarioRepository;

@Service
// Contiene las consultas de usuarios y la comprobación usada por el inicio de
// sesión.
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder=passwordEncoder;
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

    public UsuarioResponseDTO guardarUsuario(UsuarioCreateDTO dto) {
        // Persiste el usuario que llegó desde el controlador.
        if (usuarioRepository.existsByIdUsuario(dto.getIdUsuario())) {

            throw new UsuarioIdDuplicadoException(dto.getIdUsuario());

        }

        if (usuarioRepository.existsByCorreoUsuario(dto.getCorreoUsuario())) {

            throw new UsuarioCorreoDuplicadoException(dto.getCorreoUsuario());

        }

        Usuario usuario = UsuarioMapper.toEntity(dto);

         // hasheo
        String hasd=passwordEncoder.encode(dto.getContrasenaUsuario());
        usuario.setContrasenaUsuario(hasd);

        Usuario creado = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDTO(creado);
    }

    public void eliminarUsuario(String id) {
        // Primero se comprueba que el registro exista para evitar un borrado
        // silencioso.
        if (!usuarioRepository.existsByIdUsuario(id)) {

            throw new UsuarioNotFoundException(id);

        } else {

            usuarioRepository.deleteById(id);

        }

    }

    public UsuarioResponseDTO autenticar(String correo, String contrasena) {
    Usuario usuario = usuarioRepository.findByCorreoUsuario(correo)
            .filter(u -> {
                String stored = u.getContrasenaUsuario();
                if (stored == null) return false;

                if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
                    // Ya es un hash BCrypt
                    return passwordEncoder.matches(contrasena, stored);
                } else {
                    // Está en texto plano → comparación legacy
                    boolean ok = stored.equals(contrasena);
                    if (ok) {
                        // Rehash y actualiza silenciosamente
                        u.setContrasenaUsuario(passwordEncoder.encode(contrasena));
                        usuarioRepository.save(u);
                    }
                    return ok;
                }
            })
            .orElseThrow(CredencialesInvalidasException::new);

    return UsuarioMapper.toResponseDTO(usuario);
}

}
