import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

/*
 * SimulationController class for the Traffic Bot Simulator
 * This class handles user inputs and updates the simulation model.
 */
public class SimulationController implements ActionListener, KeyListener {

    /*
     * Both keyReleased and keyTyped methods are not used in this implementation.
     * But are required to implement the KeyListener interface.
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    final private SimulationModel model;
    final private GUI view;
    final private Timer mainTimer;

    // Constructor for SimulationController
    public SimulationController() {
        // Initialize model and view
        model = new SimulationModel();
        view = new GUI(model);

        // Attach this controller as listeners for user inputs
        view.getAutopilotButton().addActionListener(this);
        view.getDefaultScenarioButton().addActionListener(this);
        view.getCrashScenarioButton().addActionListener(this);
        view.getTrafficScenarioButton().addActionListener(this);
        view.addKeyListener(this);

        // Start with default scenario active
        model.setScenario(new DefaultScenario());

        // Set up the main simulation loop
        mainTimer = new Timer(Constants.FRAME_DELAY, e -> onTick());
        mainTimer.start();
    }

    private void onTick() {
        // Autopilot movement (if enabled)
        if (model.isAutopilotEnabled() && model.getCurrentScenario() != null) {
            model.getCurrentScenario().updateDroneAuto(model);
        }

        // Update cars and traffic status every frame
        model.moveCars();
        model.updateTrafficStatus();

        // Continuously check for nearby cars to scan
        model.checkProximityToCars();

        // Refresh the view
        view.refresh();
    }

    @Override
    // Handle button actions
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        //Switch case to handle different button actions
        switch (command) {
            case "TOGGLE_AUTOPILOT":
                boolean wasEnabled = model.isAutopilotEnabled();
                model.toggleAutopilot();
                if (wasEnabled) {
                    System.out.println("Manual Mode enabled!");
                } else {
                    System.out.println("Autopilot Mode enabled!");
                }
                view.requestFocusInWindow();
                break;
            case "SCENARIO_DEFAULT":
                model.setScenario(new DefaultScenario());
                view.requestFocusInWindow();
                break;
            case "SCENARIO_CRASH":
                model.setScenario(new CrashScenario());
                view.requestFocusInWindow();
                break;
            case "SCENARIO_TRAFFIC":
                model.setScenario(new TrafficScenario());
                view.requestFocusInWindow();
                break;
        }
    }

    @Override
    // Handle key events for manual control of the drone
    public void keyPressed(KeyEvent e) {
        // Disable autopilot on manual control
        model.setAutopilotEnabled(false);
        int key = e.getKeyCode();

        // Move the drone based on key presses
        switch (key) {
            case KeyEvent.VK_UP -> model.moveDrone(0, -Constants.DRONE_MANUAL_MOVE);
            case KeyEvent.VK_DOWN -> model.moveDrone(0, Constants.DRONE_MANUAL_MOVE);
            case KeyEvent.VK_LEFT -> model.moveDrone(-Constants.DRONE_MANUAL_MOVE, 0);
            case KeyEvent.VK_RIGHT -> model.moveDrone(Constants.DRONE_MANUAL_MOVE, 0);
        }

        // Check for newly scanned cars after moving
        model.checkProximityToCars();

        // Refresh view immediately
        view.refresh();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimulationController::new);
    }
}