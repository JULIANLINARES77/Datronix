package uniminuto.datronix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import uniminuto.datronix.dto.LoginRequest;
import uniminuto.datronix.dto.LoginResponseDTO;
import uniminuto.datronix.dto.UsuarioResponseDTO;
import uniminuto.datronix.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioService usuarioService;
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        UsuarioResponseDTO usuario = usuarioService.toResponseDTO(usuarioService.autenticarUsuario(
                request.getCorreoUsuario(),
                request.getContrasenaUsuario()));
        return ResponseEntity.ok(LoginResponseDTO.builder().usuario(usuario).build());
    }
}
