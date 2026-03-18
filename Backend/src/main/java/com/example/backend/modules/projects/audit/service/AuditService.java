package com.example.backend.modules.projects.audit.service;

import com.example.backend.modules.analysis.exporter.MistralService;
import com.example.backend.modules.analysis.parser.BpmnParserStrategy;
import com.example.backend.modules.analysis.parser.McdParserStrategy;
import com.example.backend.modules.analysis.parser.MfcParserStrategy;
import com.example.backend.modules.projects.audit.dao.AnomalyTypeRepository;
import com.example.backend.modules.projects.audit.dao.AuditProjectRepository;
import com.example.backend.modules.projects.audit.dao.ReportRepository;
import com.example.backend.modules.projects.audit.dto.AnomalyDTO;
import com.example.backend.modules.projects.audit.entity.*;
import com.example.backend.modules.projects.audit.taiga.service.TaigaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final TaigaService taigaService;
    private final AnomalyTypeRepository anomalyTypeRepository;
    private ReportRepository reportRepository;
    private AuditProjectRepository auditProjectRepository;
    private final MistralService mistralService;

    public AuditService(TaigaService taigaService, AnomalyTypeRepository anomalyTypeRepository,
                        ReportRepository reportRepository, AuditProjectRepository auditProjectRepository,
                        MistralService mistralService) {
        this.taigaService = taigaService;
        this.anomalyTypeRepository = anomalyTypeRepository;
        this.reportRepository = reportRepository;
        this.auditProjectRepository = auditProjectRepository;
        this.mistralService = mistralService;
    }


   /**
    * The function `mapDtoToEntity` converts data from a DTO object to an entity object in Java,
    * handling mapping and creation of related entities.
    * 
    * @param anomalyDTO AnomalyDTO is a data transfer object (DTO) that contains information about an
    * anomaly. It likely includes fields such as description, severity, and type of the anomaly. The
    * method `mapDtoToEntity` takes an AnomalyDTO object as input and maps its data to an Anomaly
    * entity object
    * @return The method `mapDtoToEntity` is returning an `Anomaly` entity.
    */
    private Anomaly mapDtoToEntity(AnomalyDTO anomalyDTO) {
        Anomaly anomaly = new Anomaly();
        anomaly.setDescription(anomalyDTO.description());

        try {
            anomaly.setSeverity(SeverityLevel.valueOf(anomalyDTO.severity().toUpperCase().trim()));
        }catch(Exception e){
            anomaly.setSeverity(SeverityLevel.MEDIUM);
        }

        AnomalyType type = anomalyTypeRepository.findByWording(anomalyDTO.type())
                .orElseGet(() -> {
                    AnomalyType newType = new AnomalyType();
                    newType.setWording(anomalyDTO.type().toUpperCase());
                    return anomalyTypeRepository.save(newType);
                });

        anomaly.setAnomalyType(type);

        if(anomalyDTO.suggestion() != null && ! anomalyDTO.suggestion().isEmpty()){
            Suggestion suggestion = new Suggestion();
            suggestion.setContent(anomalyDTO.suggestion());
            suggestion.setAnomaly(anomaly);
            anomaly.setSuggestion(suggestion);
        }


        return anomaly;
    }


    /**
     * This Java function fetches user stories from a Taiga project and returns a formatted string with
     * their details.
     * 
     * @param project The `fetchTaigaContent` method takes an `AuditProject` object as a parameter.
     * This object likely contains information about a project, such as its slug and Taiga token. The
     * method uses this information to fetch user stories from Taiga using a `taigaService` and then
     * constructs a
     * @return The `fetchTaigaContent` method returns a string containing details of user stories
     * fetched from Taiga. If the method executes successfully, it will return a string with the
     * details of each user story including the subject and description. If an exception occurs during
     * the execution, it will return a string indicating an error with the Taiga service along with the
     * error message.
     */
    private String fetchTaigaContent(AuditProject project){

        try {
            
            Integer taigaId = taigaService.getProjectIdBySlug(project.getProjectSlug(), project.getTaigaToken());
            var stories = taigaService.getUserStories(taigaId, project.getTaigaToken());

            StringBuilder sb = new StringBuilder("--- DETAILS DES USER STORIES (Taiga) ---\n");
            for (var us : stories) {
                sb.append("- US: ").append(us.getSubject())
                        .append(" | Description: ").append(us.getSubject()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Erreur Taiga : " + e.getMessage();
        }
    }

   /**
    * This Java function starts an audit process for a project by parsing uploaded files and fetching
    * content from Taiga, then executes and saves the audit.
    * 
    * @param projectId The `projectId` parameter is of type `UUID` and represents the unique identifier
    * of the project for which the audit is being started.
    * @param bpmn The `startAudit` method you provided seems to be a part of an auditing process for a
    * project. It takes in a project ID, along with three files (bpmn, mcd, mfc) in the form of
    * `MultipartFile`. The method then retrieves the corresponding project from the
    * @param mcd The `mcd` parameter in the `startAudit` method is of type `MultipartFile`, which is a
    * representation of an uploaded file in a Spring application. In this context, `mcd` is expected to
    * be a file containing data related to the audit process. The method checks if the
    * @param mfc The `mfc` parameter in the `startAudit` method is of type `MultipartFile`, which is a
    * representation of an uploaded file in a Spring application. In this context, `mfc` is used to
    * pass a file containing content related to the audit process. The method checks if the
    * @return The method `startAudit` is returning a `Report` object.
    */
    @Transactional
    public Report startAudit(UUID projectId, MultipartFile bpmn, MultipartFile mcd, MultipartFile mfc)
            throws IOException, ParserConfigurationException, SAXException{

        AuditProject audit = auditProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found!"));

        String bpmnContent = (bpmn != null && !bpmn.isEmpty()) ? new BpmnParserStrategy(bpmn.getInputStream()).parse() : null;
        String mcdContent = (mcd != null && !mcd.isEmpty()) ? new McdParserStrategy(mcd.getInputStream()).parse() : null;
        String mfcContent = (mfc != null && !mfc.isEmpty()) ? new MfcParserStrategy(mfc.getInputStream()).parse() : null;

        String usContent = null;
        if (audit.getTaigaToken() != null && audit.getProjectSlug() != null) {
            usContent = fetchTaigaContent(audit);
        }


        return executeAndSaveAudit(audit, bpmnContent, mcdContent, mfcContent, usContent);

    }

    /**
     * The function `executeAndSaveAudit` executes an audit analysis using input parameters, creates a
     * report with anomalies, calculates a final score, and saves the report in a repository.
     * 
     * @param project The `project` parameter in the `executeAndSaveAudit` method represents the
     * AuditProject object which contains information about the audit project being executed. It likely
     * includes details such as project name, description, audit type, and other relevant
     * project-specific data. This parameter is used to associate the generated report with
     * @param bpmn BPMN (Business Process Model and Notation) is a standard for modeling business
     * processes. It is a graphical representation of a business process using specific symbols and
     * diagrams. In the context of your method `executeAndSaveAudit`, the `bpmn` parameter likely
     * represents a BPMN file or
     * @param mcd The parameter `mcd` in the `executeAndSaveAudit` method likely stands for "Model
     * Comparison Document". This document is probably used in the audit analysis process to compare
     * different models or versions of a system or process. It may contain information about the
     * differences, similarities, advantages, and disadvantages of
     * @param mfc The parameter `mfc` in the `executeAndSaveAudit` method likely stands for "Model Flow
     * Chart". It is used as input for the `mistralService.executeAuditAnalysis` method along with
     * other parameters like `bpmn`, `mcd`, and `us` to perform an
     * @param us The `us` parameter in the `executeAndSaveAudit` method likely stands for User Stories.
     * It is used as input for the `mistralService.executeAuditAnalysis` method to perform an audit
     * analysis on the provided User Stories along with the BPMN, MCD, and MFC inputs.
     * @return A Report object is being returned from the executeAndSaveAudit method.
     */
    private Report executeAndSaveAudit(AuditProject project, String bpmn, String mcd, String mfc, String us) throws IOException {

        List<AnomalyDTO> dtos = mistralService.executeAuditAnalysis(bpmn, mcd, mfc, us);
        Report report = new Report();
        report.setProject(project);
        report.setCreationDate(LocalDateTime.now());

        for (AnomalyDTO dto : dtos) {
            Anomaly anomaly = mapDtoToEntity(dto);
            anomaly.setRapport(report);
            report.getAnomalies().add(anomaly);
        }


        double scoreFinal = mistralService.calculScore(report);
        report.setScore(scoreFinal);

        return reportRepository.save(report);

    }





}
