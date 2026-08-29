package com.ats.service;

import com.ats.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class PdfParserService {

    @Value("${ats.pdf.max-file-size-mb:10}")
    private int maxFileSizeMb;

    /**
     * Extracts text from Spring MultipartFile uploads.
     * Required by AutomatedAtsService.
     */
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("Uploaded PDF file is empty or null.");
        }

        try {
            return extractTextFromPdf(file.getBytes());
        } catch (IOException e) {
            log.error("Failed to read bytes from uploaded MultipartFile '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new FileProcessingException("Failed to read uploaded PDF file.", e);
        }
    }

    /**
     * Extracts text from raw PDF byte arrays.
     */
    public String extractTextFromPdf(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new FileProcessingException("Uploaded PDF file is empty or null.");
        }

        long sizeLimit = (long) maxFileSizeMb * 1024 * 1024;
        if (pdfBytes.length > sizeLimit) {
            log.error("PDF size exceeds maximum configured threshold ({} MB)", maxFileSizeMb);
            throw new FileProcessingException("PDF file size exceeds maximum allowed limit of " + maxFileSizeMb + " MB.");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new FileProcessingException("Cannot extract text from password-protected or encrypted PDF.");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String extractedText = stripper.getText(document);

            if (extractedText == null || extractedText.trim().isBlank()) {
                log.warn("PDF parsed successfully but yielded no readable text. May be an image-only scan.");
                throw new FileProcessingException("Extracted text is empty. If this is a scanned document or image, text cannot be parsed directly.");
            }

            return normalizeExtractedText(extractedText);

        } catch (InvalidPasswordException e) {
            log.error("PDF is password protected: {}", e.getMessage());
            throw new FileProcessingException("Uploaded PDF is password-protected.", e);
        } catch (IOException e) {
            log.error("Failed to parse PDF document structure: {}", e.getMessage(), e);
            throw new FileProcessingException("Failed to extract text from PDF file. File may be corrupted.", e);
        }
    }

    private String normalizeExtractedText(String rawText) {
        if (rawText == null) return "";
        return rawText
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                .replace('\u00A0', ' ')
                .replaceAll("\\r\\n?", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .replaceAll("(?m)[ \t]+$", "")
                .trim();
    }
}