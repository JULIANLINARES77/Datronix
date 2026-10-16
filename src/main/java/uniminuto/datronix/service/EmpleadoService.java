package uniminuto.datronix.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.EmpleadoDTO;
import uniminuto.datronix.entity.Empleado;
import uniminuto.datronix.exception.EmpleadoDuplicadoException;
import uniminuto.datronix.exception.EmpleadoNotFoundException;
import uniminuto.datronix.mapper.EmpleadoMapper;
import uniminuto.datronix.repository.EmpleadoRepository;

@Service
public class EmpleadoService {
    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpleadoDTO> listarEmpleados() {
        return empleadoRepository.findAll().stream().map(EmpleadoMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public EmpleadoDTO buscarEmpleado(String id) {
        return empleadoRepository.findById(id)
                .map(EmpleadoMapper::toDto)
                .orElseThrow(() -> new EmpleadoNotFoundException(id));
    }

    @Transactional
    public EmpleadoDTO crearEmpleado(EmpleadoDTO request) {
        String id = request.getIdEmpleado().trim();
        if (empleadoRepository.existsById(id)) {
            throw new EmpleadoDuplicadoException(id);
        }
        return EmpleadoMapper.toDto(empleadoRepository.save(EmpleadoMapper.toEntity(request)));
    }

    @Transactional
    public EmpleadoDTO actualizarEmpleado(String id, EmpleadoDTO request) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new EmpleadoNotFoundException(id));
        EmpleadoMapper.updateEntity(empleado, request);
        return EmpleadoMapper.toDto(empleadoRepository.save(empleado));
    }

    @Transactional
    public void eliminarEmpleado(String id) {
        if (!empleadoRepository.existsById(id)) {
            throw new EmpleadoNotFoundException(id);
        }
        empleadoRepository.deleteById(id);
    }
}
