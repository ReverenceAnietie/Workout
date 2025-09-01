package workout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class UIUtils {
    public static final Color PURPLE = new Color(0x6A1B9A); // Primary purple color
    public static final Color PURPLE_HOVER = new Color(0x8E24AA); // Lighter purple for hover
    public static final Color BACKGROUND = new Color(0xF5F5F5); // Light gray background
    public static final Color TEXT_COLOR = new Color(0x333333); // Dark text for contrast
    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14); // Modern font
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 16);

    public static void stylePanel(JPanel panel) {
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public static void styleButton(JButton button) {
        button.setBackground(PURPLE);
        button.setForeground(Color.WHITE);
        button.setFont(FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PURPLE_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PURPLE);
            }
        });
    }

    public static void styleTextField(JTextField textField) {
        textField.setFont(FONT);
        textField.setForeground(TEXT_COLOR);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCCCCCC)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(FONT);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(0xCCCCCC)));
    }

    public static void styleLabel(JLabel label, boolean isTitle) {
        label.setFont(isTitle ? TITLE_FONT : LABEL_FONT);
        label.setForeground(TEXT_COLOR);
    }

    public static void styleTextArea(JTextArea textArea) {
        textArea.setFont(FONT);
        textArea.setForeground(TEXT_COLOR);
        textArea.setBackground(Color.WHITE);
        textArea.setBorder(BorderFactory.createLineBorder(new Color(0xCCCCCC)));
    }

    public static void styleTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setFont(FONT);
        tabbedPane.setBackground(BACKGROUND);
        tabbedPane.setForeground(TEXT_COLOR);
        UIManager.put("TabbedPane.selected", PURPLE);
        UIManager.put("TabbedPane.focus", PURPLE_HOVER);
    }

    public static class StyledButton extends JButton {
        public StyledButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(120, 40)); // Uniform button size
            styleButton(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            // No border for a cleaner look
        }
    }
}