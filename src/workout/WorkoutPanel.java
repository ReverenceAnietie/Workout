package workout;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkoutPanel extends JPanel {
    private User currentUser;
    private JComboBox<String> routineComboBox;
    private JButton loadButton;
    private JTextArea displayArea;
    private JButton saveWorkoutButton;

    public WorkoutPanel(User user) {
        this.currentUser = user;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(this);
        initUI();
    }

    public WorkoutPanel(int userId) {
        this.currentUser = null; // Deprecated constructor
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(this);
        initUI();
    }

    WorkoutPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initUI() {
        // Title
        JLabel titleLabel = new JLabel("Log Workout");
        UIUtils.styleLabel(titleLabel, true);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);
        add(Box.createVerticalStrut(10));

        // Load routine section
        JPanel loadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        UIUtils.stylePanel(loadPanel);
        routineComboBox = new JComboBox<>();
        UIUtils.styleComboBox(routineComboBox);
        loadPanel.add(routineComboBox);

        loadButton = new UIUtils.StyledButton("Load Routine");
        loadPanel.add(loadButton);

        loadPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, loadPanel.getPreferredSize().height));
        add(loadPanel);
        add(Box.createVerticalStrut(10));

        // Create workout section
        JPanel createPanel = new JPanel();
        createPanel.setLayout(new BoxLayout(createPanel, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(createPanel);

        // Placeholder instructions above the display area
        JPanel instructionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        UIUtils.stylePanel(instructionPanel);
        JLabel instructionLabel = new JLabel("How to Input & Save a Workout: Enter lines like 'Exercise: Squat, Sets: 2, Reps: 3, Weight: 20, Unit: KG', then click Save to update progress.");
        instructionLabel.setForeground(Color.WHITE);
        instructionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, instructionPanel.getPreferredSize().height));
        createPanel.add(instructionPanel);

        JPanel createControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        UIUtils.stylePanel(createControls);
        saveWorkoutButton = new UIUtils.StyledButton("Save Workout");
        saveWorkoutButton.addActionListener(e -> saveWorkout());
        createControls.add(saveWorkoutButton);

        createControls.setMaximumSize(new Dimension(Integer.MAX_VALUE, createControls.getPreferredSize().height));
        createPanel.add(createControls);

        // Display area (editable)
        displayArea = new JTextArea(10, 50);
        displayArea.setEditable(true);
        UIUtils.styleTextArea(displayArea);
        JScrollPane displayScrollPane = new JScrollPane(displayArea);
        displayScrollPane.setPreferredSize(new Dimension(600, 350));
        createPanel.add(displayScrollPane);

        createPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        add(createPanel);

        // Load routines
        loadRoutines();

        // Load button action
        loadButton.addActionListener(e -> {
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "Please log in to load routines.");
                return;
            }
            String routineName = (String) routineComboBox.getSelectedItem();
            if (routineName == null || routineName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a routine.");
                return;
            }

            loadButton.setEnabled(false);
            displayArea.setText("Loading routine...\n");

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    loadRoutineFromDatabase(routineName);
                    return null;
                }

                @Override
                protected void done() {
                    loadButton.setEnabled(true);
                }
            }.execute();
        });
    }

    public void loadRoutines() {
        routineComboBox.removeAllItems();
        if (currentUser == null) {
            displayArea.setText("Please log in to view your routines.");
            return;
        }

        String sql = "SELECT name FROM routines WHERE user_id = ? ORDER BY name";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    routineComboBox.addItem(rs.getString("name"));
                    any = true;
                }
                if (!any) {
                    displayArea.setText("No routines found for this user.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading routines: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadRoutineFromDatabase(String routineName) {
        List<String> details = fetchRoutineDetails(routineName);

        SwingUtilities.invokeLater(() -> {
            if (details.isEmpty()) {
                displayArea.setText("No exercises found for routine: " + routineName + ". Please add exercises to this routine.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Selected Routine: ").append(routineName).append("\n\n");
                for (String line : details) {
                    sb.append(line).append("\n");
                }
                displayArea.setText(sb.toString());
                displayArea.setCaretPosition(0);
            }
        });
    }

    private List<String> fetchRoutineDetails(String routineName) {
        List<String> details = new ArrayList<>();

        String sql =
            "SELECT re.exercise, re.sets, re.reps " +
            "FROM routine_exercises re " +
            "JOIN routines r ON re.routine_id = r.id " +
            "WHERE r.name = ? AND r.user_id = ? " +
            "ORDER BY re.exercise";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, routineName);
            stmt.setInt(2, currentUser != null ? currentUser.getId() : 0);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String exercise = rs.getString("exercise");
                    int sets = rs.getInt("sets");
                    int reps = rs.getInt("reps");
                    details.add("Exercise: " + exercise + ", Sets: " + sets + ", Reps: " + reps);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    private void saveWorkout() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Please log in to save a workout.");
            return;
        }

        String[] lines = displayArea.getText().trim().split("\n");
        if (lines.length == 0 || lines[0].trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter at least one workout entry.");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            String workoutSql = "INSERT INTO workouts (user_id, exercise, sets, reps, weight, unit, date) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE)";
            Pattern pattern = Pattern.compile("Exercise: (\\w+), Sets: (\\d+), Reps: (\\d+), Weight: (\\d+\\.?\\d*), Unit: (KG|LBS)");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                Matcher matcher = pattern.matcher(line);
                if (!matcher.matches()) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Invalid format. Use: 'Exercise: Squat, Sets: 2, Reps: 3, Weight: 20, Unit: KG'");
                    return;
                }

                String exercise = matcher.group(1);
                int sets = Integer.parseInt(matcher.group(2));
                int reps = Integer.parseInt(matcher.group(3));
                float weight = Float.parseFloat(matcher.group(4));
                String unit = matcher.group(5);

                try (PreparedStatement pstmt = conn.prepareStatement(workoutSql)) {
                    pstmt.setInt(1, currentUser.getId());
                    pstmt.setString(2, exercise);
                    pstmt.setInt(3, sets);
                    pstmt.setInt(4, reps);
                    pstmt.setFloat(5, weight);
                    pstmt.setString(6, unit);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Workout saved successfully!");
            displayArea.setText(""); // Clear after successful save
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving workout: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for sets, reps, or weight.");
        }
    }
}