package uniminuto.datronix.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import uniminuto.datronix.dto.VentaCreateDTO;
import uniminuto.datronix.dto.VentaRecienteResponseDTO;
import uniminuto.datronix.dto.VentaResponseDTO;
import uniminuto.datronix.service.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarVentas() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    @GetMapping("/recientes")
    public ResponseEntity<List<VentaRecienteResponseDTO>> listarVentasRecientes() {
        return ResponseEntity.ok(ventaService.listarVentasRecientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> buscarVentaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.buscarVentaPorId(id));
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrarVenta(@Valid @RequestBody VentaCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.registrarVenta(request));
    }
}
