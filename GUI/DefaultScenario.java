/*
 * Default scenario for the simulation.
 * Simple scenario with cars moving in opposite directions on horizontal and vertical roads.
 * This scenario is used when no specific scenario is selected.
 */

public class DefaultScenario implements ScenarioStrategy {
    @Override
    // This method is called when the scenario is activated.
    public void activate(SimulationModel model) {
        System.out.println("Default Scenario Activated!");

        // Clear previously scanned cars from the database
        if (Database.getInstance().getSize() > 0) {
            Database.clearCars();
        }
        model.clearCars();

        // Add initial cars positioned on the roads
        model.addCar(new Car(100, Constants.HORIZONTAL_ROAD_Y + Constants.LANE_OFFSET, 1, 3, Car.Direction.RIGHT));
        model.addCar(new Car(Constants.WINDOW_WIDTH - 100, Constants.HORIZONTAL_ROAD_Y + Constants.LANE_OFFSET, 2, 3, Car.Direction.LEFT));
        model.addCar(new Car(Constants.VERTICAL_ROAD_X + Constants.LANE_OFFSET, 100, 3, 3, Car.Direction.DOWN));
    }

    @Override
    // This method is called when the scenario is deactivated.
    public void deactivate() {
    }
}