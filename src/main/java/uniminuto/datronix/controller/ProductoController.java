package uniminuto.datronix.controller;

import uniminuto.datronix.dto.ProductoDTO;
import uniminuto.datronix.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
// Expone las operaciones de productos y delega las decisiones al servicio.
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos() {
        // Entrega la lista en formato DTO para no exponer directamente la entidad JPA.
        return ResponseEntity.ok(productoService.listarProductos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable Long id) {
        // Consulta un producto por su identificador y deja al servicio manejar los casos faltantes.
        return ResponseEntity.ok(productoService.buscarProductoPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> guardarProducto(@Valid @RequestBody ProductoDTO dto) {
        // Crea el producto y responde con 201 para indicar que nació un nuevo registro.
        ProductoDTO creado = productoService.guardarProducto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoDTO dto) {
        // Actualiza el producto identificado en la URL con los datos recibidos.
        ProductoDTO actualizado = productoService.actualizarProducto(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        // El servicio comprueba el ID y, si todo está bien, se responde sin contenido.
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}