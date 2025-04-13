import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/*
 * TrafficScenario class for the Traffic Bot Simulator
 * This class implements the ScenarioStrategy interface and manages the traffic simulation.
 */
public class TrafficScenario implements ScenarioStrategy {
    private Timer spawnTimer;
    private Timer trafficMonitorTimer;
    private boolean lightPrinted;
    private boolean moderatePrinted;
    private boolean heavyPrinted;
    private int nextCarId;

    @Override
    // Activate the traffic simulation scenario
    public void activate(SimulationModel model) {
        System.out.println("Traffic Simulation Activated!");
        if (Database.getInstance().getSize() > 0) {
            Database.clearCars();
        }
        model.clearCars();
        nextCarId = 1;

        // Add initial car on the horizontal road
        model.addCar(new Car(0, Constants.HORIZONTAL_ROAD_Y + Constants.LANE_OFFSET, nextCarId++, 1, Car.Direction.RIGHT));

        // Reset traffic status message flags
        lightPrinted = false;
        moderatePrinted = false;
        heavyPrinted = false;

        // Prepare a timer to spawn new cars every 2 seconds
        spawnTimer = new Timer(Constants.TRAFFIC_SPAWN_INTERVAL, new ActionListener() {
            @Override
            // Spawn a new car at regular intervals
            public void actionPerformed(ActionEvent e) {
                model.addCar(new Car(0, Constants.HORIZONTAL_ROAD_Y + Constants.LANE_OFFSET, nextCarId++, 1, Car.Direction.RIGHT));
            }
        });

        // Prepare a timer to monitor traffic conditions
        trafficMonitorTimer = new Timer(Constants.TRAFFIC_MONITOR_INTERVAL, new ActionListener() {
            @Override
            // Check the number of scanned cars and print traffic status
            // based on the number of cars in the database
            public void actionPerformed(ActionEvent e) {
                int scannedCars = Database.getInstance().getSize();
                if (scannedCars > 7 && !heavyPrinted) {
                    System.out.println("Traffic is heavy");
                    heavyPrinted = true;
                } else if (scannedCars > 5 && !moderatePrinted) {
                    System.out.println("Traffic is moderate");
                    moderatePrinted = true;
                } else if (scannedCars > 3 && !lightPrinted) {
                    System.out.println("Traffic is light");
                    lightPrinted = true;
                }
            }
        });

        // Start monitoring traffic and perform an initial scan
        trafficMonitorTimer.start();
        model.checkProximityToCars();

        // Start spawning cars
        spawnTimer.start();
    }

    @Override

    //deactivate the traffic simulation scenario
    public void deactivate() {
        if (spawnTimer != null) {
            spawnTimer.stop();
        }
        if (trafficMonitorTimer != null) {
            trafficMonitorTimer.stop();
        }
    }
}