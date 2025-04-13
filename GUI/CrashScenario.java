import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/*
 * Crash Scenario for the simulation.
 * This scenario simulates a crash at the intersection of two roads.
 * It places two cars at the crash site and enables autopilot mode for the drone to circle around the wreck.
 */

public class CrashScenario implements ScenarioStrategy {
    //Variables for the crash scenario
    private Timer wreckScanTimer;
    private int crashCenterX;
    private int crashCenterY;
    private double circleAngle;

    @Override
    // Activate the crash scenario
    // This method sets up the crash site and initializes the cars involved in the crash.
    public void activate(SimulationModel model) {
        System.out.println("Crash Simulation Activated!");
        if (Database.getInstance().getSize() > 0) {
            Database.clearCars();
        }
        model.clearCars();
        // Determine crash site at the center of the intersection
        crashCenterX = Constants.VERTICAL_ROAD_X + Constants.ROAD_WIDTH / 2;
        crashCenterY = Constants.HORIZONTAL_ROAD_Y + Constants.ROAD_WIDTH / 2;
        
        // Add two stopped cars at the crash site (opposite directions)
        model.addCar(new Car(crashCenterX - 25, crashCenterY, 1, 0, Car.Direction.RIGHT));
        model.addCar(new Car(crashCenterX + 25, crashCenterY, 2, 0, Car.Direction.LEFT));

        // Enable autopilot mode to circle the crash site
        model.setAutopilotEnabled(true);
        circleAngle = 0.0;
        System.out.println("Scanning wreck...");

        // Timer to monitor when scanning is complete
        wreckScanTimer = new Timer(Constants.WRECK_SCAN_INTERVAL, new ActionListener() {
            @Override
            // Action performed when the timer ticks
            // This method checks if the database has at least 2 cars (indicating a crash)
            public void actionPerformed(ActionEvent e) {
                if (Database.getInstance().getSize() >= 2) {
                    System.out.println("Scanning Completed!");
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        wreckScanTimer.start();
    }

    @Override
    // Deactivate the crash scenario
    // This method stops the wreck scan timer and disables autopilot mode.
    public void deactivate() {
        if (wreckScanTimer != null) {
            wreckScanTimer.stop();
        }
    }

    @Override
    // Update the drone's position in autopilot mode
    // This method moves the drone in a circular path around the crash site.
    public void updateDroneAuto(SimulationModel model) {
        // Circle around the crash site
        circleAngle += Math.toRadians(Constants.ORBIT_ANGLE_STEP_DEGREES);
        int newX = crashCenterX + (int) (Constants.ORBIT_RADIUS * Math.cos(circleAngle));
        int newY = crashCenterY + (int) (Constants.ORBIT_RADIUS * Math.sin(circleAngle));
        model.getDrone().setPosition(newX, newY);
    }
}