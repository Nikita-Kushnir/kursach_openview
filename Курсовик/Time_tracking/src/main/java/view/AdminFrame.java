package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import controller.AdminController;
import model.Leave;
import model.TimeRecord;
import model.User;
import util.PDFGenerator;
import java.util.List;
import java.util.HashMap;

public class AdminFrame extends JFrame {
    private JTable employeeTable = new JTable();
    private JTable leavesTable = new JTable();
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JRadioButton userReportRadio = new JRadioButton("Отчет по сотруднику");
    private JRadioButton allReportRadio = new JRadioButton("Общий отчет");
    private ButtonGroup reportGroup = new ButtonGroup();

    public AdminFrame() {
        setTitle("Админская панель");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Инициализация радио-кнопок для выбора типа отчета
        reportGroup.add(userReportRadio);
        reportGroup.add(allReportRadio);
        userReportRadio.setSelected(true);

        // Вкладка "Сотрудники"
        JPanel employeePanel = new JPanel(new BorderLayout());
        refreshEmployeeTable();
        employeePanel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        // Вкладка "Заявки"
        JPanel leavesPanel = new JPanel(new BorderLayout());
        refreshLeavesTable();

        // Панель с кнопками для управления заявками
        JPanel leavesButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approveBtn = new JButton("Одобрить");
        JButton rejectBtn = new JButton("Отклонить");

        // Обработчики кнопок
        approveBtn.addActionListener(e -> processLeaveRequest("APPROVED"));
        rejectBtn.addActionListener(e -> processLeaveRequest("REJECTED"));

        leavesButtonPanel.add(approveBtn);
        leavesButtonPanel.add(rejectBtn);

        leavesPanel.add(new JScrollPane(leavesTable), BorderLayout.CENTER);
        leavesPanel.add(leavesButtonPanel, BorderLayout.SOUTH);

        // Добавление вкладок
        tabbedPane.addTab("Сотрудники", employeePanel);
        tabbedPane.addTab("Заявки", leavesPanel);

        // Панель управления отчетами
        JPanel reportControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportControlPanel.add(new JLabel("Тип отчета:"));
        reportControlPanel.add(userReportRadio);
        reportControlPanel.add(allReportRadio);

        // Кнопки общего назначения
        JButton generateBtn = new JButton("Создать отчет");
        generateBtn.addActionListener(e -> generateReport());
        JButton logoutBtn = new JButton("Выйти");
        logoutBtn.addActionListener(e -> logout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(reportControlPanel);
        buttonPanel.add(generateBtn);
        buttonPanel.add(logoutBtn);

        // Добавление компонентов на форму
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Обновление таблицы сотрудников
    private void refreshEmployeeTable() {
        try {
            ResultSet rs = AdminController.getAllEmployees();
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Имя пользователя", "Логин"}, 0
            );

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("login")
                });
            }

            employeeTable.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки сотрудников!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Обновление таблицы заявок
    private void refreshLeavesTable() {
        try {
            ResultSet rs = AdminController.getAllLeaveRequests();
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Сотрудник", "Тип", "Начало", "Конец", "Статус", "Комментарий"}, 0
            );

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("user_name"),
                        rs.getString("type"),
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        rs.getString("status"),
                        rs.getString("comment")
                });
            }

            leavesTable.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки заявок!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Обработка изменения статуса заявки
    private void processLeaveRequest(String action) {
        int selectedRow = leavesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите заявку из таблицы!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int leaveId = (int) leavesTable.getValueAt(selectedRow, 0);
        if (AdminController.updateLeaveStatus(leaveId, action)) {
            refreshLeavesTable(); // Обновляем таблицу
            JOptionPane.showMessageDialog(
                    this,
                    "Статус заявки #" + leaveId + " изменен на: " + action,
                    "Успешно",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(this, "Ошибка изменения статуса!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Генерация отчетов
    private void generateReport() {
        if (userReportRadio.isSelected()) {
            generateUserReport();
        } else {
            generateAllEmployeesReport();
        }
    }

    // Отчет по выбранному сотруднику
    private void generateUserReport() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите сотрудника из таблицы!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = (int) employeeTable.getValueAt(selectedRow, 0);
        String userName = (String) employeeTable.getValueAt(selectedRow, 1);

        List<TimeRecord> timeRecords = AdminController.getEmployeeTimeRecords(userId);
        List<Leave> leaves = AdminController.getEmployeeLeaves(userId);

        if (timeRecords.isEmpty() && leaves.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет данных для сотрудника: " + userName, "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String filename = userName + ".pdf";
        PDFGenerator.generateReport(timeRecords, leaves, filename);
        showSuccessMessage(filename);
    }

    // Общий отчет по всем сотрудникам
    private void generateAllEmployeesReport() {
        HashMap<User, List<TimeRecord>> allTimeRecords = AdminController.getAllTimeRecords();
        HashMap<User, List<Leave>> allLeaves = AdminController.getAllLeaves();

        if (allTimeRecords.isEmpty() && allLeaves.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет данных по сотрудникам!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String filename = "Report all employees.pdf";
        PDFGenerator.generateFullReport(allTimeRecords, allLeaves, filename);
        showSuccessMessage(filename);
    }

    // Отображение сообщения об успешной генерации
    private void showSuccessMessage(String filename) {
        JOptionPane.showMessageDialog(
                this,
                "Отчет сохранен в файл:\n" + filename,
                "Успешно",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Выход из системы
    private void logout() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}