package workout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SignupPanel extends JPanel {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField;
    private Workout mainFrame;

    public SignupPanel(Workout mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        UIUtils.stylePanel(this); // Apply modern panel styling
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Sign Up");
        UIUtils.styleLabel(titleLabel, true); // Apply title font
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Username:");
        UIUtils.styleLabel(usernameLabel, false);
        add(usernameLabel, gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        UIUtils.styleTextField(usernameField);
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel emailLabel = new JLabel("Email:");
        UIUtils.styleLabel(emailLabel, false);
        add(emailLabel, gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        UIUtils.styleTextField(emailField);
        add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password:");
        UIUtils.styleLabel(passwordLabel, false);
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        UIUtils.styleTextField(passwordField);
        add(passwordField, gbc);

        // Button panel for side-by-side buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(UIUtils.BACKGROUND);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        JButton signupButton = new UIUtils.StyledButton("Sign Up");
        signupButton.addActionListener(new SignupAction());
        buttonPanel.add(signupButton);

        JButton loginButton = new UIUtils.StyledButton("Back to Login");
        loginButton.addActionListener(e -> mainFrame.showPanel("Login"));
        buttonPanel.add(loginButton);

        add(buttonPanel, gbc);
    }

    private class SignupAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(SignupPanel.this, "Please fill in all fields");
                return;
            }

            try (PreparedStatement pstmt = Database.getConnection().prepareStatement(
                    "INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, email);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(SignupPanel.this, "Signup successful! Please login.");
                mainFrame.showPanel("Login");
            } catch (SQLException ex) {
                if (ex.getMessage().contains("unique constraint")) {
                    JOptionPane.showMessageDialog(SignupPanel.this, "Username already exists");
                } else {
                    JOptionPane.showMessageDialog(SignupPanel.this, "Error during signup: " + ex.getMessage());
                }
            }
        }
    }
}