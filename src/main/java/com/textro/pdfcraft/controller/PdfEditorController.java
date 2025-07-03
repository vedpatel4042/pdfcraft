package com.textro.pdfcraft.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.textro.pdfcraft.model.PdfEditRequest;
import com.textro.pdfcraft.model.PdfPageState;
import com.textro.pdfcraft.service.PdfEditorService;

@RestController
@RequestMapping("/api/editor")
@CrossOrigin(origins = "*")
public class PdfEditorController {

    @Autowired
    private PdfEditorService pdfEditorService;

    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> createSession(
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            String sessionId = pdfEditorService.processUpload(file);
            Map<String, Object> info = pdfEditorService.getPdfInfo(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("info", info);
            response.put("message", "Editor session created successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/session/{sessionId}/info")
    public ResponseEntity<Map<String, Object>> getSessionInfo(@PathVariable String sessionId) {
        try {
            Map<String, Object> info = pdfEditorService.getPdfInfo(sessionId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("info", info);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get session info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/session/{sessionId}/page/{pageNumber}")
    public ResponseEntity<Map<String, Object>> getPageState(
            @PathVariable String sessionId,
            @PathVariable int pageNumber) {

        try {
            PdfPageState state = pdfEditorService.getPageState(sessionId, pageNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pageState", state);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get page state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/session/{sessionId}/text")
    public ResponseEntity<Map<String, Object>> addText(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {

        try {
            int pageNumber = (Integer) request.get("pageNumber");
            String text = (String) request.get("text");
            float x = ((Number) request.get("x")).floatValue();
            float y = ((Number) request.get("y")).floatValue();
            float fontSize = ((Number) request.getOrDefault("fontSize", 12)).floatValue();
            String fontFamily = (String) request.getOrDefault("fontFamily", "helvetica");
            String color = (String) request.getOrDefault("color", "#000000");

            String textId = pdfEditorService.addTextToPdf(sessionId, pageNumber, text, x, y, fontSize, fontFamily, color);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("textId", textId);
            response.put("message", "Text added successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to add text: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/session/{sessionId}/text/{textId}")
    public ResponseEntity<Map<String, Object>> editText(
            @PathVariable String sessionId,
            @PathVariable String textId,
            @RequestBody Map<String, Object> request) {

        try {
            int pageNumber = (Integer) request.get("pageNumber");
            String text = (String) request.get("text");
            float x = ((Number) request.get("x")).floatValue();
            float y = ((Number) request.get("y")).floatValue();
            float fontSize = ((Number) request.getOrDefault("fontSize", 12)).floatValue();
            String fontFamily = (String) request.getOrDefault("fontFamily", "helvetica");
            String color = (String) request.getOrDefault("color", "#000000");

            pdfEditorService.editTextInPdf(sessionId, pageNumber, textId, text, x, y, fontSize, fontFamily, color);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Text edited successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to edit text: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/session/{sessionId}/text/{textId}")
    public ResponseEntity<Map<String, Object>> deleteText(
            @PathVariable String sessionId,
            @PathVariable String textId,
            @RequestParam int pageNumber) {

        try {
            pdfEditorService.deleteTextFromPdf(sessionId, pageNumber, textId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Text deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete text: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/session/{sessionId}/page")
    public ResponseEntity<Map<String, Object>> addPage(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {

        try {
            int position = (Integer) request.getOrDefault("position", -1);
            String pageType = (String) request.getOrDefault("pageType", "blank");

            PdfPageState newState = pdfEditorService.addPage(sessionId, position, pageType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pageState", newState);
            response.put("message", "Page added successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to add page: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/session/{sessionId}/page/{pageNumber}")
    public ResponseEntity<Map<String, Object>> deletePage(
            @PathVariable String sessionId,
            @PathVariable int pageNumber) {

        try {
            PdfPageState state = pdfEditorService.deletePage(sessionId, pageNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pageState", state);
            response.put("message", "Page deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete page: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/session/{sessionId}/merge")
    public ResponseEntity<Map<String, Object>> mergePdf(
            @PathVariable String sessionId,
            @RequestParam("file") MultipartFile mergeFile) {

        try {
            int newPageCount = pdfEditorService.mergePdf(sessionId, mergeFile);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newPageCount", newPageCount);
            response.put("message", "PDF merged successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to merge PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/session/{sessionId}/split")
    public ResponseEntity<Map<String, Object>> splitPdf(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {

        try {
            int splitAtPage = (Integer) request.get("splitAtPage");
            String newSessionId = pdfEditorService.splitPdf(sessionId, splitAtPage);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newSessionId", newSessionId);
            response.put("message", "PDF split successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to split PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/session/{sessionId}/page/{pageNumber}/image")
    public ResponseEntity<byte[]> getPageImage(
            @PathVariable String sessionId,
            @PathVariable int pageNumber,
            @RequestParam(value = "dpi", defaultValue = "150") int dpi) {

        try {
            byte[] imageBytes = pdfEditorService.renderPageAsImage(sessionId, pageNumber, dpi);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return ResponseEntity.ok().headers(headers).body(imageBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/session/{sessionId}/page/{pageNumber}/text")
    public ResponseEntity<Map<String, Object>> extractPageText(
            @PathVariable String sessionId,
            @PathVariable int pageNumber) {

        try {
            String text = pdfEditorService.extractTextFromPage(sessionId, pageNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("text", text);
            response.put("length", text.length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to extract text: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/session/{sessionId}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String sessionId) {
        try {
            byte[] pdfBytes = pdfEditorService.getPdfAsBytes(sessionId);
            String fileName = pdfEditorService.getFileName(sessionId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/session/{sessionId}/save")
    public ResponseEntity<Map<String, Object>> savePdf(@PathVariable String sessionId) {
        try {
            pdfEditorService.autoSavePdf(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF saved successfully");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to save PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> cleanupSession(@PathVariable String sessionId) {
        try {
            pdfEditorService.cleanupSession(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Session cleaned up successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to cleanup session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/edit")
    public ResponseEntity<Map<String, Object>> applyEdit(@RequestBody PdfEditRequest request) {
        try {
            pdfEditorService.applyTextEdit(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Edit applied successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to apply edit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/session/{sessionId}/existing-text/{pageNumber}")
    public ResponseEntity<Map<String, Object>> getExistingText(
            @PathVariable String sessionId,
            @PathVariable int pageNumber) {

        try {
            var existingElements = pdfEditorService.getExistingTextElements(sessionId, pageNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("elements", existingElements);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get existing text: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/session/{sessionId}/pdf")
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", e.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
