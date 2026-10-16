package uniminuto.datronix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uniminuto.datronix.dto.ReporteDashboardDTO;
import uniminuto.datronix.service.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ReporteDashboardDTO> obtenerDashboard() {
        return ResponseEntity.ok(reporteService.obtenerDashboard());
    }
}
