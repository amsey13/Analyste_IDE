package com.example.backend.modules.projects.audit.service;

import com.example.backend.modules.projects.audit.dto.AnomalyDTO;
import com.example.backend.modules.projects.audit.dto.AuditVersionDTO;
import com.example.backend.modules.projects.audit.entity.Anomaly;
import com.example.backend.modules.projects.audit.entity.Report;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReportService {

    private static final Color PRIMARY_BLUE = new Color(31, 53, 94);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color WARNING_ORANGE = new Color(240, 84, 41);
    private static final Color LIGHT_BG = new Color(248, 249, 250);
    private static final Color TEXT_GRAY = new Color(95, 111, 134);

    // Polices changez selon le style que vous voulez critiqueurs
    private final Font corpFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, PRIMARY_BLUE);
    private final Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private final Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, PRIMARY_BLUE);
    private final Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

    /**
     * Generates a PDF audit report for a given project report.
     *
     * @param report the audit report data
     * @param diff   the audit version differences (can be null for first audit)
     * @return the generated PDF as a byte array
     * @throws DocumentException if PDF generation fails
     */
    public byte[] generateAuditPdf(Report report, AuditVersionDTO diff) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter.getInstance(document, outputStream);

        document.open();

        addBrandHeader(document);

        addProjectInfo(document, report);

        addScoreSection(document, report.getScore());

        if (diff != null) {
            addEvolutionSection(document, diff);
        } else {
            document.add(new Paragraph("Premier audit de cohérence pour ce projet.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, TEXT_GRAY)));
        }

        addAnomaliesSection(document, report);

        document.close();
        return outputStream.toByteArray();
    }

    /**
     * Adds the branded header section to the PDF document.
     *
     * @param document the PDF document
     * @throws DocumentException if writing to document fails
     */
    private void addBrandHeader(Document document) throws DocumentException {
        Paragraph header = new Paragraph("MamadyCorporation", corpFont);
        header.setAlignment(Element.ALIGN_RIGHT);
        document.add(header);
        document.add(new Paragraph(new Chunk(new LineSeparator(1, 100, PRIMARY_BLUE, Element.ALIGN_CENTER, -2))));
        document.add(new Paragraph(" "));
    }

    /**
     * Adds project metadata (name and generation date) to the PDF.
     *
     * @param document the PDF document
     * @param report   the audit report
     * @throws DocumentException if writing to document fails
     */
    private void addProjectInfo(Document document, Report report) throws DocumentException {
        document.add(new Paragraph("Rapport d'Audit de Cohérence", titleFont));
        String meta = String.format("Projet : %s | Généré le : %s",
                report.getProject().getName(),
                report.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        document.add(new Paragraph(meta, FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_GRAY)));
        document.add(new Paragraph(" "));
    }

    /**
     * Adds the compliance score section to the PDF.
     *
     * @param document the PDF document
     * @param score    the compliance score (0–100)
     * @throws DocumentException if writing to document fails
     */
    private void addScoreSection(Document document, double score) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(35);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_BG);
        cell.setPadding(10);
        cell.setBorderColor(new Color(222, 226, 230));

        cell.addElement(new Paragraph("Taux de Conformité", bodyFont));
        Color scoreColor = score >= 70 ? SUCCESS_GREEN : WARNING_ORANGE;
        Paragraph pScore = new Paragraph((int) score + "%",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, scoreColor));
        pScore.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pScore);

        table.addCell(cell);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    /**
     * Adds the evolution section comparing audit versions.
     *
     * @param document the PDF document
     * @param diff     the version diff data
     * @throws DocumentException if writing to document fails
     */
    private void addEvolutionSection(Document document, AuditVersionDTO diff) throws DocumentException {

        Color evolColor = diff.scoreGap() >= 0 ? SUCCESS_GREEN : Color.RED;
        String sign = diff.scoreGap() >= 0 ? "+" : "";

        Paragraph p = new Paragraph(String.format("Évolution : %s%d pts", sign, diff.scoreGap()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, evolColor));
        p.add(new Chunk(String.format(" (%d corrigées | %d nouvelles)",
                diff.fixedAnomalies().size(), diff.newAnomalies().size()),
                FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_GRAY)));
        document.add(p);

        renderAnomalyList(document, "Anomalies résolues", diff.fixedAnomalies(), SUCCESS_GREEN, "✓ ");
        renderAnomalyList(document, "Nouvelles anomalies détectées", diff.newAnomalies(), new Color(255, 140, 0), "⚠ ");
        document.add(new Paragraph(" "));
    }

    /**
     * Renders a list of anomalies into the PDF document.
     *
     * @param document the PDF document
     * @param title    the section title
     * @param list     list of anomalies
     * @param color    text color for the section
     * @param symbol   prefix symbol for each item
     * @throws DocumentException if writing to document fails
     */
    private void renderAnomalyList(Document document, String title, java.util.List<AnomalyDTO> list, Color color,
            String symbol) throws DocumentException {
        if (list.isEmpty()) {
            return;
        }

        document.add(new Paragraph(title + " :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color)));
        com.lowagie.text.List pdfList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
        pdfList.setListSymbol(new Chunk("  " + symbol, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color)));

        for (AnomalyDTO dto : list) {
            pdfList.add(new ListItem(dto.description(), bodyFont));
        }
        document.add(pdfList);
    }

    /**
     * The function adds a section titled "Analyse détaillée et Solutions" to a
     * document and then
     * iterates through a list of anomalies in a report, adding each anomaly as a
     * card to the document.
     * 
     * @param document The `document` parameter is of type `Document` and is used to
     *                 represent the PDF
     *                 document that you are generating. It is where you add content
     *                 such as paragraphs, tables,
     *                 images, etc.
     * @param report   The `report` parameter is an object that contains information
     *                 about anomalies that
     *                 need to be analyzed and solutions that need to be
     *                 implemented. It likely has a method
     *                 `getAnomalies()` that returns a list of `Anomaly` objects.
     */
    private void addAnomaliesSection(Document document, Report report) throws DocumentException {
        document.add(new Paragraph("Analyse détaillée et Solutions :", sectionTitleFont));
        document.add(new Paragraph(" "));

        for (Anomaly anomaly : report.getAnomalies()) {
            addAnomalyCard(document, anomaly);
            document.add(new Paragraph(" "));
        }
    }

    /**
     * The `addAnomalyCard` function creates a PDF table with anomaly details and
     * recommendations to be
     * added to a document.
     * 
     * @param document The `document` parameter in the `addAnomalyCard` method
     *                 represents the PDF
     *                 document to which you want to add an anomaly card. This
     *                 method creates a card layout for
     *                 displaying information about an anomaly and adds it to the
     *                 specified PDF document. The anomaly
     *                 details such as type, severity, description,
     * @param anomaly  Anomaly object containing information about an anomaly such
     *                 as type, severity,
     *                 description, and suggestion.
     */
    private void addAnomalyCard(Document document, Anomaly anomaly) throws DocumentException {
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(100);

        PdfPCell head = new PdfPCell();
        head.setBackgroundColor(new Color(255, 243, 205));
        head.setPadding(8);
        head.addElement(new Phrase("⚠ " + anomaly.getAnomalyType().getWording() + " (" + anomaly.getSeverity() + ")",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(133, 100, 4))));
        head.addElement(new Paragraph(anomaly.getDescription(), bodyFont));
        card.addCell(head);

        PdfPCell foot = new PdfPCell();
        foot.setBackgroundColor(new Color(232, 244, 255));
        foot.setPadding(8);
        foot.addElement(
                new Phrase("→ Recommandation :", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PRIMARY_BLUE)));
        String sug = (anomaly.getSuggestion() != null) ? anomaly.getSuggestion().getContent() : "Aucune suggestion.";
        foot.addElement(new Paragraph(sug, FontFactory.getFont(FontFactory.HELVETICA, 10, PRIMARY_BLUE)));
        card.addCell(foot);

        document.add(card);
    }

}
