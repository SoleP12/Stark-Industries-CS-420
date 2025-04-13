import java.awt.Color;

/*
 * Constants for the simulation application.
 * Helps the readability of the code and makes it easier to manage changes.
 */
public interface Constants {
    // Window dimensions
    int WINDOW_WIDTH = 800;
    int WINDOW_HEIGHT = 600;

    // Road dimensions and positions
    int ROAD_WIDTH = 100;
    int HORIZONTAL_ROAD_Y = WINDOW_HEIGHT/2 - ROAD_WIDTH/2;
    int VERTICAL_ROAD_X = WINDOW_WIDTH/2 - ROAD_WIDTH/2;
    int LANE_OFFSET = 40; // Vertical offset from road start for car lane alignment

    // Entity sizes
    int DRONE_SIZE = 20;
    int CAR_SIZE = 20;

    // Drone movement
    int DRONE_MANUAL_MOVE = 10;       // pixels moved by drone on manual key press
    int DRONE_AUTOPILOT_SPEED = 2;    // speed of drone movement in autopilot mode
    int ORBIT_RADIUS = 50;            // radius for circling in crash scenario
    int ORBIT_ANGLE_STEP_DEGREES = 3; // degrees to move per tick when circling

    // Drone patrol boundaries (for Drone.move rectangular path)
    int DRONE_PATROL_MIN_X = 50;
    int DRONE_PATROL_MIN_Y = 50;
    int DRONE_PATROL_MAX_X = 600;
    int DRONE_PATROL_MAX_Y = 400;

    // Scanning range
    int SCAN_RANGE = 30;

    // Traffic evaluation
    int TRAFFIC_THRESHOLD = 2;
    double TRAFFIC_DISTANCE = 50.0;

    // Timing constants (in milliseconds)
    int FRAME_DELAY = 16;                 // ~60 FPS
    int WRECK_SCAN_INTERVAL = 500;        // interval to check crash site scanning
    int TRAFFIC_SPAWN_INTERVAL = 2000;    // interval to spawn new traffic cars
    int TRAFFIC_MONITOR_INTERVAL = 1000;  // interval to check traffic status
    
    // Colors
    Color ROAD_COLOR = new Color(50, 50, 50);
    Color BUILDING_COLOR = new Color(211, 211, 211);
    Color GRASS_COLOR = new Color(34, 139, 34);
}