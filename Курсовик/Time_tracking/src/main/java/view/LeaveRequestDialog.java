package view;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import java.util.Properties;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.time.ZoneId;
import javax.swing.JFormattedTextField;
import org.jdatepicker.impl.*;

public class LeaveRequestDialog extends JDialog {
    private JDatePickerImpl startDatePicker;
    private JDatePickerImpl endDatePicker;
    private JTextArea commentArea = new JTextArea(3, 20);
    private boolean approved = false;

    public LeaveRequestDialog(JFrame parent, String type) {
        super(parent, "Оформление " + type, true);
        setSize(400, 300);
        setLocationRelativeTo(parent);

        // Настройка JDatePicker
        Properties p = new Properties();
        p.put("text.today", "Сегодня");
        p.put("text.month", "Месяц");
        p.put("text.year", "Год");

        UtilDateModel startModel = new UtilDateModel();
        JDatePanelImpl startDatePanel = new JDatePanelImpl(startModel, p);
        startDatePicker = new JDatePickerImpl(startDatePanel, new DateLabelFormatter());

        UtilDateModel endModel = new UtilDateModel();
        JDatePanelImpl endDatePanel = new JDatePanelImpl(endModel, p);
        endDatePicker = new JDatePickerImpl(endDatePanel, new DateLabelFormatter());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel datePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        datePanel.add(new JLabel("Начало:"));
        datePanel.add(startDatePicker);
        datePanel.add(new JLabel("Окончание:"));
        datePanel.add(endDatePicker);

        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.add(new JLabel("Комментарий:"), BorderLayout.NORTH);
        commentPanel.add(new JScrollPane(commentArea), BorderLayout.CENTER);

        JButton submitBtn = new JButton("Отправить");
        submitBtn.addActionListener(e -> validateAndSubmit());

        mainPanel.add(datePanel, BorderLayout.NORTH);
        mainPanel.add(commentPanel, BorderLayout.CENTER);
        mainPanel.add(submitBtn, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void validateAndSubmit() {
        LocalDate start = getDateFromPicker(startDatePicker);
        LocalDate end = getDateFromPicker(endDatePicker);

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this, "Выберите обе даты!");
            return;
        }

        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this, "Дата окончания должна быть после даты начала!");
            return;
        }

        approved = true;
        dispose();
    }

    private LocalDate getDateFromPicker(JDatePickerImpl picker) {
        if (picker.getModel().getValue() == null) return null;
        java.util.Date date = (java.util.Date) picker.getModel().getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Добавьте этот внутренний класс для форматирования даты
    private static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final String datePattern = "yyyy-MM-dd";
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }

    // Getters остаются без изменений
    public boolean isApproved() { return approved; }
    public LocalDate getStartDate() { return getDateFromPicker(startDatePicker); }
    public LocalDate getEndDate() { return getDateFromPicker(endDatePicker); }
    public String getComment() { return commentArea.getText(); }
}