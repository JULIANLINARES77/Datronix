package uniminuto.datronix.mapper;

import uniminuto.datronix.dto.SoporteResponseDTO;
import uniminuto.datronix.entity.Soporte;

public final class SoporteMapper {
    private SoporteMapper() {
    }

    public static SoporteResponseDTO toDto(Soporte soporte) {
        return SoporteResponseDTO.builder()
                .idSoporte(soporte.getIdSoporte())
                .empresa(soporte.getEmpresa())
                .idRemitente(soporte.getIdRemitente())
                .nombreRemitente(soporte.getNombreRemitente())
                .telefonoRemitente(soporte.getTelefonoRemitente())
                .asunto(soporte.getAsunto())
                .descripcion(soporte.getDescripcion())
                .fechaCreacion(soporte.getFechaCreacion())
                .build();
    }
}
