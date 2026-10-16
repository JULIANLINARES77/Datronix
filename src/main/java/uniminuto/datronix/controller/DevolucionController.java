package uniminuto.datronix.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import uniminuto.datronix.dto.DevolucionCreateDTO;
import uniminuto.datronix.dto.DevolucionResponseDTO;
import uniminuto.datronix.service.DevolucionService;

@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionController {
    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @GetMapping
    public ResponseEntity<List<DevolucionResponseDTO>> listarDevoluciones() {
        return ResponseEntity.ok(devolucionService.listarDevoluciones());
    }

    @PostMapping
    public ResponseEntity<DevolucionResponseDTO> registrarDevolucion(
            @Valid @RequestBody DevolucionCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucionService.registrarDevolucion(request));
    }
}
