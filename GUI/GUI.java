import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/*
 * GUI class for the Traffic Bot Simulator
 * This class creates the main window and handles user interactions.
 * Main class for the GUI of the Traffic Bot Simulator.
 */

public class GUI extends JFrame {
    final private SimulationModel model;
    final private GamePanel gamePanel;
    final private JButton autopilotButton;
    final private JButton defaultScenarioButton;
    final private JButton crashScenarioButton;
    final private JButton trafficScenarioButton;

    // Constructor for GUI
    public GUI(SimulationModel model) {
        this.model = model;
        setTitle("Traffic Bot Simulator");
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create game panel for drawing
        gamePanel = new GamePanel(model);

        // Create button panel with control buttons
        JPanel buttonPanel = new JPanel();
        autopilotButton = new JButton("AutoPilot/Manual");
        defaultScenarioButton = new JButton("Default Scenario");
        crashScenarioButton = new JButton("Crash Scenario");
        trafficScenarioButton = new JButton("Traffic Scenario");

        // Set action commands for controller identification
        autopilotButton.setActionCommand("TOGGLE_AUTOPILOT");
        defaultScenarioButton.setActionCommand("SCENARIO_DEFAULT");
        crashScenarioButton.setActionCommand("SCENARIO_CRASH");
        trafficScenarioButton.setActionCommand("SCENARIO_TRAFFIC");

        // Add buttons to the panel
        buttonPanel.add(autopilotButton);
        buttonPanel.add(defaultScenarioButton);
        buttonPanel.add(crashScenarioButton);
        buttonPanel.add(trafficScenarioButton);

        // Add panels to frame
        add(gamePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setFocusable(true);
        setVisible(true);
    }

    // Getters for components
    public GamePanel getGamePanel() {
        return gamePanel;
    }
    public JButton getAutopilotButton() {
        return autopilotButton;
    }
    public JButton getDefaultScenarioButton() {
        return defaultScenarioButton;
    }
    public JButton getCrashScenarioButton() {
        return crashScenarioButton;
    }
    public JButton getTrafficScenarioButton() {
        return trafficScenarioButton;
    }

    // Refresh the game panel to update the display
    public void refresh() {
        gamePanel.repaint();
    }
}