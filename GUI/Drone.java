/*
 * Drone class for the simulation.
 * This class represents the Traffic Drone
 */

public class Drone {
    //variables
    private int x, y;
    private final int speed;
    private int direction; // 0: right, 1: down, 2: left, 3: up

    // Constructor
    public Drone(int x, int y) {
        this.x = x;
        this.y = y;
        this.speed = 1;
        this.direction = 0;
    }

    public void move() {
        // Follow a fixed rectangular path defined by boundaries
        switch (direction) {
            case 0 -> { // move right
                x += speed;
                if (x >= Constants.DRONE_PATROL_MAX_X) {
                    direction = 1;
                }
            }
            case 1 -> { // move down
                y += speed;
                if (y >= Constants.DRONE_PATROL_MAX_Y) {
                    direction = 2;
                }
            }
            case 2 -> { // move left
                x -= speed;
                if (x <= Constants.DRONE_PATROL_MIN_X) {
                    direction = 3;
                }
            }
            case 3 -> { // move up
                y -= speed;
                if (y <= Constants.DRONE_PATROL_MIN_Y) {
                    direction = 0;
                }
            }
        }
    }

    // Check if the drone is in range of a car
    public boolean isInRange(Car car) {
        int dx = x - car.getX();
        int dy = y - car.getY();
        return Math.sqrt(dx * dx + dy * dy) < Constants.SCAN_RANGE;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // Setters for x and y coordinates
    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }
}