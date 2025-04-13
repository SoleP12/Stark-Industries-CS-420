import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

/*
 * Game panel for rendering the simulation.
 * This class is responsible for drawing the background, the drone, and the cars.
 */

public class GamePanel extends JPanel {
    final private SimulationModel model;
    final private BufferedImage backgroundImage;

    // Constructor initializes the model and pre-renders the background
    public GamePanel(SimulationModel model) {
        this.model = model;
        // Set background color for any uncovered area
        setBackground(Constants.GRASS_COLOR);
        // Pre-render static background (roads, buildings)
        backgroundImage = BackgroundRenderer.createBackground(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    @Override
    // Override paintComponent to draw the game elements
    // This method is called whenever the panel needs to be redrawn
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw static background
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, null);
        }

        // Draw the drone as a red square
        g2d.setColor(Color.RED);
        int droneX = model.getDrone().getX();
        int droneY = model.getDrone().getY();
        g2d.fillRect(droneX, droneY, Constants.DRONE_SIZE, Constants.DRONE_SIZE);
        
        // Draw cars (yellow if in traffic, blue otherwise)
        List<Car> cars = model.getCars();
        for (Car car : cars) {
            g2d.setColor(car.isInTraffic() ? Color.YELLOW : Color.BLUE);
            g2d.fillOval(car.getX(), car.getY(), Constants.CAR_SIZE, Constants.CAR_SIZE);
        }
    }
}