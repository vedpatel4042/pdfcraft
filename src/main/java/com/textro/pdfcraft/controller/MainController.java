package com.textro.pdfcraft.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.textro.pdfcraft.service.PdfEditorService;
import com.textro.pdfcraft.service.PdfService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MainController {

    @Autowired
    private PdfEditorService pdfEditorService;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to PDF Craft API");
        response.put("version", "1.0.0");
        response.put("status", "active");
        response.put("endpoints", Map.of(
                "upload", "/api/upload",
                "info", "/api/info",
                "editor", "/api/editor/*",
                "pdf", "/api/pdf/*"
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pdf/document/{sessionId}")
    public ResponseEntity<Resource> getPdfDocument(@PathVariable String sessionId) {
        try {
            File pdfFile = pdfEditorService.getPdfFile(sessionId);
            Path path = pdfFile.toPath();
            Resource resource = new InputStreamResource(Files.newInputStream(path));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .contentLength(pdfFile.length())
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // @PostMapping("/upload")
    // public ResponseEntity<Map<String, Object>> uploadPdf(
    //         @RequestParam("file") MultipartFile file,
    //         @RequestParam(value = "mode", defaultValue = "editor") String mode) {
    //     try {
    //         Map<String, Object> response = new HashMap<>();
    //         if ("editor".equals(mode)) {
    //             // Upload for editor mode (session-based)
    //             String sessionId = pdfEditorService.processUpload(file);
    //             Map<String, Object> info = pdfEditorService.getPdfInfo(sessionId);
    //             response.put("success", true);
    //             response.put("sessionId", sessionId);
    //             response.put("mode", "editor");
    //             response.put("info", info);
    //             response.put("message", "PDF uploaded successfully for editing");
    //         } else {
    //             // Upload for processing mode (direct file operations)
    //             Map<String, Object> info = pdfService.getPdfInfo(file);
    //             response.put("success", true);
    //             response.put("mode", "processing");
    //             response.put("info", info);
    //             response.put("message", "PDF uploaded successfully for processing");
    //         }
    //         return ResponseEntity.ok(response);
    //     } catch (IOException e) {
    //         Map<String, Object> errorResponse = new HashMap<>();
    //         errorResponse.put("success", false);
    //         errorResponse.put("error", "Failed to upload PDF: " + e.getMessage());
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    //     }
    // }
    // @PostMapping("/upload")
    // public ResponseEntity<Map<String, Object>> uploadPdf(
    //         @RequestParam("file") MultipartFile file,
    //         @RequestParam(value = "mode", defaultValue = "editor") String mode) {
    //     try {
    //         Map<String, Object> response = new HashMap<>();
    //         if (file.isEmpty()) {
    //             response.put("success", false);
    //             response.put("error", "File is empty");
    //             return ResponseEntity.badRequest().body(response);
    //         }
    //         if ("editor".equals(mode)) {
    //             String sessionId = pdfEditorService.processUpload(file);
    //             Map<String, Object> info = pdfEditorService.getPdfInfo(sessionId);
    //             response.put("success", true);
    //             response.put("sessionId", sessionId);
    //             response.put("mode", "editor");
    //             response.put("info", info);
    //             response.put("message", "PDF uploaded successfully for editing");
    //         } else {
    //             Map<String, Object> info = pdfService.getPdfInfo(file);
    //             response.put("success", true);
    //             response.put("mode", "processing");
    //             response.put("info", info);
    //             response.put("message", "PDF uploaded successfully for processing");
    //         }
    //         return ResponseEntity.ok(response);
    //     } catch (IOException e) {
    //         Map<String, Object> errorResponse = new HashMap<>();
    //         errorResponse.put("success", false);
    //         errorResponse.put("error", "Failed to upload PDF: " + e.getMessage());
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    //     }
    // }
    @PostMapping("/info")
    public ResponseEntity<Map<String, Object>> getPdfInfo(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> info = pdfService.getPdfInfo(file);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("info", info);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get PDF info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("timestamp", System.currentTimeMillis());
        response.put("services", Map.of(
                "pdfEditor", "active",
                "pdfProcessor", "active"
        ));
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", e.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @GetMapping("/editor")
    public String editor() {
        return "forward:/editor.html";
    }

    @GetMapping("/api/editor/session/{sessionId}/pdf")
    public ResponseEntity<byte[]> getSessionPdf(@PathVariable String sessionId) {
        try {
            byte[] pdfBytes = pdfEditorService.getPdfAsBytes(sessionId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
