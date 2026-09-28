package uniminuto.datronix.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import uniminuto.datronix.dto.LoginRequest;
import uniminuto.datronix.dto.UsuarioCreateDTO;
import uniminuto.datronix.dto.UsuarioResponseDTO;
import uniminuto.datronix.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen
// Atiende el registro, las consultas y el inicio de sesión de los usuarios.
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {

        this.usuarioService = usuarioService;

    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {

        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorId(@PathVariable String id) {
        // Usa el documento o identificador recibido en la URL para buscar un usuario.
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorId(id));

    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> guardarUsuario(@Valid @RequestBody UsuarioCreateDTO dto) {
        // Guarda directamente los datos del usuario enviados por el formulario.

        UsuarioResponseDTO creado = usuarioService.guardarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String id) {
        // Solicita al servicio que elimine el usuario indicado.
        usuarioService.eliminarUsuario(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        // Comprueba las credenciales y devuelve al usuario o un aviso de acceso
        // rechazado.

        UsuarioResponseDTO usuario = usuarioService.autenticar(request.getCorreoUsuario(),
                request.getContrasenaUsuario());
        return ResponseEntity.ok(usuario);
    }

}
