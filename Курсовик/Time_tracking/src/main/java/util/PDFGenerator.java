package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import model.Leave;
import model.TimeRecord;
import java.io.FileOutputStream;
import java.util.List;

public class PDFGenerator {
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(30, 144, 255));
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font TABLE_BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

    public static void generateReport(List<TimeRecord> timeRecords, List<Leave> leaves, String filename) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Главный заголовок
            Paragraph mainTitle = new Paragraph("Employee's Work Report Card", TITLE_FONT);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(20);
            document.add(mainTitle);

            // Секция рабочего времени
            addTimeSection(document, timeRecords);

            // Секция отсутствий (если есть данные)
            if (!leaves.isEmpty()) {
                addLeaveSection(document, leaves);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    private static void addTimeSection(Document doc, List<TimeRecord> records) throws DocumentException {
        if (records.isEmpty()) return;

        // Заголовок секции
        Paragraph title = new Paragraph("Working Hours", SECTION_FONT);
        title.setSpacingAfter(15);
        doc.add(title);

        // Таблица
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{25f, 25f, 25f, 25f});
        table.setSpacingBefore(10);

        // Заголовки таблицы
        addTableHeader(table, "Date", "Start", "End", "Completed");

        // Данные
        for (TimeRecord record : records) {
            addTableCell(table, record.getStartTime().toLocalDate().toString());
            addTableCell(table, formatTime(record.getStartTime().toLocalTime()));

            String endTime = record.getEndTime() != null
                    ? formatTime(record.getEndTime().toLocalTime())
                    : "Not Completed";

            addTableCell(table, endTime);
            addTableCell(table, formatDuration(record.getTotalSeconds()));
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private static void addLeaveSection(Document doc, List<Leave> leaves) throws DocumentException {
        // Заголовок секции
        Paragraph title = new Paragraph("Absences", SECTION_FONT);
        title.setSpacingBefore(20);
        title.setSpacingAfter(15);
        doc.add(title);

        // Таблица
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{20f, 20f, 20f, 40f});

        // Заголовки таблицы
        addTableHeader(table, "Type", "Start Date", "End Date", "Reason");

        // Данные
        for (Leave leave : leaves) {
            String type = leave.getType().equalsIgnoreCase("VACATION")
                    ? "Vacation"
                    : "Sick Leave";

            addTableCell(table, type);
            addTableCell(table, leave.getStartDate().toString());
            addTableCell(table, leave.getEndDate().toString());
            addTableCell(table, leave.getComment() != null ? leave.getComment() : "-");
        }

        doc.add(table);
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, TABLE_HEADER_FONT));
            cell.setBackgroundColor(new BaseColor(30, 144, 255)); // Синий цвет
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private static void addTableCell(PdfPTable table, String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content, TABLE_BODY_FONT));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static String formatDuration(int totalSeconds) {
        if (totalSeconds <= 0) return "0:00";
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        return String.format("%d:%02d", hours, minutes);
    }

    private static String formatTime(java.time.LocalTime time) {
        return time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}