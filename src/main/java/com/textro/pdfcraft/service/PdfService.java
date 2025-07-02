package com.textro.pdfcraft.service;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    public String extractTextFromPdf(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    public byte[] addTextToPdf(MultipartFile file, String text, float x, float y, int pageNumber) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = document.getPage(pageNumber - 1);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(text);
                contentStream.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    public List<byte[]> convertPdfToImages(MultipartFile file, int dpi) throws IOException {
        List<byte[]> images = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "PNG", baos);
                images.add(baos.toByteArray());
            }
        }

        return images;
    }

    public byte[] mergePdfs(List<MultipartFile> files) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            for (MultipartFile file : files) {
                try (InputStream inputStream = file.getInputStream()) {
                    PdfDocument srcDoc = new PdfDocument(new PdfReader(inputStream));

                    for (int i = 1; i <= srcDoc.getNumberOfPages(); i++) {
                        PdfPage page = srcDoc.getPage(i);
                        pdfDoc.addPage(page.copyTo(pdfDoc));
                    }

                    srcDoc.close();
                }
            }

            document.close();
            return outputStream.toByteArray();
        }
    }

    public List<byte[]> splitPdf(MultipartFile file) throws IOException {
        List<byte[]> pages = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            PdfDocument srcDoc = new PdfDocument(new PdfReader(inputStream));

            for (int i = 1; i <= srcDoc.getNumberOfPages(); i++) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(outputStream);
                PdfDocument pdfDoc = new PdfDocument(writer);

                PdfPage page = srcDoc.getPage(i);
                pdfDoc.addPage(page.copyTo(pdfDoc));

                pdfDoc.close();
                pages.add(outputStream.toByteArray());
            }

            srcDoc.close();
        }

        return pages;
    }

    public byte[] addWatermark(MultipartFile file, String watermarkText) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfReader reader = new PdfReader(inputStream);
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(reader, writer);
            Document document = new Document(pdfDoc);

            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                PdfPage page = pdfDoc.getPage(i);

                Paragraph watermark = new Paragraph(watermarkText)
                        .setFontSize(50)
                        .setOpacity(0.3f)
                        .setTextAlignment(TextAlignment.CENTER);

                document.showTextAligned(watermark,
                        page.getPageSize().getWidth() / 2,
                        page.getPageSize().getHeight() / 2,
                        i, TextAlignment.CENTER,
                        com.itextpdf.layout.properties.VerticalAlignment.MIDDLE, 45);
            }

            document.close();
            return outputStream.toByteArray();
        }
    }

    public byte[] rotatePdf(MultipartFile file, int degrees) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            for (PDPage page : document.getPages()) {
                page.setRotation(page.getRotation() + degrees);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] encryptPdf(MultipartFile file, String password) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfReader reader = new PdfReader(inputStream);
            PdfWriter writer = new PdfWriter(outputStream,
                    new WriterProperties().setStandardEncryption(
                            password.getBytes(),
                            password.getBytes(),
                            EncryptionConstants.ALLOW_PRINTING,
                            EncryptionConstants.ENCRYPTION_AES_128
                    )
            );

            PdfDocument pdfDoc = new PdfDocument(reader, writer);
            pdfDoc.close();

            return outputStream.toByteArray();
        }
    }

    public Map<String, Object> getPdfInfo(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            return Map.of(
                    "pages", document.getNumberOfPages(),
                    "title", document.getDocumentInformation().getTitle() != null ?
                            document.getDocumentInformation().getTitle() : "Untitled",
                    "author", document.getDocumentInformation().getAuthor() != null ?
                            document.getDocumentInformation().getAuthor() : "Unknown",
                    "subject", document.getDocumentInformation().getSubject() != null ?
                            document.getDocumentInformation().getSubject() : "",
                    "encrypted", document.isEncrypted()
            );
        }
    }
}

