package uniminuto.datronix.controller;

import uniminuto.datronix.dto.ProveedorDTO;
import uniminuto.datronix.service.ProveedorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/proveedores")
// Recibe las solicitudes de proveedores y las traduce a llamadas del servicio.
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public ResponseEntity<List<ProveedorDTO>> listarProveedores() {
        // Devuelve los proveedores en el formato definido para el intercambio con la interfaz.
        return ResponseEntity.ok(proveedorService.listarProveedores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDTO> buscarPorId(@PathVariable String id) {
        // Consulta un proveedor concreto usando el ID que llega en la dirección web.
        return ResponseEntity.ok(proveedorService.buscarProveedorPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProveedorDTO> guardarProveedor(@Valid @RequestBody ProveedorDTO dto) {
        // Guarda los datos recibidos y comunica que se creó un registro nuevo.
        ProveedorDTO creado = proveedorService.guardarProveedor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTO> actualizarProveedor(
            @PathVariable String id,
            @Valid @RequestBody ProveedorDTO dto) {
        // Busca por ID y reemplaza la información editable del proveedor.
        ProveedorDTO actualizado = proveedorService.actualizarProveedor(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProveedor(@PathVariable String id) {
        // Elimina el registro solo después de que el servicio confirme que existe.
        proveedorService.eliminarProveedor(id);
        return ResponseEntity.noContent().build();
    }
}