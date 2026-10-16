package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.SoporteCreateDTO;
import uniminuto.datronix.entity.Soporte;
import uniminuto.datronix.repository.SoporteRepository;
import uniminuto.datronix.service.SoporteService;

class SoporteServiceTest {
    private final SoporteRepository repository = Mockito.mock(SoporteRepository.class);
    private final SoporteService service = new SoporteService(repository);

    @Test
    void crearTicketTrimsInputAndReturnsSavedTicket() {
        when(repository.save(any(Soporte.class))).thenAnswer(invocation -> {
            Soporte soporte = invocation.getArgument(0);
            soporte.setIdSoporte(9L);
            return soporte;
        });
        SoporteCreateDTO request = new SoporteCreateDTO();
        request.setEmpresa(" Empresa Uno ");
        request.setIdRemitente(" DOC-1 ");
        request.setNombreRemitente(" Remitente ");
        request.setTelefonoRemitente(" 3001234567 ");
        request.setAsunto(" Asunto ");
        request.setDescripcion(" Descripción del problema ");
        request.setFechaCreacion(LocalDateTime.of(2026, 9, 5, 16, 0));

        var response = service.crearTicket(request);

        assertEquals(9L, response.getIdSoporte());
        assertEquals("Empresa Uno", response.getEmpresa());
        assertEquals("DOC-1", response.getIdRemitente());
        assertEquals("Descripción del problema", response.getDescripcion());
    }
}
