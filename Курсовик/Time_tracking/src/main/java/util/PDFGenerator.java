package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import model.Leave;
import model.TimeRecord;
import model.User;
import java.io.FileOutputStream;
import java.util.*;
import java.util.List;

public class PDFGenerator {
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(30, 144, 255));
    private static final Font SUB_SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
    private static final Font TABLE_BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

    public static void generateReport(List<TimeRecord> timeRecords, List<Leave> leaves, String filename) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            Paragraph mainTitle = new Paragraph("Employee's Work Report Card", TITLE_FONT);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(20);
            document.add(mainTitle);

            if (!timeRecords.isEmpty()) {
                addTimeSection(document, timeRecords);
            }

            if (!leaves.isEmpty()) {
                addLeaveSection(document, leaves);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    public static void generateFullReport(
            HashMap<User, List<TimeRecord>> timeRecords,
            HashMap<User, List<Leave>> leaves,
            String filename
    ) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            Paragraph mainTitle = new Paragraph("Общий отчет по сотрудникам", TITLE_FONT);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(20);
            document.add(mainTitle);

            // Объединяем всех сотрудников из timeRecords и leaves
            Set<User> allUsers = new HashSet<>(timeRecords.keySet());
            allUsers.addAll(leaves.keySet());

            for (User user : allUsers) {
                List<TimeRecord> userTimeRecords = timeRecords.getOrDefault(user, new ArrayList<>());
                List<Leave> userLeaves = leaves.getOrDefault(user, new ArrayList<>());
                addUserSection(document, user, userTimeRecords, userLeaves);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    private static void addUserSection(Document doc, User user, List<TimeRecord> records, List<Leave> leaves)
            throws DocumentException {
        Paragraph userHeader = new Paragraph("Сотрудник: " + user.getName(), SUB_SECTION_FONT);
        userHeader.setSpacingBefore(15);
        doc.add(userHeader);

        // Добавляем рабочее время, если есть данные
        if (!records.isEmpty()) {
            addTimeSection(doc, records);
        }

        // Добавляем отпуска/больничные, если есть данные
        if (!leaves.isEmpty()) {
            addLeaveSection(doc, leaves);
        }

        doc.add(Chunk.NEWLINE);
    }

    private static void addTimeSection(Document doc, List<TimeRecord> records) throws DocumentException {
        Paragraph title = new Paragraph("Working Hours", SECTION_FONT);
        title.setSpacingAfter(15);
        doc.add(title);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{25f, 25f, 25f, 25f});
        table.setSpacingBefore(10);

        addTableHeader(table, "Date", "Start", "End", "Completed");
        for (TimeRecord record : records) {
            addTableCell(table, record.getStartTime().toLocalDate().toString());
            addTableCell(table, formatTime(record.getStartTime().toLocalTime()));
            String endTime = (record.getEndTime() != null) ? formatTime(record.getEndTime().toLocalTime()) : "Not Completed";
            addTableCell(table, endTime);
            addTableCell(table, formatDuration(record.getTotalSeconds()));
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private static void addLeaveSection(Document doc, List<Leave> leaves) throws DocumentException {
        Paragraph title = new Paragraph("Absences", SECTION_FONT);
        title.setSpacingBefore(20);
        title.setSpacingAfter(15);
        doc.add(title);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{20f, 20f, 20f, 40f});

        addTableHeader(table, "Type", "Start Date", "End Date", "Reason");
        for (Leave leave : leaves) {
            String type = leave.getType().equalsIgnoreCase("VACATION") ? "Vacation" : "Sick Leave";
            addTableCell(table, type);
            addTableCell(table, leave.getStartDate().toString());
            addTableCell(table, leave.getEndDate().toString());
            addTableCell(table, (leave.getComment() != null) ? leave.getComment() : "-");
        }

        doc.add(table);
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, TABLE_HEADER_FONT));
            cell.setBackgroundColor(new BaseColor(30, 144, 255));
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