package view;

import controller.EmployeeController;
import model.User;
import model.TimeRecord;
import model.Leave;
import util.PDFGenerator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

public class EmployeeFrame extends JFrame {
    private final User user;
    private JButton startDayBtn, startPauseBtn, endPauseBtn, endDayBtn;
    private JButton vacationBtn, sickBtn, reportBtn;
    private boolean isWorkDayStarted = false;
    private boolean isPauseActive = false;

    public EmployeeFrame(User user) {
        this.user = user;
        checkCurrentState();
        initializeUI();
    }

    private void checkCurrentState() {
        isWorkDayStarted = EmployeeController.hasActiveWorkDay(user.getId());
        if (isWorkDayStarted) {
            isPauseActive = EmployeeController.isPauseActive(user.getId());
        }
    }

    private void initializeUI() {
        setTitle("Панель сотрудника - " + user.getName());
        setSize(650, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Панель управления временем
        JPanel timePanel = new JPanel(new GridLayout(4, 1, 10, 10));
        startDayBtn = createButton("Начать рабочий день", e -> startWorkDay());
        startPauseBtn = createButton("Начать перерыв", e -> startPause());
        endPauseBtn = createButton("Завершить перерыв", e -> endPause());
        endDayBtn = createButton("Завершить рабочий день", e -> endWorkDay());
        updateButtonsState();

        timePanel.add(startDayBtn);
        timePanel.add(startPauseBtn);
        timePanel.add(endPauseBtn);
        timePanel.add(endDayBtn);

        // Панель отпусков/больничных и отчета
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        JPanel leavePanel = new JPanel(new GridLayout(2, 1, 10, 10));
        vacationBtn = createButton("Взять отпуск", e -> handleLeaveRequest("VACATION"));
        sickBtn = createButton("Взять больничный", e -> handleLeaveRequest("SICK"));
        leavePanel.add(vacationBtn);
        leavePanel.add(sickBtn);

        // Кнопка создания отчета
        JPanel reportPanel = new JPanel();
        reportBtn = createButton("Создать отчет", e -> generateReport());
        reportPanel.add(reportBtn);

        rightPanel.add(leavePanel, BorderLayout.CENTER);
        rightPanel.add(reportPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutBtn = createButton("Выйти", e -> logout());
        bottomPanel.add(logoutBtn);

        mainPanel.add(timePanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);


        add(mainPanel);
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        button.setFocusPainted(false);
        return button;
    }

    private void updateButtonsState() {
        startDayBtn.setEnabled(!isWorkDayStarted);
        startPauseBtn.setEnabled(isWorkDayStarted && !isPauseActive);
        endPauseBtn.setEnabled(isWorkDayStarted && isPauseActive);
        endDayBtn.setEnabled(isWorkDayStarted);
    }

    // Методы для управления рабочим временем
    private void startWorkDay() {
        if (EmployeeController.startWorkDay(user.getId())) {
            isWorkDayStarted = true;
            updateButtonsState();
            JOptionPane.showMessageDialog(this, "Рабочий день начат!");
        }
    }

    private void startPause() {
        if (EmployeeController.startPause(user.getId())) {
            isPauseActive = true;
            updateButtonsState();
            JOptionPane.showMessageDialog(this, "Перерыв начат!");
        }
    }

    private void endPause() {
        if (EmployeeController.endPause(user.getId())) {
            isPauseActive = false;
            updateButtonsState();
            JOptionPane.showMessageDialog(this, "Перерыв завершен!");
        }
    }

    private void endWorkDay() {
        if (EmployeeController.endWorkDay(user.getId())) {
            isWorkDayStarted = false;
            isPauseActive = false;
            updateButtonsState();
            JOptionPane.showMessageDialog(this, "Рабочий день завершен!");
        }
    }

    private void logout() {
        new LoginFrame().setVisible(true);
        dispose(); // Закрываем текущее окно
    }

    // Метод для обработки запросов на отпуск/больничный
    private void handleLeaveRequest(String type) {
        LeaveRequestDialog dialog = new LeaveRequestDialog(this, type);
        dialog.setVisible(true);

        if (dialog.isApproved()) {
            LocalDate start = dialog.getStartDate();
            LocalDate end = dialog.getEndDate();
            String comment = dialog.getComment();

            if (EmployeeController.requestLeave(user.getId(), type, start, end, comment)) {
                JOptionPane.showMessageDialog(this, "Заявка отправлена!");
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при отправке заявки!");
            }
        }
    }

    // Метод для генерации отчета
    private void generateReport() {
        List<TimeRecord> timeRecords = EmployeeController.getTimeRecords(user.getId());
        List<Leave> leaves = EmployeeController.getLeaves(user.getId());

        if (timeRecords.isEmpty() && leaves.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет данных для отчета!");
            return;
        }

        String filename = "report_" + user.getId() + ".pdf";
        PDFGenerator.generateReport(timeRecords, leaves, filename);
        JOptionPane.showMessageDialog(this,
                "Отчет сохранен в файл: " + filename,
                "Успешно",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new EmployeeFrame(new User(1, "Test User", "test", "EMPLOYEE")).setVisible(true)
        );
    }
}