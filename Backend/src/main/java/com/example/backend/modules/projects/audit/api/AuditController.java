package com.example.backend.modules.projects.audit.api;

import com.example.backend.modules.projects.audit.entity.Report;
import com.example.backend.modules.projects.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("api/audit")
public class AuditController {


    @Autowired
    private AuditService auditService;


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



}
