package com.textro.pdfcraft.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.textro.pdfcraft.model.PdfEditRequest;
import com.textro.pdfcraft.model.PdfPageState;

@Service
public class PdfEditorService {

    // Session storage
    private final Map<String, PDDocument> documentSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionFileNames = new ConcurrentHashMap<>();
    private final Map<String, Map<String, TextAnnotation>> textAnnotations = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, PdfPageState>> pageStates = new ConcurrentHashMap<>();
    private final Map<String, Integer> currentPageMap = new ConcurrentHashMap<>();
    private final Map<String, Float> zoomLevelMap = new ConcurrentHashMap<>();
    private final Map<String, List<ExistingTextElement>> existingTextElements = new ConcurrentHashMap<>();

    // Text annotation class
    private static class TextAnnotation {

        String id;
        int pageNumber;
        String text;
        float x, y;
        float fontSize;
        String fontFamily;
        String color;
        boolean isDeleted = false;
        boolean isEditing = false;
        long lastModified = System.currentTimeMillis();

        TextAnnotation(String id, int pageNumber, String text, float x, float y,
                float fontSize, String fontFamily, String color) {
            this.id = id;
            this.pageNumber = pageNumber;
            this.text = text;
            this.x = x;
            this.y = y;
            this.fontSize = fontSize;
            this.fontFamily = fontFamily;
            this.color = color;
        }
    }

    // Existing text element class
    private static class ExistingTextElement {

        String id;
        int pageNumber;
        String text;
        float x, y;
        float width, height;
        float fontSize;
        boolean isSelected = false;

        ExistingTextElement(String id, int pageNumber, String text, float x, float y,
                float width, float height, float fontSize) {
            this.id = id;
            this.pageNumber = pageNumber;
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.fontSize = fontSize;
        }
    }

    /* ===================== */
 /* == Session Methods == */
 /* ===================== */
    public String processUpload(MultipartFile file) throws IOException {
        String sessionId = UUID.randomUUID().toString();

        PDDocument document;
        if (file != null && file.getSize() > 0) {
            document = Loader.loadPDF(file.getBytes());
            sessionFileNames.put(sessionId, file.getOriginalFilename());
        } else {
            // Create new blank document
            document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            sessionFileNames.put(sessionId, "new_document.pdf");
        }

        documentSessions.put(sessionId, document);
        textAnnotations.put(sessionId, new ConcurrentHashMap<>());
        currentPageMap.put(sessionId, 1);
        zoomLevelMap.put(sessionId, 1.0f);

        // Initialize page states
        Map<Integer, PdfPageState> states = new HashMap<>();
        for (int i = 1; i <= document.getNumberOfPages(); i++) {
            states.put(i, new PdfPageState(i));
        }
        pageStates.put(sessionId, states);

        // Extract existing text elements
        extractExistingTextElements(sessionId, document);

        return sessionId;
    }

    public PdfPageState getPageState(String sessionId, int pageNumber) {
        Map<Integer, PdfPageState> states = pageStates.get(sessionId);
        if (states == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }
        return states.get(pageNumber);
    }

    public String addTextToPdf(String sessionId, int pageNumber, String text,
            float x, float y, float fontSize,
            String fontFamily, String color) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        String annotationId = UUID.randomUUID().toString();
        TextAnnotation annotation = new TextAnnotation(
                annotationId, pageNumber, text, x, y, fontSize, fontFamily, color);
        textAnnotations.get(sessionId).put(annotationId, annotation);

        // Apply text annotation to document
        applyTextAnnotation(document, annotation);

        // Update page state
        PdfPageState state = pageStates.get(sessionId).get(pageNumber);
        if (state != null) {
            PdfEditRequest request = new PdfEditRequest();
            request.setSessionId(sessionId);
            request.setPageNumber(pageNumber);
            request.setText(text);
            request.setX(x);
            request.setY(y);
            request.setFontSize(fontSize);
            state.getEdits().add(request);
        }

