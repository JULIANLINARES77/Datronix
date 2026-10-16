package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.NominaCreateDTO;
import uniminuto.datronix.entity.Empleado;
import uniminuto.datronix.entity.Nomina;
import uniminuto.datronix.repository.EmpleadoRepository;
import uniminuto.datronix.repository.NominaRepository;
import uniminuto.datronix.service.NominaService;

class NominaServiceTest {
    private final NominaRepository nominaRepository = Mockito.mock(NominaRepository.class);
    private final EmpleadoRepository empleadoRepository = Mockito.mock(EmpleadoRepository.class);
    private final NominaService service = new NominaService(nominaRepository, empleadoRepository);

    @Test
    void registrarPagoResolvesEmployeeAndReturnsPayrollDto() {
        Empleado empleado = Empleado.builder()
                .idEmpleado("EMP-1")
                .nombreEmpleado("Empleado Uno")
                .build();
        when(empleadoRepository.findById("EMP-1")).thenReturn(Optional.of(empleado));
        when(nominaRepository.save(any(Nomina.class))).thenAnswer(invocation -> {
            Nomina nomina = invocation.getArgument(0);
            nomina.setIdNomina(17L);
            return nomina;
        });
        NominaCreateDTO request = new NominaCreateDTO();
        request.setIdEmpleado("EMP-1");
        request.setFechaPago(LocalDate.of(2026, 9, 30));
        request.setSalarioPagado(new BigDecimal("2500000.00"));
        request.setPeriodo("2026-09");

        var response = service.registrarPago(request);

        assertEquals(17L, response.getIdNomina());
        assertEquals("Empleado Uno", response.getNombreEmpleado());
        assertEquals("2026-09", response.getPeriodo());
        assertEquals(new BigDecimal("2500000.00"), response.getSalarioPagado());
    }
}
