package com.unimas.library.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.unimas.library.entity.Book;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Generates PDF and Excel exports of the book catalogue. */
@Service
public class ExportService {

    private static final String[] HEADERS =
            {"#", "Title", "Author", "ISBN", "Category", "Year", "Available", "Total"};

    public byte[] booksPdf(List<Book> books, String libraryName) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 48, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(29, 78, 137));
            Font metaFont  = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
            Font headFont  = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Font cellFont  = new Font(Font.HELVETICA, 9);

            doc.add(new Paragraph(libraryName + " — Book Catalogue", titleFont));
            doc.add(new Paragraph("Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                    + " · " + books.size() + " titles", metaFont));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(new float[]{1, 6, 4, 3, 3, 1.5f, 2, 1.5f});
            table.setWidthPercentage(100);
            for (String h : HEADERS) {
                PdfPCell c = new PdfPCell(new Phrase(h, headFont));
                c.setBackgroundColor(new Color(29, 78, 137));
                c.setPadding(5);
                table.addCell(c);
            }
            int i = 1;
            for (Book b : books) {
                table.addCell(new Phrase(String.valueOf(i++), cellFont));
                table.addCell(new Phrase(b.getTitle(), cellFont));
                table.addCell(new Phrase(b.getAuthor(), cellFont));
                table.addCell(new Phrase(b.getIsbn(), cellFont));
                table.addCell(new Phrase(b.getCategory(), cellFont));
                table.addCell(new Phrase(b.getPublicationYear() == null ? "-" :
                        b.getPublicationYear().toString(), cellFont));
                table.addCell(new Phrase(String.valueOf(b.getAvailableCopies()), cellFont));
                table.addCell(new Phrase(String.valueOf(b.getTotalCopies()), cellFont));
            }
            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate the PDF export.", e);
        }
    }

    public byte[] booksCsv(List<Book> books) {
        StringBuilder sb = new StringBuilder();
        sb.append("#,Title,Author,ISBN,Category,Language,Edition,Shelf,Year,Available,Total\r\n");
        int i = 1;
        for (Book b : books) {
            sb.append(i++).append(',')
              .append(csv(b.getTitle())).append(',')
              .append(csv(b.getAuthor())).append(',')
              .append(csv(b.getIsbn())).append(',')
              .append(csv(b.getCategory())).append(',')
              .append(csv(b.getLanguage())).append(',')
              .append(csv(b.getEdition())).append(',')
              .append(csv(b.getShelfLocation())).append(',')
              .append(b.getPublicationYear() == null ? "" : b.getPublicationYear()).append(',')
              .append(b.getAvailableCopies()).append(',')
              .append(b.getTotalCopies()).append("\r\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String csv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        return (s.contains(",") || s.contains("\"") || s.contains("\n")) ? "\"" + s + "\"" : s;
    }

    public byte[] booksExcel(List<Book> books) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Books");

            CellStyle headStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font hf = wb.createFont();
            hf.setBold(true);
            hf.setColor(IndexedColors.WHITE.getIndex());
            headStyle.setFont(hf);
            headStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row head = sheet.createRow(0);
            for (int c = 0; c < HEADERS.length; c++) {
                Cell cell = head.createCell(c);
                cell.setCellValue(HEADERS[c]);
                cell.setCellStyle(headStyle);
            }
            int r = 1;
            for (Book b : books) {
                Row row = sheet.createRow(r);
                row.createCell(0).setCellValue(r);
                row.createCell(1).setCellValue(b.getTitle());
                row.createCell(2).setCellValue(b.getAuthor());
                row.createCell(3).setCellValue(b.getIsbn());
                row.createCell(4).setCellValue(b.getCategory());
                if (b.getPublicationYear() != null) row.createCell(5).setCellValue(b.getPublicationYear());
                row.createCell(6).setCellValue(b.getAvailableCopies());
                row.createCell(7).setCellValue(b.getTotalCopies());
                r++;
            }
            sheet.setColumnWidth(0, 3000);
            sheet.setColumnWidth(1, 9000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 3000);
            sheet.setColumnWidth(6, 3000);
            sheet.setColumnWidth(7, 3000);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate the Excel export.", e);
        }
    }
}
