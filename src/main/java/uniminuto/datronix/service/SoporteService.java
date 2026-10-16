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
    private static final String CORREO_DESTINO_SOPORTE = "linaresjulian77@gmail.com";

    private final SoporteRepository soporteRepository;
    private final EmailService emailService;

    public SoporteService(SoporteRepository soporteRepository, EmailService emailService) {
        this.soporteRepository = soporteRepository;
        this.emailService = emailService;
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
        Soporte guardado = soporteRepository.save(soporte);
        emailService.enviarCorreo(
                CORREO_DESTINO_SOPORTE,
                "Nuevo reporte de soporte: " + guardado.getAsunto(),
                construirCorreoHtml(guardado));
        return SoporteMapper.toDto(guardado);
    }

    private String construirCorreoHtml(Soporte soporte) {
        return "<h2>Nuevo reporte de soporte de Datronix</h2>"
                + "<p><strong>Empresa:</strong> " + escaparHtml(soporte.getEmpresa()) + "</p>"
                + "<p><strong>Remitente:</strong> " + escaparHtml(soporte.getNombreRemitente()) + "</p>"
                + "<p><strong>Documento:</strong> " + escaparHtml(soporte.getIdRemitente()) + "</p>"
                + "<p><strong>Teléfono:</strong> " + escaparHtml(soporte.getTelefonoRemitente()) + "</p>"
                + "<p><strong>Asunto:</strong> " + escaparHtml(soporte.getAsunto()) + "</p>"
                + "<p><strong>Descripción:</strong></p><p>"
                + escaparHtml(soporte.getDescripcion()).replace("\n", "<br>")
                + "</p>";
    }

    private String escaparHtml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
