package com.example.backend.modules.projects.audit.service;

import com.example.backend.modules.projects.audit.entity.Anomaly;
import com.example.backend.modules.projects.audit.entity.Report;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReportService {

    public byte[] generateAuditPdf(Report report) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);

        document.open();

        //Titre et Header

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
        Paragraph title = new Paragraph("Rapport d'Audit de Cohérence", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" "));

        // 2. Infos Projet & Score
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        document.add(new Paragraph("Projet : " + report.getProject().getName(), subTitleFont));
        document.add(new Paragraph("Date : " + report.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        Paragraph scorePara = new Paragraph("Score Final : " + report.getScore() + "/100",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, report.getScore() > 70 ? Color.GREEN : Color.RED));
        document.add(scorePara);

        document.add(new Paragraph(" "));

        // 3. Tableau des Anomalies
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 5, 3});


        addTableHeader(table);

        for (Anomaly anomaly : report.getAnomalies()) {
            table.addCell(createSeverityCell(anomaly.getSeverity().name()));
            table.addCell(new Phrase(anomaly.getDescription()));
            String suggestion = (anomaly.getSuggestion() != null) ? anomaly.getSuggestion().getContent() : "N/A";
            table.addCell(new Phrase(suggestion));
        }

        document.add(table);
        document.close();
        return outputStream.toByteArray();
    }

    private void addTableHeader(PdfPTable table) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setPadding(5);

        String[] headers = {"Sévérité", "Description", "Suggestion"};
        for (String h : headers) {
            header.setPhrase(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(header);
        }
    }

    private PdfPCell createSeverityCell(String severity) {
        PdfPCell cell = new PdfPCell(new Phrase(severity));
        if ("CRITICAL".equals(severity)){
            cell.setBackgroundColor(new Color(255, 0, 0));
        }
        else if ("HIGH".equals(severity)){
            cell.setBackgroundColor(new Color(255, 0, 150));
        }
        else if ("MEDIUM".equals(severity)) {
            cell.setBackgroundColor(new Color(230, 230, 0));
        }
        else if("LOW".equals(severity)){
            cell.setBackgroundColor(new Color(200, 200, 200));
        }
        return cell;
    }






}
