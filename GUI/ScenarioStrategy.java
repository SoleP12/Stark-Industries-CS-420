/*
 * Scenario Strategy Interface
 * This interface defines the methods for activating and deactivating different scenarios in the simulation model.
 */

public interface ScenarioStrategy {
    void activate(SimulationModel model);
    void deactivate();
    default void updateDroneAuto(SimulationModel model) {
        model.advanceDroneToNextWaypoint();
    }
}