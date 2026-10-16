package uniminuto.datronix.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import uniminuto.datronix.dto.BodegaEstadoUpdateDTO;
import uniminuto.datronix.dto.BodegaRemisionDTO;
import uniminuto.datronix.service.BodegaService;

@RestController
@RequestMapping("/api/bodega/remisiones")
public class BodegaController {
    private final BodegaService bodegaService;

    public BodegaController(BodegaService bodegaService) {
        this.bodegaService = bodegaService;
    }

    @GetMapping
    public ResponseEntity<List<BodegaRemisionDTO>> listarRemisionesPendientes() {
        return ResponseEntity.ok(bodegaService.listarRemisionesPendientes());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<BodegaRemisionDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody BodegaEstadoUpdateDTO request) {
        return ResponseEntity.ok(bodegaService.actualizarEstado(id, request));
    }
}
