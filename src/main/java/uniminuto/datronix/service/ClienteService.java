package uniminuto.datronix.service;

import uniminuto.datronix.dto.ClienteDTO;
import uniminuto.datronix.entity.Cliente;
import uniminuto.datronix.exception.ClienteDuplicadoException;
import uniminuto.datronix.exception.ClienteNotFoundException;
import uniminuto.datronix.mapper.ClienteMapper;
import uniminuto.datronix.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
// Coordina las reglas de clientes y conecta los DTO del API con la base de
// datos.
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // LISTAR → devuelve List<ClienteDTO>
    public List<ClienteDTO> listarClientes() {
        // La entidad vive en la base de datos, pero hacia afuera se entrega un DTO.
        return clienteRepository.findAll()
                .stream()
                .map(ClienteMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ClienteDTO> buscarClientes(String criterio) {
        String termino = criterio == null ? "" : criterio.trim();
        if (termino.isEmpty()) {
            return listarClientes();
        }
        return clienteRepository
                .findByIdClienteContainingIgnoreCaseOrNombreClienteContainingIgnoreCase(termino, termino)
                .stream()
                .map(ClienteMapper::toDto)
                .collect(Collectors.toList());
    }

    // BUSCAR POR ID → devuelve ClienteDTO
    public ClienteDTO buscarClientePorId(String id) {
        // Un ID inexistente se transforma en una excepción que luego maneja la
        // aplicación.
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
        return ClienteMapper.toDto(cliente);
    }

    // GUARDAR → recibe DTO, devuelve DTO
    public ClienteDTO guardarCliente(ClienteDTO dto) {

        if (clienteRepository.existsById(dto.getIdCliente())) {

            throw new  ClienteDuplicadoException(dto.getIdCliente());

        }

        // Primero se convierte lo recibido, después se guarda y finalmente se devuelve
        // el resultado.
        Cliente cliente = ClienteMapper.toEntity(dto);
        Cliente guardado = clienteRepository.save(cliente);
        return ClienteMapper.toDto(guardado);
    }

    // ACTUALIZAR → recibe DTO, devuelve DTO
    public ClienteDTO actualizarCliente(String id, ClienteDTO dto) {
        // Se recupera el cliente original para conservar su registro y actualizar sus
        // datos.
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));

        // Actualizar campos (solo los que vienen en el DTO)
        clienteExistente.setNombreCliente(dto.getNombreCliente());
        clienteExistente.setTelefonoCliente(dto.getTelefonoCliente());
        clienteExistente.setEmailCliente(dto.getEmailCliente());
        clienteExistente.setDireccionCliente(dto.getDireccionCliente());
        clienteExistente.setTipoCliente(dto.getTipoCliente());

        Cliente actualizado = clienteRepository.save(clienteExistente);
        return ClienteMapper.toDto(actualizado);
    }

    // ELIMINAR → lanza excepción si no existe
    public void eliminarCliente(String id) {
        // La comprobación evita intentar borrar silenciosamente un cliente que no está
        // registrado.
        if (!clienteRepository.existsById(id)) {
            throw new ClienteNotFoundException(id);
        }
        clienteRepository.deleteById(id);
    }
}