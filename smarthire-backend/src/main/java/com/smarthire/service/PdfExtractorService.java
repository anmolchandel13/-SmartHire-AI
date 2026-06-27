package com.smarthire.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * PdfExtractorService — Extracts plain text content from PDF resume files.
 *
 * This service uses Apache PDFBox, a Java library specifically designed for
 * working with PDF documents. Here's what happens internally:
 *
 *   1. PDFBox opens the PDF binary data
 *   2. PDFTextStripper iterates through every page
 *   3. It reads all text content, preserving basic formatting
 *   4. Returns the complete text as a single String
 *
 * Why PDFBox?
 *   - It's pure Java (no native dependencies, no external tools needed)
 *   - It's well-maintained by the Apache Foundation
 *   - It handles most PDF formats including scanned text-based PDFs
 *   - It's free and open source
 *
 * Limitations:
 *   - Cannot extract text from image-only PDFs (scanned documents without OCR)
 *   - May not perfectly preserve table formatting
 *   - Very large PDFs (100+ pages) may be slow
 *
 * For a resume (typically 1-3 pages), extraction is nearly instant.
 */
@Service
@Slf4j
public class PdfExtractorService {

    /**
     * Extract text from an uploaded PDF file.
     *
     * @param file the uploaded MultipartFile containing a PDF
     * @return the extracted plain text content
     * @throws RuntimeException if the PDF cannot be read or text cannot be extracted
     */
    public String extractText(MultipartFile file) {
        log.info("Starting PDF text extraction for file: {}", file.getOriginalFilename());

        try {
            // Load the PDF document from the file's input stream bytes
            PDDocument document = Loader.loadPDF(file.getBytes());

            // Create a text stripper to extract text from all pages
            PDFTextStripper textStripper = new PDFTextStripper();

            // Configure the stripper
            textStripper.setSortByPosition(true);  // Read text in visual order (left-to-right, top-to-bottom)

            // Extract all text from the document
            String extractedText = textStripper.getText(document);

            // Close the document to free resources
            document.close();

            // Clean up the extracted text
            extractedText = cleanExtractedText(extractedText);

            log.info("Successfully extracted {} characters from PDF: {}",
                    extractedText.length(), file.getOriginalFilename());

            return extractedText;

        } catch (IOException ex) {
            log.error("Failed to extract text from PDF: {} - {}", file.getOriginalFilename(), ex.getMessage());
            throw new RuntimeException("Failed to extract text from PDF: " + ex.getMessage(), ex);
        }
    }

    /**
     * Clean up extracted text by removing excessive whitespace and empty lines.
     *
     * PDFBox often produces text with irregular spacing (because PDF text
     * positioning is based on coordinates, not logical structure).
     * This method normalizes the text for better AI analysis.
     *
     * @param text the raw extracted text
     * @return cleaned text with normalized whitespace
     */
    private String cleanExtractedText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text
                // Replace multiple consecutive blank lines with a single blank line
                .replaceAll("(\\n\\s*){3,}", "\n\n")
                // Replace multiple spaces with a single space
                .replaceAll(" {2,}", " ")
                // Trim leading/trailing whitespace
                .trim();
    }
}
