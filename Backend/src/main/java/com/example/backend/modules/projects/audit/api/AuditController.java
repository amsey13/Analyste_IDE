package com.example.backend.modules.projects.audit.api;

import com.example.backend.modules.projects.audit.dao.ReportRepository;
import com.example.backend.modules.projects.audit.entity.Report;
import com.example.backend.modules.projects.audit.service.AuditService;
import com.example.backend.modules.projects.audit.service.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("api/audit")
public class AuditController {


    @Autowired
    private AuditService auditService;
    @Autowired
    private ReportRepository  reportRepository;
    @Autowired
    private PdfReportService pdfReportService;


    @PostMapping("/{projectId}/analyze")
    public ResponseEntity<Report> analyze(
            @PathVariable UUID projectId,
            @RequestParam(value = "bpmn", required = false) MultipartFile bpmn,
            @RequestParam(value = "mcd", required = false) MultipartFile mcd,
            @RequestParam(value = "mfc", required = false) MultipartFile mfc
    ){
        try{
            Report report = auditService.startAudit(projectId,bpmn,mcd,mfc);
            return ResponseEntity.ok(report);
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();

        }
    }

    @GetMapping("/{reportId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Rapport introuvable"));

        try {
            byte[] pdfContents = pdfReportService.generateAuditPdf(report);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Rapport_Audit_" + reportId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfContents.length)
                    .body(pdfContents);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<Report> getReport(@PathVariable UUID reportId) {
        return reportRepository.findById(reportId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/project/{projectId}/latest")
    public ResponseEntity<Report> getLatestReport(@PathVariable UUID projectId) {
        return reportRepository.findFirstByProjectIdOrderByCreationDateDesc(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build()); 
    }

}
