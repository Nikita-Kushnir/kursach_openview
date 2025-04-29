package view;

import controller.AuthController;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private final JTextField loginField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);

    public LoginFrame() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Вход в систему");
        setSize(300, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Заголовок
        JLabel titleLabel = new JLabel("Вход в систему", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Поле логина
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Логин:"), gbc);

        gbc.gridx = 1;
        loginField.setPreferredSize(new Dimension(150, 25));
        mainPanel.add(loginField, gbc);

        // Поле пароля
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Пароль:"), gbc);

        gbc.gridx = 1;
        passwordField.setPreferredSize(new Dimension(150, 25));
        mainPanel.add(passwordField, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton loginBtn = new JButton("Войти");
        JButton registerBtn = new JButton("Регистрация");

        loginBtn.addActionListener(this::performLogin);
        registerBtn.addActionListener(e -> openRegistration());

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
    }

    private void performLogin(ActionEvent e) {
        String login = loginField.getText();
        String password = new String(passwordField.getPassword());

        User user = AuthController.login(login, password);

        if (user != null) {
            switch (user.getRole()) {
                case "ADMIN":
                    new AdminFrame().setVisible(true);
                    break;
                case "EMPLOYEE":
                    new EmployeeFrame(user).setVisible(true);
                    break;
            }
            dispose(); // Закрыть окно входа
        } else {
            JOptionPane.showMessageDialog(this,
                    "Неверный логин или пароль",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void openRegistration() {
        new RegisterFrame().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}