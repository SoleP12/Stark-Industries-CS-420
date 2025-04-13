import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

/*
 * Renders the background for the simulation window, including roads and buildings.
 */

public class BackgroundRenderer {
    // Creates a BufferedImage representing the background of the simulation window.
    public static BufferedImage createBackground(int width, int height) {
        BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffer.createGraphics();

        // Fill background with grass color
        g2d.setColor(Constants.GRASS_COLOR);
        g2d.fillRect(0, 0, width, height);

        // Draw roads and buildings onto the background
        drawRoads(g2d, width, height);
        drawBuildings(g2d);
        //
        g2d.dispose();
        return buffer;
    }

    // Draws the roads and lane markings on the background.
    private static void drawRoads(Graphics2D g2d, int width, int height) {
        g2d.setColor(Constants.ROAD_COLOR);
        int horizontalY = height/2 - Constants.ROAD_WIDTH/2;
        int verticalX = width/2 - Constants.ROAD_WIDTH/2;

        // Draw horizontal and vertical road surfaces
        g2d.fillRect(0, horizontalY, width, Constants.ROAD_WIDTH);
        g2d.fillRect(verticalX, 0, Constants.ROAD_WIDTH, height);

        // Draw dashed white lane markings in the middle of the roads
        g2d.setColor(Color.WHITE);
        Stroke oldStroke = g2d.getStroke();
        float[] dashPattern = {10, 15};
        BasicStroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dashPattern, 0);
        g2d.setStroke(dashed);

        // Horizontal lane line
        g2d.drawLine(0, horizontalY + Constants.ROAD_WIDTH/2, width, horizontalY + Constants.ROAD_WIDTH/2);

        // Vertical lane line
        g2d.drawLine(verticalX + Constants.ROAD_WIDTH/2, 0, verticalX + Constants.ROAD_WIDTH/2, height);
        g2d.setStroke(oldStroke);
    }

    // Draws buildings on the background.
    private static void drawBuildings(Graphics2D g2d) {
        g2d.setColor(Constants.BUILDING_COLOR);
        
        // Four buildings at predetermined locations
        g2d.fillRect(50, 100, 250, 100);   // Building 1
        g2d.fillRect(545, 50, 150, 180);   // Building 2
        g2d.fillRect(50, 350, 175, 180);   // Building 3
        g2d.fillRect(550, 350, 130, 130);  // Building 4
    }
}
