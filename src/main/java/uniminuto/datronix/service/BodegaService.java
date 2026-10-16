package uniminuto.datronix.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniminuto.datronix.dto.BodegaEstadoUpdateDTO;
import uniminuto.datronix.dto.BodegaRemisionDTO;
import uniminuto.datronix.entity.EstadoRemisionBodega;
import uniminuto.datronix.entity.Remision;
import uniminuto.datronix.exception.EstadoRemisionBodegaInvalidoException;
import uniminuto.datronix.exception.RemisionBodegaNotFoundException;
import uniminuto.datronix.mapper.BodegaMapper;
import uniminuto.datronix.repository.RemisionRepository;

@Service
public class BodegaService {
    private final RemisionRepository remisionRepository;

    public BodegaService(RemisionRepository remisionRepository) {
        this.remisionRepository = remisionRepository;
    }

    @Transactional(readOnly = true)
    public List<BodegaRemisionDTO> listarRemisionesPendientes() {
        return remisionRepository.findRemisionesPendientesBodega(EstadoRemisionBodega.ENTREGADA)
                .stream()
                .map(BodegaMapper::toDto)
                .toList();
    }

    @Transactional
    public BodegaRemisionDTO actualizarEstado(Long idRemision, BodegaEstadoUpdateDTO request) {
        Remision remision = remisionRepository.findByIdForUpdate(idRemision)
                .orElseThrow(() -> new RemisionBodegaNotFoundException(idRemision));
        EstadoRemisionBodega actual = EstadoRemisionBodega.actual(remision.getEstadoBodega());
        EstadoRemisionBodega solicitado = parsearEstado(request.getEstadoBodega());
        if (!actual.permite(solicitado)) {
            throw new EstadoRemisionBodegaInvalidoException(actual.name(), solicitado.name());
        }

        remision.setEstadoBodega(solicitado);
        remision.setObservacionesBodega(normalizarObservaciones(request.getObservacionesBodega()));
        return BodegaMapper.toDto(remisionRepository.save(remision));
    }

    private EstadoRemisionBodega parsearEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new EstadoRemisionBodegaInvalidoException("desconocido", estado);
        }
        try {
            return EstadoRemisionBodega.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new EstadoRemisionBodegaInvalidoException("desconocido", estado);
        }
    }

    private String normalizarObservaciones(String observaciones) {
        if (observaciones == null || observaciones.isBlank()) {
            return null;
        }
        return observaciones.trim();
    }
}
