package com.textro.pdfcraft.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class ConversionService {

    public byte[] convertWordToPdf(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document pdfDocument = new Document(pdfDoc);

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    pdfDocument.add(new Paragraph(text));
                }
            }

            pdfDocument.close();
            return outputStream.toByteArray();
        }
    }

    public byte[] convertExcelToPdf(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document pdfDocument = new Document(pdfDoc);

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                Paragraph sheetHeader = new Paragraph("Sheet: " + sheet.getSheetName());
                sheetHeader.setFontSize(14);
                pdfDocument.add(sheetHeader);

                int maxColumns = 0;
                for (Row row : sheet) {
                    if (row.getLastCellNum() > maxColumns) {
                        maxColumns = row.getLastCellNum();
                    }
                }

                if (maxColumns > 0) {
                    Table table = new Table(maxColumns);

                    for (Row row : sheet) {
                        for (int cellIndex = 0; cellIndex < maxColumns; cellIndex++) {
                            org.apache.poi.ss.usermodel.Cell poiCell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            String cellValue = getCellValueAsString(poiCell);
                            table.addCell(new Cell().add(new Paragraph(cellValue)));
                        }
                    }

                    pdfDocument.add(table);
                }

                if (sheetIndex < workbook.getNumberOfSheets() - 1) {
                    pdfDocument.add(new com.itextpdf.layout.element.AreaBreak());
                }
            }

            pdfDocument.close();
            return outputStream.toByteArray();
        }
    }

    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    public byte[] convertPowerPointToPdf(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             XMLSlideShow ppt = new XMLSlideShow(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document pdfDocument = new Document(pdfDoc);

            int slideNumber = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                Paragraph slideTitle = new Paragraph("Slide " + slideNumber++);
                slideTitle.setFontSize(16);
                pdfDocument.add(slideTitle);

                for (XSLFTextShape shape : slide.getPlaceholders()) {
                    for (XSLFTextParagraph paragraph : shape.getTextParagraphs()) {
                        String text = paragraph.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            pdfDocument.add(new Paragraph(text));
                        }
                    }
                }

                pdfDocument.add(new com.itextpdf.layout.element.AreaBreak());
            }

            pdfDocument.close();
            return outputStream.toByteArray();
        }
    }

    public byte[] convertImageToPdf(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            BufferedImage image = ImageIO.read(inputStream);

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document pdfDocument = new Document(pdfDoc);

            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", imageBytes);

            com.itextpdf.layout.element.Image pdfImage =
                    new com.itextpdf.layout.element.Image(
                            com.itextpdf.io.image.ImageDataFactory.create(imageBytes.toByteArray())
                    );

            pdfImage.scaleToFit(pdfDoc.getDefaultPageSize().getWidth() - 72,
                    pdfDoc.getDefaultPageSize().getHeight() - 72);

            pdfDocument.add(pdfImage);
            pdfDocument.close();

            return outputStream.toByteArray();
        }
    }

    public byte[] convertImagesToPdf(MultipartFile[] files) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document pdfDocument = new Document(pdfDoc);

            for (MultipartFile file : files) {
                try (InputStream inputStream = file.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);

                    ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
                    ImageIO.write(image, "PNG", imageBytes);

                    com.itextpdf.layout.element.Image pdfImage =
                            new com.itextpdf.layout.element.Image(
                                    com.itextpdf.io.image.ImageDataFactory.create(imageBytes.toByteArray())
                            );

                    pdfImage.scaleToFit(pdfDoc.getDefaultPageSize().getWidth() - 72,
                            pdfDoc.getDefaultPageSize().getHeight() - 72);

                    pdfDocument.add(pdfImage);
                    pdfDocument.add(new com.itextpdf.layout.element.AreaBreak());
                }
            }

            pdfDocument.close();
            return outputStream.toByteArray();
        }
    }

    public boolean isSupportedFileType(String filename) {
        String extension = filename.toLowerCase().substring(filename.lastIndexOf('.') + 1);
        return switch (extension) {
            case "pdf", "docx", "doc", "xlsx", "xls", "pptx", "ppt",
                 "png", "jpg", "jpeg", "gif", "bmp", "tiff", "webp" -> true;
            default -> false;
        };
    }

    public String getFileType(String filename) {
        return filename.toLowerCase().substring(filename.lastIndexOf('.') + 1);
    }
}