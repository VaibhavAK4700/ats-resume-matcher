package com.ats.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class PdfGeneratorService {

    private static final Color PRIMARY_ACCENT = new Color(30, 58, 138);
    private static final Color TEXT_DARK = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color DIVIDER_COLOR = new Color(229, 231, 235);

    /**
     * Overload method required by AutomatedAtsService.
     * Generates a PDF resume using default match scoring.
     */
    public byte[] generatePdf(String textContent) {
        return generateSuggestedResumePdf(textContent, 100);
    }

    /**
     * Generates a tailored resume PDF document using Markdown content parsing.
     */
    public byte[] generateSuggestedResumePdf(String textContent, int score) {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_ACCENT);
            Font scoreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, TEXT_MUTED);
            Font h1Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, PRIMARY_ACCENT);
            Font h2Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, TEXT_DARK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_DARK);
            Font bodyBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_DARK);

            Paragraph title = new Paragraph("TAILORED RESUME RECOMMENDATION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph scorePara = new Paragraph("ATS Optimization Score: " + score + "%", scoreFont);
            scorePara.setAlignment(Element.ALIGN_CENTER);
            scorePara.setSpacingAfter(8f);
            document.add(scorePara);

            LineSeparator line = new LineSeparator(1f, 100, DIVIDER_COLOR, Element.ALIGN_CENTER, -2);
            document.add(line);
            document.add(Chunk.NEWLINE);

            if (textContent != null && !textContent.isBlank()) {
                parseAndAppendMarkdownContent(document, textContent, h1Font, h2Font, bodyFont, bodyBoldFont);
            } else {
                document.add(new Paragraph("No tailored resume content provided.", bodyFont));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate suggested resume PDF: ", e);
            throw new RuntimeException("Error rendering PDF document", e);
        }
    }

    /**
     * Generates an ATS Match Analysis Report stream.
     */
    public ByteArrayInputStream generateAtsReport(Map<String, Object> analysisData) {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_ACCENT);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_ACCENT);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_DARK);

            Paragraph title = new Paragraph("ATS Resume Match Analysis Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            LineSeparator line = new LineSeparator(1f, 100, DIVIDER_COLOR, Element.ALIGN_CENTER, -2);
            document.add(Chunk.NEWLINE);
            document.add(line);
            document.add(Chunk.NEWLINE);

            String matchScore = analysisData.getOrDefault("matchScore", "N/A").toString();
            Paragraph scorePara = new Paragraph();
            scorePara.add(new Chunk("Overall Match Score: ", sectionFont));
            scorePara.add(new Chunk(matchScore + "%", titleFont));
            scorePara.setSpacingAfter(10f);
            document.add(scorePara);

            addSectionHeader(document, "Matching Keywords", sectionFont);
            addListToDocument(document, extractList(analysisData.get("matchingKeywords")), bodyFont);

            addSectionHeader(document, "Missing Keywords", sectionFont);
            addListToDocument(document, extractList(analysisData.get("missingKeywords")), bodyFont);

            addSectionHeader(document, "Improvement Recommendations", sectionFont);
            String feedback = analysisData.getOrDefault("feedback", "No specific recommendations.").toString();
            Paragraph feedbackPara = new Paragraph(feedback, bodyFont);
            feedbackPara.setSpacingBefore(4f);
            document.add(feedbackPara);

            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Failed to generate ATS report PDF: ", e);
            throw new RuntimeException("Error rendering ATS analysis report PDF", e);
        }
    }

    private void parseAndAppendMarkdownContent(Document document, String textContent, Font h1Font, Font h2Font, Font bodyFont, Font bodyBoldFont) throws DocumentException {
        String[] lines = textContent.split("\\r?\\n");
        List currentList = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                if (currentList != null) {
                    document.add(currentList);
                    currentList = null;
                }
                document.add(Chunk.NEWLINE);
                continue;
            }

            if (line.startsWith("# ")) {
                if (currentList != null) { document.add(currentList); currentList = null; }
                Paragraph h1 = new Paragraph(line.substring(2).replace("`", ""), h1Font);
                h1.setSpacingBefore(12f);
                h1.setSpacingAfter(4f);
                document.add(h1);
                document.add(new LineSeparator(0.5f, 100, DIVIDER_COLOR, Element.ALIGN_CENTER, -1));
            } else if (line.startsWith("## ") || line.startsWith("### ")) {
                if (currentList != null) { document.add(currentList); currentList = null; }
                Paragraph h2 = new Paragraph(line.replace("#", "").trim().replace("`", ""), h2Font);
                h2.setSpacingBefore(8f);
                h2.setSpacingAfter(3f);
                document.add(h2);
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                if (currentList == null) {
                    currentList = new List(List.UNORDERED);
                    currentList.setListSymbol(new Chunk("• ", bodyFont));
                    currentList.setIndentationLeft(12f);
                }
                currentList.add(new ListItem(buildFormattedParagraph(line.substring(2).replace("`", ""), bodyFont, bodyBoldFont)));
            } else {
                if (currentList != null) { document.add(currentList); currentList = null; }
                Paragraph para = buildFormattedParagraph(line.replace("`", ""), bodyFont, bodyBoldFont);
                para.setSpacingAfter(4f);
                document.add(para);
            }
        }
        if (currentList != null && !currentList.isEmpty()) {
            document.add(currentList);
        }
    }

    private Paragraph buildFormattedParagraph(String text, Font normalFont, Font boldFont) {
        Paragraph para = new Paragraph();
        String[] parts = text.split("(?<=\\*\\*)|(?=\\*\\*)");
        boolean isBold = false;
        for (String part : parts) {
            if ("**".equals(part)) {
                isBold = !isBold;
            } else if (!part.isEmpty()) {
                para.add(new Chunk(part, isBold ? boldFont : normalFont));
            }
        }
        return para;
    }

    private void addSectionHeader(Document document, String headerText, Font font) throws DocumentException {
        Paragraph header = new Paragraph(headerText, font);
        header.setSpacingBefore(12f);
        header.setSpacingAfter(4f);
        document.add(header);
    }

    private void addListToDocument(Document document, Collection<?> items, Font font) throws DocumentException {
        if (items == null || items.isEmpty()) {
            Paragraph nonePara = new Paragraph("None reported.", font);
            nonePara.setSpacingAfter(4f);
            document.add(nonePara);
            return;
        }
        List list = new List(List.UNORDERED);
        list.setListSymbol(new Chunk("• ", font));
        list.setIndentationLeft(12f);
        for (Object item : items) {
            list.add(new ListItem(item.toString(), font));
        }
        document.add(list);
    }

    private Collection<?> extractList(Object obj) {
        if (obj instanceof Collection<?>) {
            return (Collection<?>) obj;
        }
        return Collections.emptyList();
    }
}