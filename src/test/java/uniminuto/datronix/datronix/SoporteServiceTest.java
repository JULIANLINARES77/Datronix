package uniminuto.datronix.datronix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uniminuto.datronix.dto.SoporteCreateDTO;
import uniminuto.datronix.entity.Soporte;
import uniminuto.datronix.repository.SoporteRepository;
import uniminuto.datronix.service.EmailService;
import uniminuto.datronix.service.SoporteService;

class SoporteServiceTest {
    private final SoporteRepository repository = Mockito.mock(SoporteRepository.class);
    private final EmailService emailService = Mockito.mock(EmailService.class);
    private final SoporteService service = new SoporteService(repository, emailService);

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
        verify(emailService).enviarCorreo(
                eq("linaresjulian77@gmail.com"),
                eq("Nuevo reporte de soporte: Asunto"),
                org.mockito.ArgumentMatchers.contains("Descripción del problema"));
    }

    @Test
    void correoEscapesUserSuppliedHtml() {
        when(repository.save(any(Soporte.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SoporteCreateDTO request = new SoporteCreateDTO();
        request.setEmpresa("Empresa <Uno>");
        request.setIdRemitente("DOC-1");
        request.setTelefonoRemitente("3001234567");
        request.setAsunto("Asunto");
        request.setDescripcion("<script>alert(1)</script>");
        request.setFechaCreacion(LocalDateTime.of(2026, 9, 5, 16, 0));

        service.crearTicket(request);

        verify(emailService).enviarCorreo(
                eq("linaresjulian77@gmail.com"),
                eq("Nuevo reporte de soporte: Asunto"),
                org.mockito.ArgumentMatchers.contains("&lt;script&gt;"));
    }
}
