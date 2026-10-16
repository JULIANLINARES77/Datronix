package uniminuto.datronix.mapper;

import uniminuto.datronix.dto.NominaResponseDTO;
import uniminuto.datronix.entity.Nomina;

public final class NominaMapper {
    private NominaMapper() {
    }

    public static NominaResponseDTO toDto(Nomina nomina) {
        return NominaResponseDTO.builder()
                .idNomina(nomina.getIdNomina())
                .idEmpleado(nomina.getEmpleado().getIdEmpleado())
                .nombreEmpleado(nomina.getEmpleado().getNombreEmpleado())
                .fechaPago(nomina.getFechaPago())
                .salarioPagado(nomina.getSalarioPagado())
                .periodo(nomina.getPeriodo())
                .build();
    }
}
