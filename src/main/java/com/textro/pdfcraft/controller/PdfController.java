package com.textro.pdfcraft.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.textro.pdfcraft.service.PdfService;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @PostMapping("/add-text")
    public ResponseEntity<byte[]> addText(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text,
            @RequestParam("x") float x,
            @RequestParam("y") float y,
            @RequestParam(value = "page", defaultValue = "1") int page) {

        try {
            byte[] pdfBytes = pdfService.addTextToPdf(file, text, x, y, page);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "modified_" + file.getOriginalFilename());

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/split")
    public ResponseEntity<Map<String, Object>> splitPdf(@RequestParam("file") MultipartFile file) {
        try {
            List<byte[]> pages = pdfService.splitPdf(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalPages", pages.size());
            response.put("message", "PDF split successfully");
            response.put("pages", pages.size()); // In real implementation, you'd return download links

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to split PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/watermark")
    public ResponseEntity<byte[]> addWatermark(
            @RequestParam("file") MultipartFile file,
            @RequestParam("watermark") String watermarkText) {

        try {
            byte[] pdfBytes = pdfService.addWatermark(file, watermarkText);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "watermarked_" + file.getOriginalFilename());

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/rotate")
    public ResponseEntity<byte[]> rotatePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "degrees", defaultValue = "90") int degrees) {

        try {
            byte[] pdfBytes = pdfService.rotatePdf(file, degrees);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "rotated_" + file.getOriginalFilename());

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/encrypt")
    public ResponseEntity<byte[]> encryptPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) {

        try {
            byte[] pdfBytes = pdfService.encryptPdf(file, password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "encrypted_" + file.getOriginalFilename());

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/extract-text")
    public ResponseEntity<Map<String, Object>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            String text = pdfService.extractTextFromPdf(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("text", text);
            response.put("length", text.length());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to extract text: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergePdfs(@RequestParam("files") MultipartFile[] files) {
        try {
            // Note: This is a simplified merge operation
            // In a real implementation, you'd need to modify the service to handle multiple files
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF merge functionality needs to be implemented");
            response.put("filesReceived", files.length);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to merge PDFs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/compress")
    public ResponseEntity<Map<String, Object>> compressPdf(@RequestParam("file") MultipartFile file) {
        try {
            // Note: Compression functionality would need to be implemented in the service
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF compression functionality needs to be implemented");
            response.put("originalSize", file.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to compress PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

// package com.textro.pdfcraft.controller;
// import java.util.Map;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseBody;
// import org.springframework.web.multipart.MultipartFile;
// import com.textro.pdfcraft.service.PdfService;
// import lombok.RequiredArgsConstructor;
// @Controller
// @RequiredArgsConstructor
// @RequestMapping("/pdf")
// public class PdfController {
//     private final PdfService pdfService;
//     @PostMapping("/upload")
//     @ResponseBody
//     public ResponseEntity<Map<String, Object>> uploadPdf(@RequestParam("file") MultipartFile file) {
//         try {
//             Map<String, Object> info = pdfService.getPdfInfo(file);
//             return ResponseEntity.ok(info);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//         }
//     }
//     @PostMapping("/add-text")
//     @ResponseBody
//     public ResponseEntity<byte[]> addTextToPdf(
//             @RequestParam MultipartFile file,
//             @RequestParam String text,
//             @RequestParam float x,
//             @RequestParam float y,
//             @RequestParam int page
//     ) {
//         try {
//             byte[] modifiedPdf = pdfService.addTextToPdf(file, text, x, y, page);
//             return ResponseEntity.ok()
//                     .header("Content-Type", "application/pdf")
//                     .body(modifiedPdf);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().build();
//         }
//     }
// }
// // package com.textro.pdfcraft.controller;
// // import com.textro.pdfcraft.model.TextEditRequest;
// // import com.textro.pdfcraft.service.PdfService;
// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.http.HttpHeaders;
// // import org.springframework.http.MediaType;
// // import org.springframework.http.ResponseEntity;
// // import org.springframework.web.bind.annotation.*;
// // import org.springframework.web.multipart.MultipartFile;
// // import java.io.IOException;
// // import java.util.Map;
// // @RestController
// // @RequestMapping("/api/pdf")
// // @CrossOrigin(origins = "*")
// // public class PdfController {
// //     @Autowired
// //     private PdfService pdfService;
// //     @PostMapping("/upload")
// //     public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
// //         try {
// //             if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
// //                 return ResponseEntity.badRequest()
// //                         .body(Map.of("error", "Please upload a valid PDF file"));
// //             }
// //             String sessionId = pdfService.storePdfInSession(file);
// //             int pageCount = pdfService.getPageCount(sessionId);
// //             return ResponseEntity.ok(Map.of(
// //                     "sessionId", sessionId,
// //                     "pageCount", pageCount,
// //                     "fileName", file.getOriginalFilename(),
// //                     "message", "PDF uploaded successfully"
// //             ));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Failed to upload PDF: " + e.getMessage()));
// //         }
// //     }
// //     @PostMapping("/add-text")
// //     public ResponseEntity<?> addText(@RequestBody TextEditRequest request) {
// //         try {
// //             String textId = pdfService.addTextToPdf(
// //                     request.getSessionId(),
// //                     request.getPageNumber(),
// //                     request.getText(),
// //                     request.getX(),
// //                     request.getY(),
// //                     request.getFontSize(),
// //                     request.getFontFamily(),
// //                     request.getColor()
// //             );
// //             return ResponseEntity.ok(Map.of(
// //                     "success", true,
// //                     "textId", textId,
// //                     "message", "Text added successfully"
// //             ));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Failed to add text: " + e.getMessage()));
// //         }
// //     }
// //     @PutMapping("/edit-text")
// //     public ResponseEntity<?> editText(@RequestBody TextEditRequest request) {
// //         try {
// //             pdfService.editTextInPdf(
// //                     request.getSessionId(),
// //                     request.getPageNumber(),
// //                     request.getTextId(),
// //                     request.getText(),
// //                     request.getX(),
// //                     request.getY(),
// //                     request.getFontSize(),
// //                     request.getFontFamily(),
// //                     request.getColor()
// //             );
// //             return ResponseEntity.ok(Map.of(
// //                     "success", true,
// //                     "message", "Text updated successfully"
// //             ));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Failed to edit text: " + e.getMessage()));
// //         }
// //     }
// //     @DeleteMapping("/delete-text")
// //     public ResponseEntity<?> deleteText(
// //             @RequestParam String sessionId,
// //             @RequestParam int pageNumber,
// //             @RequestParam String textId) {
// //         try {
// //             pdfService.deleteTextFromPdf(sessionId, pageNumber, textId);
// //             return ResponseEntity.ok(Map.of(
// //                     "success", true,
// //                     "message", "Text deleted successfully"
// //             ));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Failed to delete text: " + e.getMessage()));
// //         }
// //     }
// //     @DeleteMapping("/delete-page/{pageNumber}")
// //     public ResponseEntity<?> deletePage(
// //             @PathVariable int pageNumber,
// //             @RequestParam String sessionId) {
// //         try {
// //             int remainingPages = pdfService.deletePage(sessionId, pageNumber);
// //             return ResponseEntity.ok(Map.of(
// //                     "success", true,
// //                     "remainingPages", remainingPages,
// //                     "message", "Page deleted successfully"
// //             ));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Failed to delete page: " + e.getMessage()));
// //         }
// //     }
// //     @PostMapping("/add-page")
// //     public ResponseEntity<?> addPage(
// //             @RequestParam String sessionId,
// //             @RequestParam int position,
// //             @RequestParam(defaultValue = "BLANK") String pageType) {
// //         try {
// //             int newPageCount = pdfService.addPage(sessionId, position, pageType);
// //             return ResponseEntity.ok(Map.of(
// //                     "success", true,
// //                     "newPageCount", newPageCount,
// //                     "message", "Page added successfully"
// //             ));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Failed to add page: " + e.getMessage()));
// //         }
// //     }
// //     @PostMapping("/merge")
// //     public ResponseEntity<?> mergePdf(
// //             @RequestParam String sessionId,
// //             @RequestParam("file") MultipartFile mergeFile) {
// //         try {
// //             if (mergeFile.isEmpty() || !"application/pdf".equals(mergeFile.getContentType())) {
// //                 return ResponseEntity.badRequest()
// //                         .body(Map.of("error", "Please upload a valid PDF file to merge"));
// //             }
// //             int newPageCount = pdfService.mergePdf(sessionId, mergeFile);
// //             return ResponseEntity.ok(Map.of(
// //                     "success", true,
// //                     "newPageCount", newPageCount,
// //                     "message", "PDFs merged successfully"
// //             ));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Failed to merge PDFs: " + e.getMessage()));
// //         }
// //     }
// //     @PostMapping("/split")
// //     public ResponseEntity<?> splitPdf(
// //             @RequestParam String sessionId,
// //             @RequestParam int splitAtPage) {
// //         try {
// //             String newSessionId = pdfService.splitPdf(sessionId, splitAtPage);
// //             return ResponseEntity.ok(Map.of(
// //                     "success", true,
// //                     "newSessionId", newSessionId,
// //                     "message", "PDF split successfully"
// //             ));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Failed to split PDF: " + e.getMessage()));
// //         }
// //     }
// //     @GetMapping("/download")
// //     public ResponseEntity<byte[]> downloadPdf(@RequestParam String sessionId) {
// //         try {
// //             byte[] pdfData = pdfService.getPdfAsBytes(sessionId);
// //             String fileName = pdfService.getFileName(sessionId);
// //             return ResponseEntity.ok()
// //                     .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
// //                     .contentType(MediaType.APPLICATION_PDF)
// //                     .body(pdfData);
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError().build();
// //         } finally {
// //             pdfService.cleanupSession(sessionId);
// //         }
// //     }
// //     @GetMapping("/page-image/{pageNumber}")
// //     public ResponseEntity<byte[]> getPageAsImage(
// //             @PathVariable int pageNumber,
// //             @RequestParam String sessionId,
// //             @RequestParam(defaultValue = "150") int dpi) {
// //         try {
// //             byte[] imageData = pdfService.getPageAsImage(sessionId, pageNumber, dpi);
// //             return ResponseEntity.ok()
// //                     .contentType(MediaType.IMAGE_PNG)
// //                     .body(imageData);
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError().build();
// //         }
// //     }
// //     @PostMapping("/auto-save")
// //     public ResponseEntity<?> autoSave(@RequestParam String sessionId) {
// //         try {
// //             pdfService.autoSavePdf(sessionId);
// //             return ResponseEntity.ok(Map.of("success", true, "message", "Auto-saved"));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Auto-save failed: " + e.getMessage()));
// //         }
// //     }
// //     @GetMapping("/extract-text/{pageNumber}")
// //     public ResponseEntity<?> extractText(
// //             @PathVariable int pageNumber,
// //             @RequestParam String sessionId) {
// //         try {
// //             String text = pdfService.extractTextFromPage(sessionId, pageNumber);
// //             return ResponseEntity.ok(Map.of("text", text));
// //         } catch (Exception e) {
// //             return ResponseEntity.internalServerError()
// //                     .body(Map.of("error", "Failed to extract text: " + e.getMessage()));
// //         }
// //     }
// // }
// // // package com.textro.pdfcraft.controller;
// // // import com.textro.pdfcraft.model.TextEditRequest;
// // // import com.textro.pdfcraft.service.PdfService;
// // // import org.springframework.beans.factory.annotation.Autowired;
// // // import org.springframework.http.HttpHeaders;
// // // import org.springframework.http.MediaType;
// // // import org.springframework.http.ResponseEntity;
// // // import org.springframework.web.bind.annotation.*;
// // // import org.springframework.web.multipart.MultipartFile;
// // // import java.io.IOException;
// // // import java.util.Map;
// // // @RestController
// // // @RequestMapping("/api/pdf")
// // // @CrossOrigin(origins = "*")
// // // public class PdfController {
// // //     @Autowired
// // //     private PdfService pdfService;
// // //     @PostMapping("/upload")
// // //     public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
// // //         try {
// // //             if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
// // //                 return ResponseEntity.badRequest()
// // //                         .body(Map.of("error", "Please upload a valid PDF file"));
// // //             }
// // //             String sessionId = pdfService.storePdfInSession(file);
// // //             int pageCount = pdfService.getPageCount(sessionId);
// // //             return ResponseEntity.ok(Map.of(
// // //                     "sessionId", sessionId,
// // //                     "pageCount", pageCount,
// // //                     "fileName", file.getOriginalFilename(),
// // //                     "message", "PDF uploaded successfully"
// // //             ));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Failed to upload PDF: " + e.getMessage()));
// // //         }
// // //     }
// // //     @PostMapping("/add-text")
// // //     public ResponseEntity<?> addText(@RequestBody TextEditRequest request) {
// // //         try {
// // //             pdfService.addTextToPdf(
// // //                     request.getSessionId(),
// // //                     request.getPageNumber(),
// // //                     request.getText(),
// // //                     request.getX(),
// // //                     request.getY(),
// // //                     request.getFontSize(),
// // //                     request.getFontFamily(),
// // //                     request.getColor()
// // //             );
// // //             return ResponseEntity.ok(Map.of(
// // //                     "success", true,
// // //                     "message", "Text added successfully"
// // //             ));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Failed to add text: " + e.getMessage()));
// // //         }
// // //     }
// // //     @PutMapping("/edit-text")
// // //     public ResponseEntity<?> editText(@RequestBody TextEditRequest request) {
// // //         try {
// // //             pdfService.editTextInPdf(
// // //                     request.getSessionId(),
// // //                     request.getPageNumber(),
// // //                     request.getTextId(),
// // //                     request.getText(),
// // //                     request.getX(),
// // //                     request.getY(),
// // //                     request.getFontSize(),
// // //                     request.getFontFamily(),
// // //                     request.getColor()
// // //             );
// // //             return ResponseEntity.ok(Map.of(
// // //                     "success", true,
// // //                     "message", "Text updated successfully"
// // //             ));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Failed to edit text: " + e.getMessage()));
// // //         }
// // //     }
// // //     @DeleteMapping("/delete-text")
// // //     public ResponseEntity<?> deleteText(
// // //             @RequestParam String sessionId,
// // //             @RequestParam int pageNumber,
// // //             @RequestParam String textId) {
// // //         try {
// // //             pdfService.deleteTextFromPdf(sessionId, pageNumber, textId);
// // //             return ResponseEntity.ok(Map.of(
// // //                     "success", true,
// // //                     "message", "Text deleted successfully"
// // //             ));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Failed to delete text: " + e.getMessage()));
// // //         }
// // //     }
// // //     @DeleteMapping("/delete-page/{pageNumber}")
// // //     public ResponseEntity<?> deletePage(
// // //             @PathVariable int pageNumber,
// // //             @RequestParam String sessionId) {
// // //         try {
// // //             int remainingPages = pdfService.deletePage(sessionId, pageNumber);
// // //             return ResponseEntity.ok(Map.of(
// // //                     "success", true,
// // //                     "remainingPages", remainingPages,
// // //                     "message", "Page deleted successfully"
// // //             ));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Failed to delete page: " + e.getMessage()));
// // //         }
// // //     }
// // //     @PostMapping("/add-page")
// // //     public ResponseEntity<?> addPage(
// // //             @RequestParam String sessionId,
// // //             @RequestParam int position,
// // //             @RequestParam(defaultValue = "BLANK") String pageType) {
// // //         try {
// // //             int newPageCount = pdfService.addPage(sessionId, position, pageType);
// // //             return ResponseEntity.ok(Map.of(
// // //                     "success", true,
// // //                     "newPageCount", newPageCount,
// // //                     "message", "Page added successfully"
// // //             ));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Failed to add page: " + e.getMessage()));
// // //         }
// // //     }
// // //     @PostMapping("/merge")
// // //     public ResponseEntity<?> mergePdf(
// // //             @RequestParam String sessionId,
// // //             @RequestParam("file") MultipartFile mergeFile) {
// // //         try {
// // //             int newPageCount = pdfService.mergePdf(sessionId, mergeFile);
// // //             return ResponseEntity.ok(Map.of(
// // //                     "success", true,
// // //                     "newPageCount", newPageCount,
// // //                     "message", "PDFs merged successfully"
// // //             ));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Failed to merge PDFs: " + e.getMessage()));
// // //         }
// // //     }
// // //     @PostMapping("/split")
// // //     public ResponseEntity<?> splitPdf(
// // //             @RequestParam String sessionId,
// // //             @RequestParam int splitAtPage) {
// // //         try {
// // //             String newSessionId = pdfService.splitPdf(sessionId, splitAtPage);
// // //             return ResponseEntity.ok(Map.of(
// // //                     "success", true,
// // //                     "newSessionId", newSessionId,
// // //                     "message", "PDF split successfully"
// // //             ));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Failed to split PDF: " + e.getMessage()));
// // //         }
// // //     }
// // //     @GetMapping("/download")
// // //     public ResponseEntity<byte[]> downloadPdf(@RequestParam String sessionId) {
// // //         try {
// // //             byte[] pdfData = pdfService.getPdfAsBytes(sessionId);
// // //             String fileName = pdfService.getFileName(sessionId);
// // //             return ResponseEntity.ok()
// // //                     .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
// // //                     .contentType(MediaType.APPLICATION_PDF)
// // //                     .body(pdfData);
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError().build();
// // //         }
// // //     }
// // //     @GetMapping("/page-image/{pageNumber}")
// // //     public ResponseEntity<byte[]> getPageAsImage(
// // //             @PathVariable int pageNumber,
// // //             @RequestParam String sessionId,
// // //             @RequestParam(defaultValue = "150") int dpi) {
// // //         try {
// // //             byte[] imageData = pdfService.getPageAsImage(sessionId, pageNumber, dpi);
// // //             return ResponseEntity.ok()
// // //                     .contentType(MediaType.IMAGE_PNG)
// // //                     .body(imageData);
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError().build();
// // //         }
// // //     }
// // //     @PostMapping("/auto-save")
// // //     public ResponseEntity<?> autoSave(@RequestParam String sessionId) {
// // //         try {
// // //             pdfService.autoSavePdf(sessionId);
// // //             return ResponseEntity.ok(Map.of("success", true, "message", "Auto-saved"));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Auto-save failed: " + e.getMessage()));
// // //         }
// // //     }
// // //     @GetMapping("/extract-text/{pageNumber}")
// // //     public ResponseEntity<?> extractText(
// // //             @PathVariable int pageNumber,
// // //             @RequestParam String sessionId) {
// // //         try {
// // //             String text = pdfService.extractTextFromPage(sessionId, pageNumber);
// // //             return ResponseEntity.ok(Map.of("text", text));
// // //         } catch (Exception e) {
// // //             return ResponseEntity.internalServerError()
// // //                     .body(Map.of("error", "Failed to extract text: " + e.getMessage()));
// // //         }
// // //     }
// // // }
// // // // package com.textro.pdfcraft.controller;
// // // // import java.io.IOException;
// // // // import org.springframework.http.ResponseEntity;
// // // // import org.springframework.web.bind.annotation.PostMapping;
// // // // import org.springframework.web.bind.annotation.RequestMapping;
// // // // import org.springframework.web.bind.annotation.RequestParam;
// // // // import org.springframework.web.bind.annotation.RestController;
// // // // import org.springframework.web.multipart.MultipartFile;
// // // // import com.textro.pdfcraft.service.PdfService;
// // // // @RestController
// // // // @RequestMapping("/api/pdf")
// // // // public class PdfController {
// // // //     private final PdfService pdfService;
// // // //     public PdfController(PdfService pdfService) {
// // // //         this.pdfService = pdfService;
// // // //     }
// // // //     @PostMapping("/upload")
// // // //     public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
// // // //         try {
// // // //             // Validate file type
// // // //             if (!file.getContentType().equals("application/pdf")) {
// // // //                 return ResponseEntity.badRequest().body("Only PDF files are allowed");
// // // //             }
// // // //             // Process the PDF (you can save it or process it as needed)
// // // //             String textContent = pdfService.extractTextFromPdf(file);
// // // //             // In a real application, you might save the file and return a reference
// // // //             return ResponseEntity.ok("PDF uploaded successfully. Text length: " + textContent.length());
// // // //         } catch (IOException e) {
// // // //             return ResponseEntity.internalServerError().body("Error processing PDF: " + e.getMessage());
// // // //         }
// // // //     }
// // // // }