//package com.textro.pdfcraft.service;
//
//import com.itextpdf.kernel.pdf.*;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.element.Paragraph;
//import com.itextpdf.layout.element.Text;
//import com.itextpdf.layout.properties.TextAlignment;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.font.PDType1Font;
//import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
//import org.apache.pdfbox.rendering.ImageType;
//import org.apache.pdfbox.rendering.PDFRenderer;
//import org.apache.pdfbox.text.PDFTextStripper;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class PdfService {
//
//    /**
//     * Extract text from PDF using PDFBox
//     */
//    public String extractTextFromPdf(MultipartFile file) throws IOException {
//        try (InputStream inputStream = file.getInputStream();
//             PDDocument document = PDDocument.load(inputStream)) {
//
//            PDFTextStripper pdfStripper = new PDFTextStripper();
//            return pdfStripper.getText(document);
//        }
//    }
//
//    /**
//     * Add text to PDF at specified coordinates
//     */
//    public byte[] addTextToPdf(MultipartFile file, String text, float x, float y, int pageNumber) throws IOException {
//        try (InputStream inputStream = file.getInputStream();
//             PDDocument document = PDDocument.load(inputStream);
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//
//            PDPage page = document.getPage(pageNumber - 1); // Page numbers are 0-indexed
//
//            try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
//                    PDPageContentStream.AppendMode.APPEND, true, true)) {
//
//                contentStream.beginText();
//                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
//                contentStream.newLineAtOffset(x, y);
//                contentStream.showText(text);
//                contentStream.endText();
//            }
//
//            document.save(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//
//    /**
//     * Convert PDF pages to images
//     */
//    public List<byte[]> convertPdfToImages(MultipartFile file, int dpi) throws IOException {
//        List<byte[]> images = new ArrayList<>();
//
//        try (InputStream inputStream = file.getInputStream();
//             PDDocument document = PDDocument.load(inputStream)) {
//
//            PDFRenderer pdfRenderer = new PDFRenderer(document);
//
//            for (int page = 0; page < document.getNumberOfPages(); page++) {
//                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
//
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                ImageIO.write(bufferedImage, "PNG", baos);
//                images.add(baos.toByteArray());
//            }
//        }
//
//        return images;
//    }
//
//    /**
//     * Merge multiple PDFs into one
//     */
//    public byte[] mergePdfs(List<MultipartFile> files) throws IOException {
//        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            PdfWriter writer = new PdfWriter(outputStream);
//            PdfDocument pdfDoc = new PdfDocument(writer);
//            Document document = new Document(pdfDoc);
//
//            for (MultipartFile file : files) {
//                try (InputStream inputStream = file.getInputStream()) {
//                    PdfDocument srcDoc = new PdfDocument(new PdfReader(inputStream));
//
//                    for (int i = 1; i <= srcDoc.getNumberOfPages(); i++) {
//                        PdfPage page = srcDoc.getPage(i);
//                        pdfDoc.addPage(page.copyTo(pdfDoc));
//                    }
//
//                    srcDoc.close();
//                }
//            }
//
//            document.close();
//            return outputStream.toByteArray();
//        }
//    }
//
//    /**
//     * Split PDF into separate pages
//     */
//    public List<byte[]> splitPdf(MultipartFile file) throws IOException {
//        List<byte[]> pages = new ArrayList<>();
//
//        try (InputStream inputStream = file.getInputStream()) {
//            PdfDocument srcDoc = new PdfDocument(new PdfReader(inputStream));
//
//            for (int i = 1; i <= srcDoc.getNumberOfPages(); i++) {
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                PdfWriter writer = new PdfWriter(outputStream);
//                PdfDocument pdfDoc = new PdfDocument(writer);
//
//                PdfPage page = srcDoc.getPage(i);
//                pdfDoc.addPage(page.copyTo(pdfDoc));
//
//                pdfDoc.close();
//                pages.add(outputStream.toByteArray());
//            }
//
//            srcDoc.close();
//        }
//
//        return pages;
//    }
//
//    /**
//     * Add watermark to PDF
//     */
//    public byte[] addWatermark(MultipartFile file, String watermarkText) throws IOException {
//        try (InputStream inputStream = file.getInputStream();
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//
//            PdfReader reader = new PdfReader(inputStream);
//            PdfWriter writer = new PdfWriter(outputStream);
//            PdfDocument pdfDoc = new PdfDocument(reader, writer);
//            Document document = new Document(pdfDoc);
//
//            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
//                PdfPage page = pdfDoc.getPage(i);
//
//                Paragraph watermark = new Paragraph(watermarkText)
//                        .setFontSize(50)
//                        .setOpacity(0.3f)
//                        .setTextAlignment(TextAlignment.CENTER);
//
//                document.showTextAligned(watermark,
//                        page.getPageSize().getWidth() / 2,
//                        page.getPageSize().getHeight() / 2,
//                        i, TextAlignment.CENTER,
//                        com.itextpdf.layout.properties.VerticalAlignment.MIDDLE, 45);
//            }
//
//            document.close();
//            return outputStream.toByteArray();
//        }
//    }
//
//    /**
//     * Rotate PDF pages
//     */
//    public byte[] rotatePdf(MultipartFile file, int degrees) throws IOException {
//        try (InputStream inputStream = file.getInputStream();
//             PDDocument document = PDDocument.load(inputStream);
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//
//            for (PDPage page : document.getPages()) {
//                page.setRotation(page.getRotation() + degrees);
//            }
//
//            document.save(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//
//    /**
//     * Encrypt PDF with password
//     */
//    public byte[] encryptPdf(MultipartFile file, String password) throws IOException {
//        try (InputStream inputStream = file.getInputStream();
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//
//            PdfReader reader = new PdfReader(inputStream);
//            PdfWriter writer = new PdfWriter(outputStream,
//                    new WriterProperties().setStandardEncryption(
//                            password.getBytes(),
//                            password.getBytes(),
//                            EncryptionConstants.ALLOW_PRINTING,
//                            EncryptionConstants.ENCRYPTION_AES_128
//                    )
//            );
//
//            PdfDocument pdfDoc = new PdfDocument(reader, writer);
//            pdfDoc.close();
//
//            return outputStream.toByteArray();
//        }
//    }
//
//    /**
//     * Get PDF metadata and page count
//     */
//    public Map<String, Object> getPdfInfo(MultipartFile file) throws IOException {
//        try (InputStream inputStream = file.getInputStream();
//             PDDocument document = PDDocument.load(inputStream)) {
//
//            return Map.of(
//                    "pages", document.getNumberOfPages(),
//                    "title", document.getDocumentInformation().getTitle() != null ?
//                            document.getDocumentInformation().getTitle() : "Untitled",
//                    "author", document.getDocumentInformation().getAuthor() != null ?
//                            document.getDocumentInformation().getAuthor() : "Unknown",
//                    "subject", document.getDocumentInformation().getSubject() != null ?
//                            document.getDocumentInformation().getSubject() : "",
//                    "encrypted", document.isEncrypted()
//            );
//        }
//    }
//}