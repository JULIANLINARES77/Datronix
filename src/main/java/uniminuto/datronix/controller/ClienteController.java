package uniminuto.datronix.controller;

import uniminuto.datronix.dto.ClienteDTO;
import uniminuto.datronix.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
// Recibe las peticiones web de clientes y deja el trabajo de negocio en ClienteService.
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarClientes() {
        // Devuelve todos los clientes con el formato que entiende el frontend.
        List<ClienteDTO> clientes = clienteService.listarClientes();
        return ResponseEntity.ok(clientes); // 200 OK
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteDTO>> buscarClientes(@RequestParam String criterio) {
        return ResponseEntity.ok(clienteService.buscarClientes(criterio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable String id) {
        // Busca un cliente concreto; si no existe, el servicio informa el error correspondiente.
        ClienteDTO cliente = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok(cliente); // 200 OK
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> guardarCliente(@Valid @RequestBody ClienteDTO dto) {
        // Recibe los datos del formulario, crea el cliente y responde con estado 201.
        ClienteDTO creado = clienteService.guardarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado); // 201 CREATED
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizarCliente(
            @PathVariable String id,
            @Valid @RequestBody ClienteDTO dto) {
        // Usa el ID de la URL para encontrar el registro que debe reemplazarse.
        ClienteDTO actualizado = clienteService.actualizarCliente(id, dto);
        return ResponseEntity.ok(actualizado); // 200 OK
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable String id) {
        // El servicio valida que exista antes de borrarlo y aquí se confirma con 204.
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build(); // 204 NO CONTENT
    }
}