package tellolib.camera;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * Interface for car detection systems.
 * Defines methods to detect cars in video frames and retrieve detection results.
 */
public interface CarDetectionInterface {

    /**
     * Perform car detection on the provided image frame.
     * @param frame The OpenCV Mat frame to analyze.
     * @return True if at least one car is detected, false otherwise.
     */
    boolean detectCars(Mat frame);

    /**
     * Perform car detection on the last frame provided.
     * Useful for reprocessing the previous image.
     * @return True if at least one car is detected, false otherwise.
     */
    boolean detectCars();

    /**
     * Returns the number of cars detected in the last processed frame.
     * @return The number of detected cars (can be zero).
     */
    int getCarCount();

    /**
     * Returns an array of bounding rectangles around detected cars from the last processed frame.
     * @return Array of Rect objects representing detected car locations.
     */
    Rect[] getCars();
}
