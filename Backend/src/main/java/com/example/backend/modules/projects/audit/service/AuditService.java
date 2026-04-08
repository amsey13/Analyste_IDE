package com.example.backend.modules.projects.audit.service;

import com.example.backend.modules.analysis.exporter.MistralService;
import com.example.backend.modules.analysis.parser.BpmnParserStrategy;
import com.example.backend.modules.analysis.parser.McdParserStrategy;
import com.example.backend.modules.analysis.parser.MfcParserStrategy;
import com.example.backend.modules.analytics.dao.LogExecutionRepository;
import com.example.backend.modules.analytics.entity.LogExecution;
import com.example.backend.modules.projects.audit.dao.AnomalyTypeRepository;
import com.example.backend.modules.projects.audit.dao.AuditProjectRepository;
import com.example.backend.modules.projects.audit.dao.ReportRepository;
import com.example.backend.modules.projects.audit.dto.AnomalyDTO;
import com.example.backend.modules.projects.audit.dto.AuditVersionDTO;
import com.example.backend.modules.projects.audit.entity.*;
import com.example.backend.modules.projects.audit.taiga.service.TaigaService;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private static final String OP_AUDIT = "AUDIT_IA";
    private static final String OP_DESERIAL = "DESERIALIZATION";

    private final TaigaService taigaService;
    private final AnomalyTypeRepository anomalyTypeRepository;
    private ReportRepository reportRepository;
    private AuditProjectRepository auditProjectRepository;
    private final MistralService mistralService;
    private final LogExecutionRepository logExecutionRepository;

    public AuditService(TaigaService taigaService, AnomalyTypeRepository anomalyTypeRepository,
                        ReportRepository reportRepository, AuditProjectRepository auditProjectRepository,
                        MistralService mistralService, LogExecutionRepository log) {
        this.taigaService = taigaService;
        this.anomalyTypeRepository = anomalyTypeRepository;
        this.reportRepository = reportRepository;
        this.auditProjectRepository = auditProjectRepository;
        this.mistralService = mistralService;
        this.logExecutionRepository = log;
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

        Long start = System.currentTimeMillis();
        try {
            
            Integer taigaId = taigaService.getProjectIdBySlug(project.getProjectSlug(), project.getTaigaToken());
            var stories = taigaService.getUserStories(taigaId, project.getTaigaToken());

            StringBuilder sb = new StringBuilder("--- DETAILS DES USER STORIES (Taiga) ---\n");
            for (var us : stories) {
                sb.append("- US: ").append(us.getSubject())
                        .append(" | Description: ").append(us.getSubject()).append("\n");
            }
            saveLog("TAIGA_EXPORT", start, "SUCCESS", project.getIdProject(), "{\"stories\": " + stories.size() + "}");
            return sb.toString();
        } catch (Exception e) {
            saveLog("TAIGA_EXPORT", start, "FAILURE", project.getIdProject(), "{\"error\": \"" + e.getMessage() + "\"}");
            throw new RuntimeException("Arreur d'importation de US TAIGA : "+e.getMessage());
        }
    }

   
    /**
     * The `startAudit` function processes uploaded files and executes an audit for a project, saving
     * logs for success or failure.
     * 
     * @param projectId The `startAudit` method you provided is annotated with `@Transactional` which
     * means that the method will be executed within a transactional context. This ensures that either
     * all operations within the method complete successfully or none of them do, helping to maintain
     * data integrity.
     * @param bpmn The `startAudit` method you provided is a transactional method that starts an audit
     * process for a project. It takes in a project ID, as well as three files (`bpmn`, `mcd`, `mfc`)
     * that contain specific content related to the audit.
     * @param mcd The `startAudit` method you provided is a transactional method that starts an audit
     * process for a project. It takes in a project ID, as well as three files: bpmn, mcd, and mfc. The
     * method first retrieves the audit project based on the provided project ID. It
     * @param mfc The `startAudit` method you provided is a transactional method that starts an audit
     * process for a project. It takes in a project ID, as well as three files: bpmn, mcd, and mfc. The
     * method then retrieves the project details, parses the content of the bpmn
     * @return The method `startAudit` is returning a `Report` object.
     */
    @Transactional
    public Report startAudit(UUID projectId, MultipartFile bpmn, MultipartFile mcd, MultipartFile mfc)
            throws IOException, ParserConfigurationException, SAXException{

        AuditProject audit = auditProjectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found!"));

        long globalStart = System.currentTimeMillis();

        String bpmnContent = (bpmn != null && !bpmn.isEmpty()) ? new BpmnParserStrategy(bpmn.getInputStream()).parse() : null;
        String mcdContent = (mcd != null && !mcd.isEmpty()) ? new McdParserStrategy(mcd.getInputStream()).parse() : null;
        String mfcContent = (mfc != null && !mfc.isEmpty()) ? new MfcParserStrategy(mfc.getInputStream()).parse() : null;

        String usContent = null;
        if (audit.getTaigaToken() != null && audit.getProjectSlug() != null) {
            usContent = fetchTaigaContent(audit);
        }
        try {
            Report report = executeAndSaveAudit(audit, bpmnContent, mcdContent, mfcContent, usContent);

            long totalDuration = System.currentTimeMillis() - globalStart;
            saveLog("GLOBAL_CHAIN_PROCESS", globalStart, "SUCCESS", projectId,
                    "{\"files_processed\": 3, \"total_ms\": " + totalDuration + "}");

            return report;

        } catch (Exception e) {
            saveLog("GLOBAL_CHAIN_PROCESS", globalStart, "FAILURE", projectId,
                    "{\"error\": \"Erreur fatale dans la chaine de traitement\"}");
            throw e;
        }
    }

    /**
     * The `saveLog` function creates a `LogExecution` object with specified details and saves it to a
     * repository.
     * 
     * @param op The `op` parameter represents the operation or action that was performed, such as
     * "create", "update", or "delete".
     * @param startMillis The `startMillis` parameter in the `saveLog` method represents the starting
     * time in milliseconds when the operation began.
     * @param status The `status` parameter in the `saveLog` method represents the status of the
     * operation being logged. It could indicate whether the operation was successful, failed, in
     * progress, or any other relevant status information.
     * @param projectId A unique identifier for the project related to the log entry.
     * @param details The `details` parameter in the `saveLog` method is a string that contains
     * additional information or details related to the log entry being saved. This could include any
     * relevant information that you want to store along with the log entry, such as error messages,
     * specific actions taken, or any other relevant context
     */
    private void saveLog(String op, long startMillis, String status, UUID projectId, String details) {
        LogExecution log = new LogExecution();
        log.setOperation(op);
        log.setStartTime(LocalDateTime.now());
        log.setEndTime(LocalDateTime.now());
        log.setDurationMs(System.currentTimeMillis() - startMillis);
        log.setStatus(status);
        log.setProjectId(projectId);
        log.setDetails(details);
        logExecutionRepository.save(log);
    }

    
    /**
     * This Java function executes an audit analysis using input parameters, saves logs for the
     * analysis process, creates a report with anomalies, calculates a final score, and saves the
     * report in a repository.
     * 
     * @param project The `project` parameter in the `executeAndSaveAudit` method represents an
     * `AuditProject` object, which likely contains information related to an audit project such as
     * project details, client information, audit scope, etc.
     * @param bpmn The `bpmn` parameter in the `executeAndSaveAudit` method likely stands for Business
     * Process Model and Notation. It is a standard for modeling business processes graphically. In
     * this context, the `bpmn` parameter is likely a string representation of a BPMN model that is
     * @param mcd The `mcd` parameter in the `executeAndSaveAudit` method likely stands for a specific
     * type of input related to the audit process. Without more context or information about the
     * application or domain, it's difficult to provide a more specific definition.
     * @param mfc The `mfc` parameter in the `executeAndSaveAudit` method likely stands for "Model Flow
     * Chart". It is one of the inputs required for executing an audit analysis in the context of the
     * `mistralService.executeAuditAnalysis` method. This parameter is used along with other inputs
     * like `
     * @param us The `us` parameter in the `executeAndSaveAudit` method likely stands for User Stories.
     * It seems to be a part of the input required for executing the audit analysis using the
     * `mistralService`. The method takes various inputs related to an audit project (`project`), BPMN,
     * M
     * @return The method `executeAndSaveAudit` is returning a `Report` object that represents the
     * audit report generated for the given `AuditProject`.
     */
    private Report executeAndSaveAudit(AuditProject project, String bpmn, String mcd, String mfc, String us) throws IOException {

        long start = System.currentTimeMillis();

        List<AnomalyDTO> dtos;
        try {

            dtos = mistralService.executeAuditAnalysis(bpmn, mcd, mfc, us);
            long iaDuration = System.currentTimeMillis() - start;
            saveLog("IA_RESPONSE_TIME", iaDuration, "SUCCESS", project.getIdProject(),
                    "{\"duration_ms\": " + iaDuration + "}");


            saveLog(OP_DESERIAL, start, "SUCCESS", project.getIdProject(), "{\"count\": " + dtos.size() + "}");

        } catch (Exception e) {

            saveLog(OP_DESERIAL, start, "FAILURE", project.getIdProject(), "{\"error\": \"" + e.getMessage() + "\"}");
            throw e;
        }


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
        Report savedReport = reportRepository.save(report);

        long duration = System.currentTimeMillis() - start;
        String details = String.format("{\"score\": %.2f, \"anomalies\": %d}", scoreFinal, dtos.size());
        saveLog(OP_AUDIT, duration, "SUCCESS", project.getIdProject(), details);

        return savedReport;

    }

   /**
    * The function `mapEntityToDto` converts an `Anomaly` entity object to an `AnomalyDTO` data
    * transfer object by mapping specific attributes.
    * 
    * @param anomaly The `mapEntityToDto` method takes an `Anomaly` object as a parameter and maps its
    * properties to an `AnomalyDTO` object. The properties of the `Anomaly` object include
    * `description`, `severity`, `anomalyType`, and `suggestion`. These properties are
    * @return An AnomalyDTO object is being returned, which is created using the properties of the
    * provided Anomaly object.
    */
    private AnomalyDTO mapEntityToDto(Anomaly anomaly) {
        return new AnomalyDTO(
                anomaly.getDescription(),
                anomaly.getSeverity().name(), 
                anomaly.getAnomalyType().getWording(),
                anomaly.getSuggestion().getContent()
        );
    }

    /**
     * This function extracts descriptions from a list of anomalies in a report and returns them as a
     * set.
     * 
     * @param report The `extractDescriptions` method takes a `Report` object as a parameter. This
     * method extracts descriptions from the anomalies present in the report and returns a set of
     * unique descriptions.
     * @return A set of descriptions extracted from the anomalies in the given report.
     */
    private Set<String> extractDescriptions(Report report) {
        return report.getAnomalies().stream()
            .map(Anomaly::getDescription)
            .collect(Collectors.toSet());
    }


    /**
     * The function `filterAndMap` takes a `Report` object and a `Predicate` condition, filters the
     * anomalies based on the condition, maps the filtered anomalies to `AnomalyDTO` objects, and
     * returns a list of `AnomalyDTO` objects.
     * 
     * @param report The `report` parameter is an object of type `Report`, which contains a list of
     * anomalies.
     * @param condition The `condition` parameter is a `Predicate` functional interface that defines a
     * condition which will be used to filter the anomalies in the `report`. It will be applied to each
     * anomaly in the list to determine whether it should be included in the final result or not.
     * @return A list of `AnomalyDTO` objects that have been filtered and mapped from the anomalies in
     * the `Report` object based on the provided condition.
     */
    private List<AnomalyDTO> filterAndMap(Report report, Predicate<Anomaly> condition) {
        return report.getAnomalies().stream()
            .filter(condition)
            .map(this::mapEntityToDto)
            .toList();
    }




    public AuditVersionDTO compareLastTwoReports(UUID projectId) throws IOException {
        List<Report> reports = reportRepository.findTop2ByProjectIdOrderByCreationDateDesc(projectId);

        for (Report r : reports) {
            System.out.println("  -> Rapport ID : " + r.getId() + " du " + r.getCreationDate() + r.getScore());
        }

        if (reports.size() <2 ) {
            System.out.println("Pas de nouvelles version pour ce projet\n");
            return null;
        }
        Report recent = reports.get(0);
        Report older = reports.get(1);

        Set<String> recentDescriptions = extractDescriptions(recent);
        Set<String> olderDescriptions = extractDescriptions(older);

        List<AnomalyDTO> fixed = filterAndMap(older, a -> !recentDescriptions.contains(a.getDescription()));
        List<AnomalyDTO> added = filterAndMap(recent, a -> !olderDescriptions.contains(a.getDescription()));
        List<AnomalyDTO> persistent = filterAndMap(recent, a -> olderDescriptions.contains(a.getDescription()));

        return new AuditVersionDTO(
                (int)older.getScore(),
                (int)recent.getScore(),
                (int)(recent.getScore() - older.getScore()),
                fixed,
                added,
                persistent
        );
    }
}
