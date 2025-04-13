/*
 * The Car class represents a car in the simulation.
 * It includes properties such as position, speed, direction, and status (in traffic or crashed).
 */

public class Car {
    // Constants for car movement and behavior
    public enum Direction { RIGHT, LEFT, DOWN, UP }

    // Car properties
    private int x, y;
    private final int id;
    private final int speed;
    private final Direction direction;
    private boolean inTraffic;
    private boolean crashed;

    // Constructor to initialize the car's properties
    public Car(int x, int y, int id, int speed, Direction direction) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.speed = speed;
        this.direction = direction;
        this.inTraffic = false;
        this.crashed = false;
    }

    // Method to check if the car is within a certain distance of another car
    public void move() {
        if (crashed) {
            return;
        }
        // Move the car based on its direction
        switch (direction) {
            case RIGHT -> x += speed;
            case LEFT  -> x -= speed;
            case DOWN  -> y += speed;
            case UP    -> y -= speed;
        }
        // Wrap around screen edges
        if (x > Constants.WINDOW_WIDTH) {
            x = 0;
        }
        if (x < 0) {
            x = Constants.WINDOW_WIDTH;
        }
        if (y > Constants.WINDOW_HEIGHT) {
            y = 0;
        }
        if (y < 0) {
            y = Constants.WINDOW_HEIGHT;
        }
    }

    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getId() { return id; }
    public boolean isInTraffic() { return inTraffic; }
    public void setInTraffic(boolean inTraffic) { this.inTraffic = inTraffic; }
    public boolean isCrashed() { return crashed; }
    public void setCrashed(boolean crashed) { this.crashed = crashed; }
}