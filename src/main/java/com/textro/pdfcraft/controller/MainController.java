package com.textro.pdfcraft.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.textro.pdfcraft.service.ConversionService;
import com.textro.pdfcraft.service.PdfService;

@Controller
public class MainController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private ConversionService conversionService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/editor")
    public String loadEditor() {
        return "editor";
    }
    // @GetMapping("/editor")
    // public String editor(@RequestParam(required = false) String file, Model model) {
    //     model.addAttribute("fileName", file);
    //     return "editor";
    // }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !conversionService.isSupportedFileType(filename)) {
                response.put("success", false);
                response.put("message", "Unsupported file type");
                return ResponseEntity.badRequest().body(response);
            }

            // Store file information
            response.put("success", true);
            response.put("fileName", filename);
            response.put("fileSize", file.getSize());
            response.put("fileType", conversionService.getFileType(filename));

            // If it's a PDF, get additional info
            if (filename.toLowerCase().endsWith(".pdf")) {
                Map<String, Object> pdfInfo = pdfService.getPdfInfo(file);
                response.putAll(pdfInfo);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/convert/to-pdf")
    @ResponseBody
    public ResponseEntity<byte[]> convertToPdf(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) {
                return ResponseEntity.badRequest().build();
            }

            byte[] pdfBytes;
            String fileType = conversionService.getFileType(filename);

            switch (fileType) {
                case "docx", "doc" ->
                    pdfBytes = conversionService.convertWordToPdf(file);
                case "xlsx", "xls" ->
                    pdfBytes = conversionService.convertExcelToPdf(file);
                case "pptx", "ppt" ->
                    pdfBytes = conversionService.convertPowerPointToPdf(file);
                case "png", "jpg", "jpeg", "gif", "bmp", "tiff" ->
                    pdfBytes = conversionService.convertImageToPdf(file);
                case "pdf" ->
                    pdfBytes = file.getBytes(); // Already PDF
                default -> {
                    return ResponseEntity.badRequest().build();
                }
            }

            String outputFilename = filename.substring(0, filename.lastIndexOf('.')) + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(outputFilename, StandardCharsets.UTF_8) + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/pdf/add-text")
    @ResponseBody
    public ResponseEntity<byte[]> addTextToPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text,
            @RequestParam("x") float x,
            @RequestParam("y") float y,
            @RequestParam("page") int page) {

        try {
            byte[] pdfBytes = pdfService.addTextToPdf(file, text, x, y, page);

            String filename = file.getOriginalFilename();
            String outputFilename = filename != null ? filename : "edited.pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(outputFilename, StandardCharsets.UTF_8) + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/pdf/merge")
    @ResponseBody
    public ResponseEntity<byte[]> mergePdfs(@RequestParam("files") List<MultipartFile> files) {
        try {
            byte[] mergedPdf = pdfService.mergePdfs(files);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"merged.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(mergedPdf);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/pdf/split")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> splitPdf(@RequestParam("file") MultipartFile file) {
        try {
            List<byte[]> pages = pdfService.splitPdf(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pageCount", pages.size());
            response.put("message", "PDF split into " + pages.size() + " pages");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error splitting PDF: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/pdf/watermark")
    @ResponseBody
    public ResponseEntity<byte[]> addWatermark(
            @RequestParam("file") MultipartFile file,
            @RequestParam("watermark") String watermarkText) {

        try {
            byte[] watermarkedPdf = pdfService.addWatermark(file, watermarkText);

            String filename = file.getOriginalFilename();
            String outputFilename = filename != null
                    ? filename.substring(0, filename.lastIndexOf('.')) + "_watermarked.pdf"
                    : "watermarked.pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(outputFilename, StandardCharsets.UTF_8) + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(watermarkedPdf);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/pdf/rotate")
    @ResponseBody
    public ResponseEntity<byte[]> rotatePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("degrees") int degrees) {

        try {
            byte[] rotatedPdf = pdfService.rotatePdf(file, degrees);

            String filename = file.getOriginalFilename();
            String outputFilename = filename != null
                    ? filename.substring(0, filename.lastIndexOf('.')) + "_rotated.pdf"
                    : "rotated.pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(outputFilename, StandardCharsets.UTF_8) + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(rotatedPdf);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/pdf/encrypt")
    @ResponseBody
    public ResponseEntity<byte[]> encryptPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {

        try {
            byte[] encryptedPdf = pdfService.encryptPdf(file, password);

            String filename = file.getOriginalFilename();
            String outputFilename = filename != null
                    ? filename.substring(0, filename.lastIndexOf('.')) + "_encrypted.pdf"
                    : "encrypted.pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(outputFilename, StandardCharsets.UTF_8) + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(encryptedPdf);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/pdf/extract-text")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            String text = pdfService.extractTextFromPdf(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("text", text);
            response.put("wordCount", text.split("\\s+").length);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error extracting text: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Textro PdfCraft");
        return ResponseEntity.ok(status);
    }
}
