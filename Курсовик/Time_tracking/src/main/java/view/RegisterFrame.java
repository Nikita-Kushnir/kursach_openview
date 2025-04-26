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
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Login:"));
        inputPanel.add(loginField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(passwordField);

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(this::performRegistration);

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(registerBtn, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void performRegistration(ActionEvent e) {
        boolean success = AuthController.register(
                nameField.getText(),
                loginField.getText(),
                new String(passwordField.getPassword())
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
            new LoginFrame().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed!");
        }
    }
}