package uniminuto.datronix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import uniminuto.datronix.mapper.UsuarioMapper;
import uniminuto.datronix.dto.LoginRequest;
import uniminuto.datronix.dto.LoginResponseDTO;
import uniminuto.datronix.dto.AuthMessageDTO;
import uniminuto.datronix.dto.EmailAddressRequestDTO;
import uniminuto.datronix.dto.EmailCodeVerificationDTO;
import uniminuto.datronix.dto.PasswordResetRequestDTO;
import uniminuto.datronix.dto.UsuarioResponseDTO;
import uniminuto.datronix.service.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        UsuarioResponseDTO usuario = UsuarioMapper.toResponseDTO(authenticationService.autenticar(
                request.getCorreoUsuario(),
                request.getContrasenaUsuario()));
        return ResponseEntity.ok(LoginResponseDTO.builder().usuario(usuario).build());
    }

    @PostMapping("/unlock/request")
    public ResponseEntity<AuthMessageDTO> solicitarDesbloqueo(
            @Valid @RequestBody EmailAddressRequestDTO request) {
        return ResponseEntity.accepted().body(AuthMessageDTO.builder()
                .mensaje(authenticationService.solicitarDesbloqueo(request))
                .build());
    }

    @PostMapping("/unlock/verify")
    public ResponseEntity<LoginResponseDTO> verificarDesbloqueo(
            @Valid @RequestBody EmailCodeVerificationDTO request) {
        return ResponseEntity.ok(LoginResponseDTO.builder()
                .usuario(authenticationService.verificarDesbloqueo(request))
                .build());
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<AuthMessageDTO> solicitarRestablecimiento(
            @Valid @RequestBody EmailAddressRequestDTO request) {
        return ResponseEntity.accepted().body(AuthMessageDTO.builder()
                .mensaje(authenticationService.solicitarRestablecimiento(request))
                .build());
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<AuthMessageDTO> restablecerContrasena(
            @Valid @RequestBody PasswordResetRequestDTO request) {
        authenticationService.restablecerContrasena(request);
        return ResponseEntity.ok(AuthMessageDTO.builder()
                .mensaje("La contraseña se actualizó. Ya puedes iniciar sesión.")
                .build());
    }
}
