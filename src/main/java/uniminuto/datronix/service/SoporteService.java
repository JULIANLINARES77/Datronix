package uniminuto.datronix.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.SoporteCreateDTO;
import uniminuto.datronix.dto.SoporteResponseDTO;
import uniminuto.datronix.entity.Soporte;
import uniminuto.datronix.mapper.SoporteMapper;
import uniminuto.datronix.repository.SoporteRepository;

@Service
public class SoporteService {
    private final SoporteRepository soporteRepository;

    public SoporteService(SoporteRepository soporteRepository) {
        this.soporteRepository = soporteRepository;
    }

    @Transactional(readOnly = true)
    public List<SoporteResponseDTO> listarTickets() {
        return soporteRepository.findAllByOrderByFechaCreacionDescIdSoporteDesc()
                .stream()
                .map(SoporteMapper::toDto)
                .toList();
    }

    @Transactional
    public SoporteResponseDTO crearTicket(SoporteCreateDTO request) {
        Soporte soporte = Soporte.builder()
                .empresa(request.getEmpresa().trim())
                .idRemitente(request.getIdRemitente().trim())
                .nombreRemitente(request.getNombreRemitente() == null
                        ? null : request.getNombreRemitente().trim())
                .telefonoRemitente(request.getTelefonoRemitente().trim())
                .asunto(request.getAsunto().trim())
                .descripcion(request.getDescripcion().trim())
                .fechaCreacion(request.getFechaCreacion())
                .build();
        return SoporteMapper.toDto(soporteRepository.save(soporte));
    }
}
