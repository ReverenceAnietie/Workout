package workout;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoutinesPanel extends JPanel {
    private User currentUser;
    private JTextField routineNameField;
    private JPanel exercisesPanel;
    private List<JPanel> exerciseRows = new ArrayList<>();

    public RoutinesPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        UIUtils.stylePanel(this);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBackground(UIUtils.BACKGROUND);

        JLabel routineLabel = new JLabel("Routine Name:");
        UIUtils.styleLabel(routineLabel, false);
        inputPanel.add(routineLabel);

        routineNameField = new JTextField();
        UIUtils.styleTextField(routineNameField);
        inputPanel.add(routineNameField);

        JButton addExerciseButton = new UIUtils.StyledButton("Add Exercise");
        addExerciseButton.addActionListener(e -> addExerciseRow());
        inputPanel.add(addExerciseButton);

        JButton saveRoutineButton = new UIUtils.StyledButton("Save Routine");
        saveRoutineButton.addActionListener(e -> saveRoutine());
        inputPanel.add(saveRoutineButton);

        exercisesPanel = new JPanel();
        exercisesPanel.setLayout(new BoxLayout(exercisesPanel, BoxLayout.Y_AXIS));
        exercisesPanel.setBackground(UIUtils.BACKGROUND);
        JScrollPane scrollPane = new JScrollPane(exercisesPanel);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addExerciseRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 5, 5));
        row.setBackground(UIUtils.BACKGROUND);

        JTextField exerciseTf = new JTextField();
        UIUtils.styleTextField(exerciseTf);
        JTextField setsTf = new JTextField();
        UIUtils.styleTextField(setsTf);
        JTextField repsTf = new JTextField();
        UIUtils.styleTextField(repsTf);

        JButton removeBtn = new UIUtils.StyledButton("Remove");
        removeBtn.addActionListener(e -> removeExerciseRow(row));

        row.add(exerciseTf);
        row.add(setsTf);
        row.add(repsTf);
        row.add(removeBtn);

        exercisesPanel.add(row);
        exerciseRows.add(row);
        exercisesPanel.revalidate();
    }

    private void removeExerciseRow(JPanel row) {
        exercisesPanel.remove(row);
        exerciseRows.remove(row);
        exercisesPanel.revalidate();
        exercisesPanel.repaint();
    }

    private void saveRoutine() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Please log in to save a routine.");
            return;
        }

        String name = routineNameField.getText().trim();
        if (name.isEmpty() || exerciseRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter routine name and at least one exercise");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            int routineId;

            String routineSql = "INSERT INTO routines (user_id, name) VALUES (?, ?) RETURNING id";
            try (PreparedStatement pstmt = conn.prepareStatement(routineSql)) {
                pstmt.setInt(1, currentUser.getId());
                pstmt.setString(2, name);
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                routineId = rs.getInt("id");
            }

            String exSql = "INSERT INTO routine_exercises (routine_id, exercise, sets, reps) VALUES (?, ?, ?, ?)";
            for (JPanel row : exerciseRows) {
                JTextField exTf = (JTextField) row.getComponent(0);
                JTextField setsTf = (JTextField) row.getComponent(1);
                JTextField repsTf = (JTextField) row.getComponent(2);

                String exercise = exTf.getText().trim();
                int sets = Integer.parseInt(setsTf.getText().trim());
                int reps = Integer.parseInt(repsTf.getText().trim());

                try (PreparedStatement pstmt = conn.prepareStatement(exSql)) {
                    pstmt.setInt(1, routineId);
                    pstmt.setString(2, exercise);
                    pstmt.setInt(3, sets);
                    pstmt.setInt(4, reps);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Routine saved successfully!");
            clearRoutineFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving routine: " + ex.getMessage());
        }
    }

    private void clearRoutineFields() {
        routineNameField.setText("");
        exercisesPanel.removeAll();
        exerciseRows.clear();
        exercisesPanel.revalidate();
    }
}
