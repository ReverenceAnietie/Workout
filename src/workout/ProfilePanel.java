package workout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ProfilePanel extends JPanel {
    private User currentUser;
    private JTextField nameField, emailField, ageField;
    private JComboBox<String> unitComboBox;
    private JLabel imageLabel;
    private String imagePath;

    public ProfilePanel(User user) {
        this.currentUser = user;
        setLayout(new GridBagLayout());
        UIUtils.stylePanel(this);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- Name ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Name:");
        UIUtils.styleLabel(nameLabel, false);
        add(nameLabel, gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        UIUtils.styleTextField(nameField);
        add(nameField, gbc);

        // --- Email ---
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel emailLabel = new JLabel("Email:");
        UIUtils.styleLabel(emailLabel, false);
        add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        UIUtils.styleTextField(emailField);
        add(emailField, gbc);

        // --- Age ---
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel ageLabel = new JLabel("Age:");
        UIUtils.styleLabel(ageLabel, false);
        add(ageLabel, gbc);

        gbc.gridx = 1;
        ageField = new JTextField(20);
        UIUtils.styleTextField(ageField);
        add(ageField, gbc);

        // --- Preferred Unit ---
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel unitLabel = new JLabel("Preferred Unit:");
        UIUtils.styleLabel(unitLabel, false);
        add(unitLabel, gbc);

        gbc.gridx = 1;
        unitComboBox = new JComboBox<>(new String[]{"KG", "LBS"});
        UIUtils.styleComboBox(unitComboBox);
        add(unitComboBox, gbc);

        // --- Profile Image ---
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel imageLabelLabel = new JLabel("Profile Image:");
        UIUtils.styleLabel(imageLabelLabel, false);
        add(imageLabelLabel, gbc);

        gbc.gridx = 1;
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(100, 100));
        add(imageLabel, gbc);

        // --- Upload Button ---
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton uploadButton = new UIUtils.StyledButton("Upload Image");
        uploadButton.addActionListener(e -> uploadImage());
        add(uploadButton, gbc);

        // --- Save Button ---
        gbc.gridy++;
        JButton saveButton = new UIUtils.StyledButton("Save Profile");
        saveButton.addActionListener(e -> saveProfile());
        add(saveButton, gbc);

        loadProfile();
    }

    public void loadProfile() {
        if (currentUser == null) return;
        nameField.setText(currentUser.getName());
        emailField.setText(currentUser.getEmail());
        ageField.setText(String.valueOf(currentUser.getAge()));
        unitComboBox.setSelectedItem(currentUser.getPreferredUnit());
        imagePath = currentUser.getProfileImagePath();

        if (imagePath != null && !imagePath.isEmpty()) {
            imageLabel.setIcon(new ImageIcon(new ImageIcon(imagePath).getImage()
                    .getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        }
    }

    private void uploadImage() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imagePath = chooser.getSelectedFile().getAbsolutePath();
            imageLabel.setIcon(new ImageIcon(new ImageIcon(imagePath).getImage()
                    .getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
        }
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid age");
            return;
        }
        String unit = (String) unitComboBox.getSelectedItem();

        String sql = "UPDATE users SET name=?, email=?, age=?, preferred_unit=?, profile_image_path=? WHERE id=?";
        try (PreparedStatement pstmt = Database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setInt(3, age);
            pstmt.setString(4, unit);
            pstmt.setString(5, imagePath);
            pstmt.setInt(6, currentUser.getId());
            pstmt.executeUpdate();

            // Update object
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setAge(age);
            currentUser.setPreferredUnit(unit);
            currentUser.setProfileImagePath(imagePath);

            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + e.getMessage());
        }
    }
}
