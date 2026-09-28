package uniminuto.datronix.mapper;

import uniminuto.datronix.dto.UsuarioCreateDTO;
import uniminuto.datronix.dto.UsuarioResponseDTO;
import uniminuto.datronix.entity.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
        // Clase utilitaria: no se instancia
    }

    // CreateDTO -> Entity  (para guardar)
    public static Usuario toEntity(UsuarioCreateDTO dto) {
        if (dto == null) return null;
        return Usuario.builder()
                .idUsuario(dto.getIdUsuario())          
                .nombreUsuario(dto.getNombreUsuario())
                .correoUsuario(dto.getCorreoUsuario())
                .contrasenaUsuario(dto.getContrasenaUsuario())
                .build();
    }

    // Entity -> ResponseDTO  (para devolver al cliente)
    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        if (usuario == null) return null;
        return UsuarioResponseDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .correoUsuario(usuario.getCorreoUsuario())
                .build();
    }
}