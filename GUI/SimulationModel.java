import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * SimulationModel class for the Traffic Bot Simulator
 * This class represents the simulation model, including the drone and cars.
 */
public class SimulationModel {
    final private Drone drone;
    final private List<Car> cars;
    private boolean autopilotEnabled;
    private ScenarioStrategy currentScenario;
    private int checkpointIndex;

    // Define waypoints for autopilot navigation (intersection corners)
    private static final int[][] CHECKPOINTS = {
        { Constants.VERTICAL_ROAD_X + Constants.ROAD_WIDTH, Constants.HORIZONTAL_ROAD_Y },
        { Constants.VERTICAL_ROAD_X + Constants.ROAD_WIDTH, Constants.HORIZONTAL_ROAD_Y + Constants.ROAD_WIDTH },
        { Constants.VERTICAL_ROAD_X, Constants.HORIZONTAL_ROAD_Y + Constants.ROAD_WIDTH },
        { Constants.VERTICAL_ROAD_X, Constants.HORIZONTAL_ROAD_Y }
    };

    // Constructor for SimulationModel
    public SimulationModel() {
        this.drone = new Drone(100, 100);
        this.cars = new ArrayList<>();
        this.autopilotEnabled = false;
        this.currentScenario = null;
        this.checkpointIndex = 0;
    }

    // Getters for drone and cars
    public Drone getDrone() {
        return drone;
    }

    public List<Car> getCars() {
        return Collections.unmodifiableList(cars);
    }

    // Getters and setters for autopilot status
    public boolean isAutopilotEnabled() {
        return autopilotEnabled;
    }

    public void setAutopilotEnabled(boolean enabled) {
        autopilotEnabled = enabled;
        if (!enabled) {
            checkpointIndex = 0;
        }
    }

    // Toggle autopilot status
    public void toggleAutopilot() {
        setAutopilotEnabled(!autopilotEnabled);
    }

    // Move the drone based on user input
    public void moveDrone(int dx, int dy) {
        int newX = drone.getX() + dx;
        int newY = drone.getY() + dy;
        newX = Math.max(0, Math.min(newX, Constants.WINDOW_WIDTH - Constants.DRONE_SIZE));
        newY = Math.max(0, Math.min(newY, Constants.WINDOW_HEIGHT - Constants.DRONE_SIZE));
        drone.setPosition(newX, newY);
    }

    // Move the drone automatically to the next waypoint
    public void moveCars() {
        for (Car car : cars) {
            car.move();
        }
    }

    // Update the traffic status of cars based on proximity to each other
    public void updateTrafficStatus() {
        for (Car car : cars) {
            int nearbyCount = 0;
            for (Car other : cars) {
                if (car == other) continue;
                double dx = car.getX() - other.getX();
                double dy = car.getY() - other.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < Constants.TRAFFIC_DISTANCE) {
                    nearbyCount++;
                }
            }
            car.setInTraffic(nearbyCount >= Constants.TRAFFIC_THRESHOLD);
        }
    }

    // Check if the drone is in range of any cars and update the database accordingly
    public void checkProximityToCars() {
        Database db = Database.getInstance();
        for (Car car : cars) {
            if (drone.isInRange(car)) {
                db.addCar(car.getId());
            }
        }
    }

    // Advance the drone to the next waypoint in autopilot mode
    public void advanceDroneToNextWaypoint() {
        int targetX = CHECKPOINTS[checkpointIndex][0];
        int targetY = CHECKPOINTS[checkpointIndex][1];
        int curX = drone.getX();
        int curY = drone.getY();
        if (curX < targetX) {
            curX += Constants.DRONE_AUTOPILOT_SPEED;
        } else if (curX > targetX) {
            curX -= Constants.DRONE_AUTOPILOT_SPEED;
        }
        if (curY < targetY) {
            curY += Constants.DRONE_AUTOPILOT_SPEED;
        } else if (curY > targetY) {
            curY -= Constants.DRONE_AUTOPILOT_SPEED;
        }
        drone.setPosition(curX, curY);
        if (Math.abs(curX - targetX) < Constants.DRONE_AUTOPILOT_SPEED &&
            Math.abs(curY - targetY) < Constants.DRONE_AUTOPILOT_SPEED) {
            checkpointIndex = (checkpointIndex + 1) % CHECKPOINTS.length;
        }
    }

    // Set the current scenario and activate it
    public void setScenario(ScenarioStrategy scenario) {
        if (currentScenario != null) {
            currentScenario.deactivate();
        }
        currentScenario = scenario;
        if (currentScenario != null) {
            currentScenario.activate(this);
        }
    }

    // Get the current scenario
    public ScenarioStrategy getCurrentScenario() {
        return currentScenario;
    }

    // Clear all cars from the simulation
    public void clearCars() {
        cars.clear();
    }

    // Add a car to the simulation
    public void addCar(Car car) {
        cars.add(car);
    }
}