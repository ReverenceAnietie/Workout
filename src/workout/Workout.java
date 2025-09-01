package workout;

import javax.swing.*;
import java.awt.*;

public class Workout extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User currentUser; // logged-in user
    private JTabbedPane tabbedPane;

    public Workout() {
        // Ensure database tables exist
        Database.createTables();

        setTitle("Gym Workout Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BACKGROUND); // modern background

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        UIUtils.stylePanel(mainPanel); // Apply consistent styling

        addLoginPanel();
        addSignupPanel();
        addHomePanel();

        add(mainPanel);
        showPanel("Login");
    }

    private void addLoginPanel() {
        LoginPanel loginPanel = new LoginPanel(this);
        mainPanel.add(loginPanel, "Login");
    }

    private void addSignupPanel() {
        SignupPanel signupPanel = new SignupPanel(this);
        mainPanel.add(signupPanel, "Signup");
    }

    private void addHomePanel() {
        tabbedPane = new JTabbedPane();
        UIUtils.styleTabbedPane(tabbedPane);

        // Initialize panels with null user; refresh after login
        tabbedPane.addTab("Workout", new WorkoutPanel((User) null));
        tabbedPane.addTab("Progress", new ProgressPanel((User) null));
        tabbedPane.addTab("Routines", new RoutinesPanel((User) null));
        tabbedPane.addTab("Profile", new ProfilePanel((User) null));
        tabbedPane.addTab("About", new AboutPanel((User) null));

        mainPanel.add(tabbedPane, "Home");
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    /**
     * Called after login/signup success.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Refresh Home panel tabs with current user
        refreshHomePanel();
        showPanel("Home");
    }

    private void refreshHomePanel() {
        // Remove existing tabs
        tabbedPane.removeAll();

        // Re-add tabs with currentUser
        tabbedPane.addTab("Workout", new WorkoutPanel(currentUser));
        tabbedPane.addTab("Progress", new ProgressPanel(currentUser));
        tabbedPane.addTab("Routines", new RoutinesPanel(currentUser));
        tabbedPane.addTab("Profile", new ProfilePanel(currentUser));
        tabbedPane.addTab("About", new AboutPanel(currentUser));

        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Workout().setVisible(true));
    }
}