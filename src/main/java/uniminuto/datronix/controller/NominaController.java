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
import uniminuto.datronix.dto.NominaCreateDTO;
import uniminuto.datronix.dto.NominaResponseDTO;
import uniminuto.datronix.service.NominaService;

@RestController
@RequestMapping("/api/nomina")
public class NominaController {
    private final NominaService nominaService;

    public NominaController(NominaService nominaService) {
        this.nominaService = nominaService;
    }

    @GetMapping
    public ResponseEntity<List<NominaResponseDTO>> listarPagos() {
        return ResponseEntity.ok(nominaService.listarPagos());
    }

    @PostMapping
    public ResponseEntity<NominaResponseDTO> registrarPago(@Valid @RequestBody NominaCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nominaService.registrarPago(request));
    }
}
