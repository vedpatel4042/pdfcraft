package com.textro.pdfcraft.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.textro.pdfcraft.service.PdfService;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file type
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body("Only PDF files are allowed");
            }

            // Process the PDF (you can save it or process it as needed)
            String textContent = pdfService.extractTextFromPdf(file);

            // In a real application, you might save the file and return a reference
            return ResponseEntity.ok("PDF uploaded successfully. Text length: " + textContent.length());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error processing PDF: " + e.getMessage());
        }
    }
}
