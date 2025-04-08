package tellolib.camera;

import org.opencv.core.Mat;
import org.opencv.core.Rect;


/* 
 * CarDetection header
 */
public interface CarDetectionInterface
{
    /*
     * perform car detection on the given image aka MAat
     * frame Mat to examine for cars
     * True if cars are detected, false otherwise
     */
    boolean detectCars(Mat frame);

    /*
     * Perform car detection on the current image in the camera stream
     * True if cars are detected, false otherwise
     */
    boolean detectCars();

    /*
     * get the number of cars detected in the last call to detectCars()
     number of cars found... may be zero case
     */
    int getCarCount();

    /*
     * return bounding rectangles for cars detected in the last call to detectCars()
     * array of Rect bounding each detected car
     */
    Rect[] getCars();
}