        return annotationId;
    }

    public void applyTextEdit(PdfEditRequest request) throws IOException {
        String annotationId = addTextToPdf(
                request.getSessionId(),
                request.getPageNumber(),
                request.getText(),
                request.getX(),
                request.getY(),
                request.getFontSize(),
                "helvetica",
                "#000000"
        );

        // Store the annotation ID in the request for future reference
        request.setId(annotationId);
    }

    public void editTextInPdf(String sessionId, int pageNumber, String textId,
            String newText, float x, float y,
            float fontSize, String fontFamily, String color) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        Map<String, TextAnnotation> annotations = textAnnotations.get(sessionId);
        if (annotations == null) {
            throw new IllegalArgumentException("No annotations found for session");
        }

        TextAnnotation annotation = annotations.get(textId);
        if (annotation == null) {
            throw new IllegalArgumentException("Text annotation not found");
        }

        // Update the annotation
        annotation.text = newText;
        annotation.x = x;
        annotation.y = y;
        annotation.fontSize = fontSize;
        annotation.fontFamily = fontFamily;
        annotation.color = color;
        annotation.lastModified = System.currentTimeMillis();

        // Reapply all annotations for the page
        reapplyAnnotationsForPage(document, sessionId, pageNumber);

        // Update page state
        PdfPageState state = pageStates.get(sessionId).get(pageNumber);
        if (state != null) {
            // Find and update the corresponding edit request
            for (PdfEditRequest editRequest : state.getEdits()) {
                if (textId.equals(editRequest.getId())) {
                    editRequest.setText(newText);
                    editRequest.setX(x);
                    editRequest.setY(y);
                    editRequest.setFontSize(fontSize);
                    break;
                }
            }
        }
    }

    public void deleteTextFromPdf(String sessionId, int pageNumber, String textId) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        Map<String, TextAnnotation> annotations = textAnnotations.get(sessionId);
        if (annotations == null) {
            throw new IllegalArgumentException("No annotations found for session");
        }

        TextAnnotation annotation = annotations.get(textId);
        if (annotation == null) {
            throw new IllegalArgumentException("Text annotation not found");
        }

        // Mark as deleted
        annotation.isDeleted = true;
        annotation.lastModified = System.currentTimeMillis();

        // Reapply all annotations for the page
        reapplyAnnotationsForPage(document, sessionId, pageNumber);

        // Update page state
        PdfPageState state = pageStates.get(sessionId).get(pageNumber);
        if (state != null) {
            state.getEdits().removeIf(edit -> textId.equals(edit.getId()));
        }
    }

    public PdfPageState addPage(String sessionId, int position) throws IOException {
        return addPage(sessionId, position, "blank");
    }

    public PdfPageState addPage(String sessionId, int position, String pageType) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        PDPage newPage = new PDPage(PDRectangle.A4);
        int pageCount = document.getNumberOfPages();
        position = Math.min(position, pageCount);

        if (position >= pageCount) {
            document.addPage(newPage);
        } else {
            List<PDPage> pages = new ArrayList<>();
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                pages.add(document.getPage(i));
            }

            // Remove all pages
            while (document.getNumberOfPages() > 0) {
                document.removePage(0);
            }

            // Add pages back with new page inserted
            for (int i = 0; i < pages.size(); i++) {
                if (i == position) {
                    document.addPage(newPage);
                }
                document.addPage(pages.get(i));
            }

            if (position >= pages.size()) {
                document.addPage(newPage);
            }
        }

        // Update page states
        Map<Integer, PdfPageState> states = pageStates.get(sessionId);
        int newPageNumber = document.getNumberOfPages();
        PdfPageState newState = new PdfPageState(newPageNumber);
        states.put(newPageNumber, newState);

        return newState;
    }

    public PdfPageState deletePage(String sessionId, int pageNumber) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        if (pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
            throw new IllegalArgumentException("Invalid page number");
        }

        document.removePage(pageNumber - 1);

        // Update page states
        Map<Integer, PdfPageState> states = pageStates.get(sessionId);
        states.remove(pageNumber);

        // Return state of current page (either same number or previous if deleted was last)
        int currentPage = Math.min(pageNumber, document.getNumberOfPages());
        return states.get(currentPage);
    }

    public int mergePdf(String sessionId, MultipartFile mergeFile) throws IOException {
        PDDocument mainDocument = documentSessions.get(sessionId);
        if (mainDocument == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        try (PDDocument mergeDocument = Loader.loadPDF(mergeFile.getBytes())) {
            for (PDPage page : mergeDocument.getPages()) {
                mainDocument.addPage(page);
            }

            // Update page states
            Map<Integer, PdfPageState> states = pageStates.get(sessionId);
            for (int i = states.size() + 1; i <= mainDocument.getNumberOfPages(); i++) {
                states.put(i, new PdfPageState(i));
            }

            return mainDocument.getNumberOfPages();
        }
    }

    public String splitPdf(String sessionId, int splitAtPage) throws IOException {
        PDDocument originalDocument = documentSessions.get(sessionId);
        if (originalDocument == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        PDDocument newDocument = new PDDocument();
        String newSessionId = UUID.randomUUID().toString();

        // Copy pages from split point to new document
        for (int i = splitAtPage - 1; i < originalDocument.getNumberOfPages(); i++) {
            newDocument.addPage(originalDocument.getPage(i));
        }

        // Remove pages from original document
        for (int i = originalDocument.getNumberOfPages() - 1; i >= splitAtPage - 1; i--) {
            originalDocument.removePage(i);
        }

        // Store new session
        documentSessions.put(newSessionId, newDocument);
        sessionFileNames.put(newSessionId, "split_" + sessionFileNames.get(sessionId));
        textAnnotations.put(newSessionId, new ConcurrentHashMap<>());
        currentPageMap.put(newSessionId, 1);
        zoomLevelMap.put(newSessionId, 1.0f);
        existingTextElements.put(newSessionId, new ArrayList<>());

        // Initialize page states for new session
        Map<Integer, PdfPageState> newStates = new HashMap<>();
        for (int i = 1; i <= newDocument.getNumberOfPages(); i++) {
            newStates.put(i, new PdfPageState(i));
        }
        pageStates.put(newSessionId, newStates);

        // Update original session page states
        Map<Integer, PdfPageState> originalStates = pageStates.get(sessionId);
        for (int i = originalStates.size(); i >= splitAtPage; i--) {
            originalStates.remove(i);
        }

        return newSessionId;
    }

    public byte[] renderPageAsImage(String sessionId, int pageNumber) throws IOException {
        return renderPageAsImage(sessionId, pageNumber, 150);
    }

    public byte[] renderPageAsImage(String sessionId, int pageNumber, int dpi) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage image = renderer.renderImageWithDPI(pageNumber - 1, dpi);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        }
    }

    public String extractTextFromPage(String sessionId, int pageNumber) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(pageNumber);
        stripper.setEndPage(pageNumber);
        return stripper.getText(document);
    }

    /* ==================== */
 /* == Helper Methods == */
 /* ==================== */
    private void extractExistingTextElements(String sessionId, PDDocument document) throws IOException {
        final List<ExistingTextElement> elements = new ArrayList<>();

        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            final int currentPageIndex = pageIndex;
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    if (!textPositions.isEmpty()) {
                        TextPosition firstPos = textPositions.get(0);
                        TextPosition lastPos = textPositions.get(textPositions.size() - 1);

                        elements.add(new ExistingTextElement(
                                UUID.randomUUID().toString(),
                                currentPageIndex + 1,
                                text.trim(),
                                firstPos.getX(),
                                firstPos.getY(),
                                lastPos.getX() + lastPos.getWidth() - firstPos.getX(),
                                firstPos.getHeight(),
                                firstPos.getFontSizeInPt()
                        ));
                    }
                }
            };
            stripper.setStartPage(currentPageIndex + 1);
            stripper.setEndPage(currentPageIndex + 1);
            stripper.getText(document);
        }
        existingTextElements.put(sessionId, elements);
    }

    private void reapplyAnnotationsForPage(PDDocument document, String sessionId, int pageNumber) throws IOException {
        // In a real implementation, you would need to recreate the page content
        // For now, we'll just reapply active annotations
        Map<String, TextAnnotation> annotations = textAnnotations.get(sessionId);
        if (annotations != null) {
            for (TextAnnotation annotation : annotations.values()) {
                if (annotation.pageNumber == pageNumber && !annotation.isDeleted) {
                    applyTextAnnotation(document, annotation);
                }
            }
        }
    }

    private void applyTextAnnotation(PDDocument document, TextAnnotation annotation) throws IOException {
        if (annotation.isDeleted) {
            return;
        }

        PDPage page = document.getPage(annotation.pageNumber - 1);
        try (PDPageContentStream contentStream = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true)) {

            PDFont font = getFont(annotation.fontFamily);
            contentStream.setFont(font, annotation.fontSize);

            PDColor color = parseColor(annotation.color);
            contentStream.setNonStrokingColor(color);

            contentStream.beginText();
            contentStream.setTextMatrix(Matrix.getTranslateInstance(annotation.x, annotation.y));
            contentStream.showText(annotation.text);
            contentStream.endText();
        }
    }

    private PDFont getFont(String fontFamily) {
        return switch (fontFamily.toLowerCase()) {
            case "times", "times-roman" ->
                new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
            case "times-bold" ->
                new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
            case "times-italic" ->
                new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC);
            case "helvetica", "arial" ->
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            case "helvetica-bold", "arial-bold" ->
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            case "helvetica-italic", "arial-italic" ->
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
            case "courier" ->
                new PDType1Font(Standard14Fonts.FontName.COURIER);
            default ->
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        };
    }

    private PDColor parseColor(String colorStr) {
        try {
            Color color = colorStr.startsWith("#") ? Color.decode(colorStr) : getColorByName(colorStr);
            return new PDColor(
                    new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f},
                    PDDeviceRGB.INSTANCE
            );
        } catch (Exception e) {
            return new PDColor(new float[]{0, 0, 0}, PDDeviceRGB.INSTANCE);
        }
    }

    private Color getColorByName(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "black" ->
                Color.BLACK;
            case "white" ->
                Color.WHITE;
            case "red" ->
                Color.RED;
            case "green" ->
                Color.GREEN;
            case "blue" ->
                Color.BLUE;
            case "yellow" ->
                Color.YELLOW;
            case "cyan" ->
                Color.CYAN;
            case "magenta" ->
                Color.MAGENTA;
            default ->
                Color.BLACK;
        };
    }

    /* ==================== */
 /* == Utility Methods == */
 /* ==================== */
    public int getPageCount(String sessionId) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }
        return document.getNumberOfPages();
    }

    public String getFileName(String sessionId) {
        return sessionFileNames.getOrDefault(sessionId, "document.pdf");
    }

    public byte[] getPdfAsBytes(String sessionId) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.save(baos);
            return baos.toByteArray();
        }
    }

    public void cleanupSession(String sessionId) {
        try {
            PDDocument document = documentSessions.remove(sessionId);
            if (document != null) {
                document.close();
            }
            sessionFileNames.remove(sessionId);
            textAnnotations.remove(sessionId);
            pageStates.remove(sessionId);
            currentPageMap.remove(sessionId);
            zoomLevelMap.remove(sessionId);
            existingTextElements.remove(sessionId);
        } catch (IOException e) {
            System.err.println("Error cleaning up session: " + e.getMessage());
        }
    }

    public void autoSavePdf(String sessionId) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        // Auto-save to memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        // Document remains in memory for further editing
    }

    public Map<String, Object> getPdfInfo(String sessionId) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("pageCount", document.getNumberOfPages());
        info.put("fileName", getFileName(sessionId));
        info.put("title", document.getDocumentInformation().getTitle());
        info.put("author", document.getDocumentInformation().getAuthor());
        info.put("subject", document.getDocumentInformation().getSubject());
        info.put("encrypted", document.isEncrypted());
        info.put("currentPage", currentPageMap.getOrDefault(sessionId, 1));
        info.put("zoomLevel", zoomLevelMap.getOrDefault(sessionId, 1.0f));
        return info;
    }

    public List<ExistingTextElement> getExistingTextElements(String sessionId, int pageNumber) {
        List<ExistingTextElement> allElements = existingTextElements.get(sessionId);
        if (allElements == null) {
            return new ArrayList<>();
        }

        return allElements.stream()
                .filter(element -> element.pageNumber == pageNumber)
                .toList();
    }

    public void setCurrentPage(String sessionId, int pageNumber) {
        currentPageMap.put(sessionId, pageNumber);
    }

    public int getCurrentPage(String sessionId) {
        return currentPageMap.getOrDefault(sessionId, 1);
    }

    public void setZoomLevel(String sessionId, float zoomLevel) {
        zoomLevelMap.put(sessionId, zoomLevel);
    }

    public float getZoomLevel(String sessionId) {
        return zoomLevelMap.getOrDefault(sessionId, 1.0f);
    }

