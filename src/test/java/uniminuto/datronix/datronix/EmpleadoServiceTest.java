package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.EmpleadoDTO;
import uniminuto.datronix.entity.Empleado;
import uniminuto.datronix.exception.EmpleadoDuplicadoException;
import uniminuto.datronix.repository.EmpleadoRepository;
import uniminuto.datronix.service.EmpleadoService;

class EmpleadoServiceTest {
    private final EmpleadoRepository empleadoRepository = Mockito.mock(EmpleadoRepository.class);
    private final EmpleadoService service = new EmpleadoService(empleadoRepository);

    @Test
    void crearEmpleadoNormalizesOptionalFieldsAndReturnsDto() {
        EmpleadoDTO request = request();
        request.setIdEmpleado("  EMP-1 ");
        request.setNombreEmpleado(" Empleado Uno ");
        request.setCorreoEmpleado("  ");
        request.setTelefonoEmpleado("  ");
        when(empleadoRepository.existsById("EMP-1")).thenReturn(false);
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        EmpleadoDTO response = service.crearEmpleado(request);

        assertEquals("EMP-1", response.getIdEmpleado());
        assertEquals("Empleado Uno", response.getNombreEmpleado());
        assertEquals(null, response.getCorreoEmpleado());
        assertEquals(null, response.getTelefonoEmpleado());
        verify(empleadoRepository).save(any(Empleado.class));
    }

    @Test
    void crearEmpleadoRejectsDuplicateDocument() {
        when(empleadoRepository.existsById("EMP-1")).thenReturn(true);

        assertThrows(EmpleadoDuplicadoException.class, () -> service.crearEmpleado(request()));
        verify(empleadoRepository, never()).save(any(Empleado.class));
    }

    private EmpleadoDTO request() {
        EmpleadoDTO request = new EmpleadoDTO();
        request.setIdEmpleado("EMP-1");
        request.setNombreEmpleado("Empleado Uno");
        request.setCargo("Administración");
        request.setCorreoEmpleado("empleado@example.com");
        request.setTelefonoEmpleado("3001234567");
        request.setFechaIngreso(LocalDate.of(2025, 1, 1));
        request.setSalarioBase(new BigDecimal("2500000.00"));
        return request;
    }
}
