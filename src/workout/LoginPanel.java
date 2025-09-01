package workout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Workout mainFrame;

    public LoginPanel(Workout mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        UIUtils.stylePanel(this); // Apply modern panel styling
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Login");
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

        JButton loginButton = new UIUtils.StyledButton("Login");
        loginButton.addActionListener(new LoginAction());
        buttonPanel.add(loginButton);

        JButton signupButton = new UIUtils.StyledButton("Sign Up");
        signupButton.addActionListener(e -> mainFrame.showPanel("Signup"));
        buttonPanel.add(signupButton);

        add(buttonPanel, gbc);
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginPanel.this, "Please fill in all fields");
                return;
            }

            try (PreparedStatement pstmt = Database.getConnection().prepareStatement(
                    "SELECT * FROM users WHERE username ILIKE ? AND password ILIKE ?")) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            username,
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("age"),
                            rs.getString("profile_image_path"),
                            rs.getString("preferred_unit")
                    );
                    mainFrame.setCurrentUser(user);
                    // Refresh ProfilePanel and WorkoutPanel
                    for (Component comp : mainFrame.getContentPane().getComponents()) {
                        if (comp instanceof JPanel) {
                            for (Component innerComp : ((JPanel) comp).getComponents()) {
                                if (innerComp instanceof JTabbedPane) {
                                    for (Component tabComp : ((JTabbedPane) innerComp).getComponents()) {
                                        if (tabComp instanceof ProfilePanel) {
                                            ((ProfilePanel) tabComp).loadProfile();
                                        } else if (tabComp instanceof WorkoutPanel) {
                                            ((WorkoutPanel) tabComp).loadRoutines();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    mainFrame.showPanel("Home");
                } else {
                    JOptionPane.showMessageDialog(LoginPanel.this, "Invalid credentials");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(LoginPanel.this, "Error during login: " + ex.getMessage());
            }
        }
    }
}