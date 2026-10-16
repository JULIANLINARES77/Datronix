package uniminuto.datronix.service;

import uniminuto.datronix.dto.ProveedorDTO;
import uniminuto.datronix.entity.Proveedor;
import uniminuto.datronix.exception.ProveedorDuplicadoException;
import uniminuto.datronix.exception.ProveedorEmailDuplicadoException;
import uniminuto.datronix.exception.ProveedorIdDuplicadoException;
import uniminuto.datronix.exception.ProveedorNotFoundException;
import uniminuto.datronix.exception.ProveedorTelefonoDuplicadoException;
import uniminuto.datronix.mapper.ProveedorMapper;
import uniminuto.datronix.repository.ProveedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
// Aplica el flujo de proveedores: convertir datos, consultar la base y devolver
// resultados.
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<ProveedorDTO> listarProveedores() {
        // Convierte cada proveedor guardado al formato que utiliza la interfaz.
        return proveedorRepository.findAll()
                .stream()
                .map(ProveedorMapper::toDto)
                .collect(Collectors.toList());
    }

    public ProveedorDTO buscarProveedorPorId(String id) {
        // Si no existe el ID, la excepción específica permite responder con un mensaje
        // adecuado.
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ProveedorNotFoundException(id));
        return ProveedorMapper.toDto(proveedor);
    }

    public ProveedorDTO guardarProveedor(ProveedorDTO dto) {
        normalizar(dto);
        if (proveedorRepository.existsById(dto.getIdProveedor())) {
            throw new ProveedorIdDuplicadoException(dto.getIdProveedor());
        }

        validarDuplicados(dto, null);
        Proveedor proveedor = ProveedorMapper.toEntity(dto);
        Proveedor guardado = proveedorRepository.save(proveedor);
        return ProveedorMapper.toDto(guardado);
    }

    public ProveedorDTO actualizarProveedor(String id, ProveedorDTO dto) {
        Proveedor proveedorExistente = proveedorRepository.findById(id)
                .orElseThrow(() -> new ProveedorNotFoundException(id));
        normalizar(dto);
        validarDuplicados(dto, id);

        proveedorExistente.setNombreEmpresa(dto.getNombreEmpresa());
        proveedorExistente.setContactoProveedor(dto.getContactoProveedor());
        proveedorExistente.setTelefonoProveedor(dto.getTelefonoProveedor());
        proveedorExistente.setEmailProveedor(dto.getEmailProveedor());
        proveedorExistente.setDireccionProveedor(dto.getDireccionProveedor());

        Proveedor actualizado = proveedorRepository.save(proveedorExistente);
        return ProveedorMapper.toDto(actualizado);
    }

    private void validarDuplicados(ProveedorDTO dto, String idProveedorExcluido) {
        boolean nombreDuplicado = idProveedorExcluido == null
                ? proveedorRepository.existsByNombreEmpresaIgnoreCase(dto.getNombreEmpresa())
                : proveedorRepository.existsByNombreEmpresaIgnoreCaseAndIdProveedorNot(
                        dto.getNombreEmpresa(), idProveedorExcluido);
        if (nombreDuplicado) {
            throw new ProveedorDuplicadoException(dto.getNombreEmpresa());
        }

        boolean telefonoDuplicado = idProveedorExcluido == null
                ? proveedorRepository.existsByTelefonoProveedor(dto.getTelefonoProveedor())
                : proveedorRepository.existsByTelefonoProveedorAndIdProveedorNot(
                        dto.getTelefonoProveedor(), idProveedorExcluido);
        if (telefonoDuplicado) {
            throw new ProveedorTelefonoDuplicadoException(dto.getTelefonoProveedor());
        }

        if (dto.getEmailProveedor() != null) {
            boolean emailDuplicado = idProveedorExcluido == null
                    ? proveedorRepository.existsByEmailProveedorIgnoreCase(dto.getEmailProveedor())
                    : proveedorRepository.existsByEmailProveedorIgnoreCaseAndIdProveedorNot(
                            dto.getEmailProveedor(), idProveedorExcluido);
            if (emailDuplicado) {
            throw new ProveedorEmailDuplicadoException(dto.getEmailProveedor());
            }
        }
    }

    private void normalizar(ProveedorDTO dto) {
        dto.setIdProveedor(dto.getIdProveedor().trim());
        dto.setNombreEmpresa(dto.getNombreEmpresa().trim());
        dto.setContactoProveedor(limpiarOpcional(dto.getContactoProveedor()));
        dto.setEmailProveedor(limpiarOpcional(dto.getEmailProveedor()));
        if (dto.getEmailProveedor() != null) {
            dto.setEmailProveedor(dto.getEmailProveedor().toLowerCase(Locale.ROOT));
        }
        dto.setDireccionProveedor(limpiarOpcional(dto.getDireccionProveedor()));
    }

    private String limpiarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    public void eliminarProveedor(String id) {
        // Evita confirmar un borrado cuando el proveedor nunca estuvo registrado.
        if (!proveedorRepository.existsById(id)) {
            throw new ProveedorNotFoundException(id);
        }
        proveedorRepository.deleteById(id);
    }
}