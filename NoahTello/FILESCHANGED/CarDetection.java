package tellolib.camera;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.DetectionModel;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * CarDetection uses OpenCV's DNN DetectionModel to run YOLOv8s and find cars (COCO class 2) in frames.
 */
public class CarDetection implements CarDetectionInterface {
    private static CarDetection instance;

    // OpenCV DNN wrapper for YOLOv8
    private DetectionModel model;
    private float confThreshold = 0.5f;  // minimum confidence to consider
    private float nmsThreshold  = 0.4f;  // IoU threshold for NMS

    private List<Rect> lastDetections = new ArrayList<>();
    private Mat lastFrame = null;

    private CarDetection() {
        // Load native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Load YOLOv8 ONNX into the DetectionModel
        String modelPath = System.getProperty("user.dir") + "/src/resources/yolov8s.onnx";
        model = new DetectionModel(Dnn.readNetFromONNX(modelPath));

        // Configure input preprocessing: scale, size, mean, swapRB, no crop
        model.setInputParams(
            1.0 / 255.0,
            new Size(640, 640),
            new Scalar(0, 0, 0),
            false,
            false
        );
    }

    public static CarDetection getInstance() {
        if (instance == null) {
            synchronized (CarDetection.class) {
                if (instance == null) {
                    instance = new CarDetection();
                }
            }
        }
        return instance;
    }

    /**
     * Detect cars (classId == 2) in the given frame.
     * @param frame BGR image to process
     * @return true if at least one car was found
     */
    @Override
    public boolean detectCars(Mat frame) {
        lastDetections.clear();
        lastFrame = frame;

        if (frame.empty()) {
            System.err.println("[CarDetection] Empty frame");
            return false;
        }

        // Prepare holders for outputs
        MatOfRect  boxes      = new MatOfRect();
        MatOfFloat confidences = new MatOfFloat();
        MatOfInt   classIds   = new MatOfInt();

        // Run detection (correct argument order for classIds, confidences, boxes)
        model.detect(frame, classIds, confidences, boxes, confThreshold, nmsThreshold);

        Rect[] rectArr   = boxes.toArray();
        int[]   idArr    = classIds.toArray();

        // Filter only COCO class 2 (car)
        for (int i = 0; i < rectArr.length; i++) {
            if (idArr[i] == 2) {
                lastDetections.add(rectArr[i]);
            }
        }

        // Save a snapshot with drawn boxes if any cars found
        if (!lastDetections.isEmpty()) {
            saveFrameWithBoxes(frame, lastDetections);
        }

        return !lastDetections.isEmpty();
    }

    /**
     * Draws the detected boxes on a copy of the frame and writes it to disk.
     */
    private void saveFrameWithBoxes(Mat frame, List<Rect> boxes) {
        Mat output = frame.clone();

        for (Rect r : boxes) {
            // ensure rectangle is within bounds
            Rect safe = new Rect(
                Math.max(0, Math.min(output.cols() - 1, r.x)),
                Math.max(0, Math.min(output.rows() - 1, r.y)),
                Math.min(r.width, output.cols() - r.x),
                Math.min(r.height, output.rows() - r.y)
            );

            Imgproc.rectangle(output, safe, new Scalar(0, 255, 0), 2);
            Imgproc.putText(
                output,
                "Car",
                new Point(safe.x, safe.y - 10),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.7,
                new Scalar(0, 255, 0),
                2
            );
        }

        String outputDir = System.getProperty("user.dir") + "/detections";
        java.io.File dir = new java.io.File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        String filePath = outputDir + "/car_" + System.currentTimeMillis() + ".png";
        Imgcodecs.imwrite(filePath, output);
        System.out.println("[CarDetection] Saved car image to: " + filePath);
    }

    /**
     * Re-run detection on the last frame (if any).
     */
    @Override
    public boolean detectCars() {
        if (lastFrame == null) return false;
        return detectCars(lastFrame);
    }

    /**
     * @return number of cars detected in the last processed frame
     */
    @Override
    public int getCarCount() {
        return lastDetections.size();
    }

    /**
     * @return array of bounding boxes for detected cars
     */
    @Override
    public Rect[] getCars() {
        return lastDetections.toArray(new Rect[0]);
    }
}
