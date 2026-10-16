package uniminuto.datronix.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.NominaCreateDTO;
import uniminuto.datronix.dto.NominaResponseDTO;
import uniminuto.datronix.entity.Empleado;
import uniminuto.datronix.entity.Nomina;
import uniminuto.datronix.exception.EmpleadoNotFoundException;
import uniminuto.datronix.mapper.NominaMapper;
import uniminuto.datronix.repository.EmpleadoRepository;
import uniminuto.datronix.repository.NominaRepository;

@Service
public class NominaService {
    private final NominaRepository nominaRepository;
    private final EmpleadoRepository empleadoRepository;

    public NominaService(NominaRepository nominaRepository, EmpleadoRepository empleadoRepository) {
        this.nominaRepository = nominaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional(readOnly = true)
    public List<NominaResponseDTO> listarPagos() {
        return nominaRepository.findAllWithEmpleado().stream().map(NominaMapper::toDto).toList();
    }

    @Transactional
    public NominaResponseDTO registrarPago(NominaCreateDTO request) {
        String idEmpleado = request.getIdEmpleado().trim();
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new EmpleadoNotFoundException(idEmpleado));
        Nomina nomina = Nomina.builder()
                .empleado(empleado)
                .fechaPago(request.getFechaPago())
                .salarioPagado(request.getSalarioPagado())
                .periodo(request.getPeriodo())
                .build();
        return NominaMapper.toDto(nominaRepository.save(nomina));
    }
}
