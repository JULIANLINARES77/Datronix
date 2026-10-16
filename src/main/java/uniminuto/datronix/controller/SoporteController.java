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
import uniminuto.datronix.dto.SoporteCreateDTO;
import uniminuto.datronix.dto.SoporteResponseDTO;
import uniminuto.datronix.service.SoporteService;

@RestController
@RequestMapping("/api/soporte")
public class SoporteController {
    private final SoporteService soporteService;

    public SoporteController(SoporteService soporteService) {
        this.soporteService = soporteService;
    }

    @GetMapping
    public ResponseEntity<List<SoporteResponseDTO>> listarTickets() {
        return ResponseEntity.ok(soporteService.listarTickets());
    }

    @PostMapping
    public ResponseEntity<SoporteResponseDTO> crearTicket(@Valid @RequestBody SoporteCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(soporteService.crearTicket(request));
    }
}
