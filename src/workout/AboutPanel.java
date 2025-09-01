package workout;

import javax.swing.*;
import java.awt.*;

public class AboutPanel extends JPanel {
    private JLabel imageLabel;
    private User currentUser;

    public AboutPanel(User user) {
        this.currentUser = user;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(this); // Apply modern panel styling

        // Title
        JLabel titleLabel = new JLabel("Gym Workout Tracker");
        UIUtils.styleLabel(titleLabel, true);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        add(Box.createVerticalStrut(10)); // Add spacing below title

        // Image section
        JPanel imagePanel = new JPanel();
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(150, 150)); // Increased size for prominence
        if (currentUser != null && currentUser.getProfileImagePath() != null && !currentUser.getProfileImagePath().isEmpty()) {
            imageLabel.setIcon(new ImageIcon(new ImageIcon(currentUser.getProfileImagePath()).getImage()
                    .getScaledInstance(150, 150, Image.SCALE_SMOOTH)));
        }
        imagePanel.add(imageLabel);
        add(imagePanel);
        add(Box.createVerticalStrut(10)); // Add spacing below image

        // Description section
        JTextArea descriptionText = new JTextArea();
        descriptionText.setText("Your_Name\n" +
                "Your_Reg_Number\n\n" +
                "About_You");
        descriptionText.setEditable(false);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setLineWrap(true);
        UIUtils.styleTextArea(descriptionText);
        descriptionText.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(new JScrollPane(descriptionText));
    }
}