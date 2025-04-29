package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import controller.AuthController;

public class RegisterFrame extends JFrame {
    private JTextField nameField = new JTextField(20);
    private JTextField loginField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);

    public RegisterFrame() {
        setTitle("Register");
        setSize(350, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Имя пользователя:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Логин:"));
        inputPanel.add(loginField);
        inputPanel.add(new JLabel("Пароль:"));
        inputPanel.add(passwordField);

        JButton registerBtn = new JButton("Зарегистрироваться");
        registerBtn.addActionListener(this::performRegistration);

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(registerBtn, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void performRegistration(ActionEvent e) {
        // Получаем значения из полей и обрезаем пробелы
        String name = nameField.getText().trim();
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Проверяем, есть ли пустые поля
        if (name.isEmpty() || login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Все поля должны быть заполнены!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String passwordError = validatePassword(password);
        if (passwordError != null) {
            showError(passwordError);
            return;
        }
        // Если все поля заполнены, выполняем регистрацию
        boolean success = AuthController.register(name, login, password);

        if (success) {
            JOptionPane.showMessageDialog(this, "Регистрация прошла успешно!");
            new LoginFrame().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Ошибка регистрации. Возможно, логин уже занят.",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String validatePassword(String password) {
        if (password.length() < 8) {
            return "Пароль должен содержать минимум 8 символов!";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Пароль должен содержать хотя бы одну заглавную букву!";
        }

        if (!password.matches(".*\\d.*")) {
            return "Пароль должен содержать хотя бы одну цифру!";
        }

        if (!password.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]+$")) {
            return "Пароль может содержать только латинские буквы, цифры и специальные символы!";
        }

        if (password.matches(".*[а-яА-Я].*")) {
            return "Пароль не должен содержать русские буквы!";
        }

        return null; // Валидация пройдена
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
        );
    }
}