// Define the upload directory path (adjust as needed)
    private static final String UPLOAD_DIR = "uploads";

    public File getPdfFile(String sessionId) throws IOException {
        Path sessionPath = Paths.get(UPLOAD_DIR, sessionId);
        if (!Files.exists(sessionPath)) {
            throw new IOException("Session not found");
        }

        Path pdfPath = sessionPath.resolve("document.pdf");
        if (!Files.exists(pdfPath)) {
            throw new IOException("PDF not found in session");
        }

        return pdfPath.toFile();
    }
}

// package com.textro.pdfcraft.service;
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.UUID;
// import javax.imageio.ImageIO;
// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.apache.pdfbox.pdmodel.PDPage;
// import org.apache.pdfbox.pdmodel.PDPageContentStream;
// import org.apache.pdfbox.pdmodel.common.PDRectangle;
// import org.apache.pdfbox.pdmodel.font.PDFont;
// import org.apache.pdfbox.pdmodel.font.PDType1Font;
// import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
// import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
// import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
// import org.apache.pdfbox.rendering.PDFRenderer;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;
// import com.textro.pdfcraft.model.PdfEditRequest;
// import com.textro.pdfcraft.model.PdfPageState;
// @Service
// public class PdfEditorService {
//     private final Map<String, PDDocument> documentSessions = new HashMap<>();
//     private final Map<String, Map<Integer, PdfPageState>> pageStates = new HashMap<>();
//     public String processUpload(MultipartFile file) throws IOException {
//         String sessionId = UUID.randomUUID().toString();
//         // PDDocument document = PDDocument.load(file.getInputStream());
//         PDDocument document = new PDDocument();
//         PDPage page = new PDPage();
//         document.addPage(page);
//         documentSessions.put(sessionId, document);
//         // Initialize page states
//         Map<Integer, PdfPageState> states = new HashMap<>();
//         for (int i = 1; i <= document.getNumberOfPages(); i++) {
//             states.put(i, new PdfPageState(i));
//         }
//         pageStates.put(sessionId, states);
//         return sessionId;
//     }
//     public PdfPageState getPageState(String sessionId, int pageNumber) {
//         return pageStates.get(sessionId).get(pageNumber);
//     }
//     public void applyTextEdit(PdfEditRequest request) throws IOException {
//         PDDocument document = documentSessions.get(request.getSessionId());
//         PDPage page = document.getPage(request.getPageNumber() - 1);
//         try (PDPageContentStream contentStream = new PDPageContentStream(
//                 document, page, PDPageContentStream.AppendMode.APPEND, true)) {
//             PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
//             contentStream.setFont(font, request.getFontSize());
//             PDColor color = new PDColor(
//                     new float[]{0, 0, 0}, PDDeviceRGB.INSTANCE);
//             contentStream.setNonStrokingColor(color);
//             contentStream.beginText();
//             contentStream.newLineAtOffset(request.getX(), request.getY());
//             contentStream.showText(request.getText());
//             contentStream.endText();
//         }
//         // Update page state
//         PdfPageState state = pageStates.get(request.getSessionId())
//                 .get(request.getPageNumber());
//         state.getEdits().add(request);
//     }
//     public PdfPageState addPage(String sessionId, int position) throws IOException {
//         PDDocument document = documentSessions.get(sessionId);
//         PDPage newPage = new PDPage(PDRectangle.A4);
//         if (position < 0 || position >= document.getNumberOfPages()) {
//             document.addPage(newPage);
//         } else {
//             document.getPages().insertBefore(newPage, document.getPage(position));
//         }
//         // Update page states
//         Map<Integer, PdfPageState> states = pageStates.get(sessionId);
//         states.put(document.getNumberOfPages(), new PdfPageState(document.getNumberOfPages()));
//         return states.get(document.getNumberOfPages());
//     }
//     public PdfPageState deletePage(String sessionId, int pageNumber) throws IOException {
//         PDDocument document = documentSessions.get(sessionId);
//         if (pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
//             throw new IllegalArgumentException("Invalid page number");
//         }
//         document.removePage(pageNumber - 1);
//         // Update page states
//         Map<Integer, PdfPageState> states = pageStates.get(sessionId);
//         states.remove(pageNumber);
//         // Return state of current page (either same number or previous if deleted was last)
//         int currentPage = Math.min(pageNumber, document.getNumberOfPages());
//         return states.get(currentPage);
//     }
//     public byte[] renderPageAsImage(String sessionId, int pageNumber) throws IOException {
//         PDDocument document = documentSessions.get(sessionId);
//         PDFRenderer renderer = new PDFRenderer(document);
//         BufferedImage image = renderer.renderImage(pageNumber - 1);
//         ByteArrayOutputStream baos = new ByteArrayOutputStream();
//         ImageIO.write(image, "PNG", baos);
//         return baos.toByteArray();
//     }
// }
