package workout;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.axis.NumberAxis;
import java.util.Timer;
import java.util.TimerTask;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;

public class ProgressPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton, showChartButton;
    private JComboBox<String> comparisonTypeCombo;
    private JPanel chartPanel;
    private User currentUser;
    private Map<String, Object> progressData; // Changed to Object to support mixed data types

    public ProgressPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        UIUtils.stylePanel(this);

        JLabel titleLabel = new JLabel("Progress Tracker");
        UIUtils.styleLabel(titleLabel, true);
        add(titleLabel, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        UIUtils.stylePanel(topPanel);

        searchField = new JTextField(20);
        UIUtils.styleTextField(searchField);
        topPanel.add(searchField);

        searchButton = new UIUtils.StyledButton("Search");
        topPanel.add(searchButton);

        comparisonTypeCombo = new JComboBox<>(new String[]{
            "Total Reps by Exercise", "Reps Over Time", "Sets vs. Reps by Exercise",
            "Weight Lifted by Exercise", "Progress by Unit", "Workout Frequency by Routine"
        });
        comparisonTypeCombo.setSelectedIndex(0);
        UIUtils.styleComboBox(comparisonTypeCombo);
        topPanel.add(comparisonTypeCombo);

        showChartButton = new UIUtils.StyledButton("Show Chart");
        topPanel.add(showChartButton);

        add(topPanel, BorderLayout.CENTER);

        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setPreferredSize(new Dimension(600, 200));
        UIUtils.stylePanel(chartPanel);
        JScrollPane chartScrollPane = new JScrollPane(chartPanel);
        add(chartScrollPane, BorderLayout.SOUTH);

        progressData = new HashMap<>();

        searchButton.addActionListener(e -> searchProgress());
        showChartButton.addActionListener(e -> showChart());

        // Real-time update timer
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentUser != null) {
                    searchProgress(); // Re-fetch data
                    showChart(); // Update chart
                }
            }
        }, 0, 5000); // Update every 5 seconds
    }

    private void searchProgress() {
        if (currentUser == null) {
            return;
        }

        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            progressData.clear(); // Clear data if keyword is empty
            return;
        }

        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() {
                return fetchProgress(keyword);
            }

            @Override
            protected void done() {
                try {
                    progressData = get();
                    if (progressData.isEmpty()) {
                        System.out.println("No data fetched for keyword: " + keyword); // Debug log
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Error fetching progress: " + ex.getMessage()); // Debug log
                }
            }
        }.execute();
    }

    private Map<String, Object> fetchProgress(String keyword) {
        Map<String, Object> data = new HashMap<>();
        String comparisonType = (String) comparisonTypeCombo.getSelectedItem();

        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt;
            switch (comparisonType) {
                case "Total Reps by Exercise":
                    stmt = conn.prepareStatement(
                            "SELECT re.exercise, SUM(re.reps) AS total_reps " +
                            "FROM routine_exercises re JOIN routines r ON re.routine_id = r.id " +
                            "WHERE (r.name ILIKE ? OR re.exercise ILIKE ?) AND r.user_id = ? " +
                            "GROUP BY re.exercise LIMIT 14");
                    stmt.setString(1, "%" + keyword + "%");
                    stmt.setString(2, "%" + keyword + "%");
                    stmt.setInt(3, currentUser.getId());
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        data.put(rs.getString("exercise"), rs.getInt("total_reps"));
                    }
                    break;

                case "Reps Over Time":
                    stmt = conn.prepareStatement(
                            "SELECT re.exercise, DATE_TRUNC('week', w.date) AS week, SUM(w.reps) AS total_reps " +
                            "FROM routine_exercises re JOIN routines r ON re.routine_id = r.id " +
                            "JOIN workouts w ON w.exercise = re.exercise AND w.user_id = r.user_id " +
                            "WHERE (r.name ILIKE ? OR re.exercise ILIKE ?) AND r.user_id = ? " +
                            "GROUP BY re.exercise, week ORDER BY week LIMIT 14");
                    stmt.setString(1, "%" + keyword + "%");
                    stmt.setString(2, "%" + keyword + "%");
                    stmt.setInt(3, currentUser.getId());
                    Map<String, Map<String, Integer>> timeData = new HashMap<>();
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        String exercise = rs.getString("exercise");
                        String week = rs.getString("week");
                        int reps = rs.getInt("total_reps");
                        timeData.computeIfAbsent(exercise, k -> new HashMap<>()).put(week, reps);
                    }
                    data.put("timeData", timeData);
                    break;

                case "Sets vs. Reps by Exercise":
                    stmt = conn.prepareStatement(
                            "SELECT re.exercise, SUM(re.sets) AS total_sets, AVG(re.reps) AS avg_reps " +
                            "FROM routine_exercises re JOIN routines r ON re.routine_id = r.id " +
                            "WHERE (r.name ILIKE ? OR re.exercise ILIKE ?) AND r.user_id = ? " +
                            "GROUP BY re.exercise LIMIT 14");
                    stmt.setString(1, "%" + keyword + "%");
                    stmt.setString(2, "%" + keyword + "%");
                    stmt.setInt(3, currentUser.getId());
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        Map<String, Number> metrics = new HashMap<>();
                        metrics.put("sets", rs.getInt("total_sets"));
                        metrics.put("avg_reps", rs.getDouble("avg_reps"));
                        data.put(rs.getString("exercise"), metrics);
                    }
                    break;

                case "Weight Lifted by Exercise":
                    stmt = conn.prepareStatement(
                            "SELECT w.exercise, SUM(w.reps * w.weight) AS total_weight " +
                            "FROM workouts w JOIN routines r ON w.user_id = r.user_id " +
                            "WHERE (r.name ILIKE ? OR w.exercise ILIKE ?) AND w.user_id = ? " +
                            "GROUP BY w.exercise LIMIT 14");
                    stmt.setString(1, "%" + keyword + "%");
                    stmt.setString(2, "%" + keyword + "%");
                    stmt.setInt(3, currentUser.getId());
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        data.put(rs.getString("exercise"), rs.getDouble("total_weight"));
                    }
                    break;

                case "Progress by Unit":
                    stmt = conn.prepareStatement(
                            "SELECT w.unit, SUM(w.reps) AS total_reps " +
                            "FROM workouts w JOIN routines r ON w.user_id = r.user_id " +
                            "WHERE (r.name ILIKE ? OR w.exercise ILIKE ?) AND w.user_id = ? " +
                            "GROUP BY w.unit");
                    stmt.setString(1, "%" + keyword + "%");
                    stmt.setString(2, "%" + keyword + "%");
                    stmt.setInt(3, currentUser.getId());
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        data.put(rs.getString("unit"), rs.getInt("total_reps"));
                    }
                    break;

                case "Workout Frequency by Routine":
                    stmt = conn.prepareStatement(
                            "SELECT r.name, COUNT(*) AS frequency " +
                            "FROM routines r WHERE r.user_id = ? AND r.name ILIKE ? " +
                            "GROUP BY r.name LIMIT 14");
                    stmt.setInt(1, currentUser.getId());
                    stmt.setString(2, "%" + keyword + "%");
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        data.put(rs.getString("name"), rs.getInt("frequency"));
                    }
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void showChart() {
        chartPanel.removeAll();

        if (progressData.isEmpty()) {
            JLabel noDataLabel = new JLabel("No data available. Please search for a valid keyword.");
            noDataLabel.setForeground(Color.WHITE);
            chartPanel.add(noDataLabel, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
            return;
        }

        String comparisonType = (String) comparisonTypeCombo.getSelectedItem();
        JFreeChart chart;

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if ("Reps Over Time".equals(comparisonType)) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Integer>> timeData = (Map<String, Map<String, Integer>>) progressData.get("timeData");
            if (timeData != null) {
                for (Map.Entry<String, Map<String, Integer>> exerciseEntry : timeData.entrySet()) {
                    String exercise = exerciseEntry.getKey();
                    for (Map.Entry<String, Integer> weekEntry : exerciseEntry.getValue().entrySet()) {
                        dataset.addValue(weekEntry.getValue(), exercise, weekEntry.getKey());
                    }
                }
            }
        } else if ("Sets vs. Reps by Exercise".equals(comparisonType)) {
            for (Map.Entry<String, Object> entry : progressData.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Number> metrics = (Map<String, Number>) entry.getValue();
                    dataset.addValue(metrics.get("sets"), "Sets", entry.getKey());
                    dataset.addValue(metrics.get("avg_reps"), "Avg Reps", entry.getKey());
                }
            }
        } else {
            for (Map.Entry<String, Object> entry : progressData.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    dataset.addValue((Number) entry.getValue(), comparisonType, entry.getKey());
                }
            }
        }
        chart = ChartFactory.createBarChart(comparisonType, "Category", "Value", dataset, PlotOrientation.HORIZONTAL, true, true, true);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(UIUtils.BACKGROUND);
        plot.setRangeGridlinePaint(new Color(255, 255, 255, 50));
        plot.setDomainGridlinePaint(new Color(255, 255, 255, 50));
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.1);
        renderer.setShadowVisible(true);
        renderer.setShadowXOffset(4);
        renderer.setShadowYOffset(4);
        renderer.setShadowPaint(new Color(0, 0, 0, 50));

        // Assign different colors for two-object comparisons
        if ("Sets vs. Reps by Exercise".equals(comparisonType)) {
            renderer.setSeriesPaint(0, new Color(0, 162, 235, 200)); // Blue for Sets
            renderer.setSeriesPaint(1, new Color(255, 165, 0, 200)); // Orange for Avg Reps
        } else {
            renderer.setSeriesPaint(0, new Color(0, 128, 0, 200)); // Green for single series
        }

        // Dual-axis for Sets vs. Reps
        if ("Sets vs. Reps by Exercise".equals(comparisonType)) {
            NumberAxis rangeAxis2 = new NumberAxis("Avg Reps");
            plot.setRangeAxis(1, rangeAxis2);
            plot.mapDatasetToRangeAxis(1, 1);
        }

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(600, 200));
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
}