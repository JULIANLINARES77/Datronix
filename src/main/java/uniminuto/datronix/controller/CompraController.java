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
import uniminuto.datronix.dto.CompraCreateDTO;
import uniminuto.datronix.dto.CompraResponseDTO;
import uniminuto.datronix.service.CompraService;

@RestController
@RequestMapping("/api/compras")
public class CompraController {
    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> listarCompras() {
        return ResponseEntity.ok(compraService.listarCompras());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> buscarCompra(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.buscarCompraPorId(id));
    }

    @PostMapping
    public ResponseEntity<CompraResponseDTO> registrarCompra(@Valid @RequestBody CompraCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.registrarCompra(request));
    }
}
