package com.textro.pdfcraft.service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

@Service
public class PdfService {

    // Session storage
    private final Map<String, PDDocument> documentSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionFileNames = new ConcurrentHashMap<>();
    private final Map<String, Map<String, TextAnnotation>> textAnnotations = new ConcurrentHashMap<>();
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
    public String storePdfInSession(MultipartFile file) throws IOException {
        String sessionId = UUID.randomUUID().toString();
        PDDocument document = Loader.loadPDF(file.getBytes());
        documentSessions.put(sessionId, document);
        sessionFileNames.put(sessionId, file.getOriginalFilename());
        textAnnotations.put(sessionId, new ConcurrentHashMap<>());
        currentPageMap.put(sessionId, 1);
        zoomLevelMap.put(sessionId, 1.0f);
        extractExistingTextElements(sessionId, document);
        return sessionId;
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
        applyTextAnnotation(document, annotation);
        return annotationId;
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
    }

    public int addPage(String sessionId, int position, String pageType) throws IOException {
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

        return document.getNumberOfPages();
    }

    public int deletePage(String sessionId, int pageNumber) throws IOException {
        PDDocument document = documentSessions.get(sessionId);
        if (document == null) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        if (pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
            throw new IllegalArgumentException("Invalid page number");
        }

        document.removePage(pageNumber - 1);
        return document.getNumberOfPages();
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

        return newSessionId;
    }

    public byte[] getPageAsImage(String sessionId, int pageNumber, int dpi) throws IOException {
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
 /* == File Methods == */
 /* ==================== */
    public Map<String, Object> getPdfInfo(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            Map<String, Object> info = new HashMap<>();
            info.put("pages", document.getNumberOfPages());
            info.put("title", document.getDocumentInformation().getTitle());
            info.put("author", document.getDocumentInformation().getAuthor());
            info.put("subject", document.getDocumentInformation().getSubject());
            info.put("encrypted", document.isEncrypted());
            return info;
        }
    }

    public byte[] addTextToPdf(MultipartFile file, String text, float x, float y, int page) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDPage pdPage = document.getPage(page - 1);
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, pdPage, PDPageContentStream.AppendMode.APPEND, true)) {
                PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                contentStream.setFont(font, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(text);
                contentStream.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    public List<byte[]> splitPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            List<byte[]> pages = new ArrayList<>();
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                try (PDDocument singlePageDoc = new PDDocument()) {
                    singlePageDoc.addPage(document.getPage(i));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    singlePageDoc.save(baos);
                    pages.add(baos.toByteArray());
                }
            }
            return pages;
        }
    }

    public byte[] addWatermark(MultipartFile file, String watermarkText) throws IOException {
        try (InputStream inputStream = file.getInputStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfReader reader = new PdfReader(inputStream);
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(reader, writer);
            Document document = new Document(pdfDoc);

            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                com.itextpdf.kernel.pdf.PdfPage page = pdfDoc.getPage(i);

                Paragraph watermark = new Paragraph(watermarkText)
                        .setFontSize(50)
                        .setOpacity(0.3f)
                        .setTextAlignment(TextAlignment.CENTER);

                document.showTextAligned(watermark,
                        page.getPageSize().getWidth() / 2,
                        page.getPageSize().getHeight() / 2,
                        i, TextAlignment.CENTER,
                        VerticalAlignment.MIDDLE, 45);
            }

            document.close();
            return outputStream.toByteArray();
        }
    }

    public byte[] rotatePdf(MultipartFile file, int degrees) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            for (PDPage page : document.getPages()) {
                page.setRotation(page.getRotation() + degrees);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    public byte[] encryptPdf(MultipartFile file, String password) throws IOException {
        try (InputStream inputStream = file.getInputStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfReader reader = new PdfReader(inputStream);
            PdfWriter writer = new PdfWriter(outputStream,
                    new WriterProperties().setStandardEncryption(
                            password.getBytes(),
                            password.getBytes(),
                            EncryptionConstants.ALLOW_PRINTING,
                            EncryptionConstants.ENCRYPTION_AES_128
                    ));

            PdfDocument pdfDoc = new PdfDocument(reader, writer);
            pdfDoc.close();

            return outputStream.toByteArray();
        }
    }

    public String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
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
}
