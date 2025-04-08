package tellolib.camera;

import org.opencv.core.*;
import org.opencv.dnn.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

/* 
 * Purpose: The main implementation for the CarDetection.
 * Uses Singleton Method/instance for easier usage in the demo
 * Refactored the code to implement YOLOv5M in object detection for accuracy
 */
public class CarDetection implements CarDetectionInterface {
    private static CarDetection instance;
    private final Net net;
    private final float confThreshold = 0.5f;
    private final float nmsThreshold = 0.4f;
    private List<Rect> lastDetections = new ArrayList<>();
    private Mat lastFrame = null;

    private CarDetection() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        this.net = Dnn.readNetFromONNX("resources/yolov5m.onnx");
    }
    /* 
     * Singleton Method implementation
     */
    public static CarDetection getInstance() {
        if (instance == null) {
            instance = new CarDetection();
        }
        return instance;
    }

    @Override
    public boolean detectCars(Mat frame) {
        this.lastFrame = frame;
        lastDetections.clear();

        Size inputSize = new Size(640, 640);
        Mat blob = Dnn.blobFromImage(frame, 1.0 / 255.0, inputSize, new Scalar(0, 0, 0), true, false);
        net.setInput(blob);

        List<Mat> outputs = new ArrayList<>();
        List<String> outNames = new ArrayList<>();
        outNames.add("output");
        net.forward(outputs, outNames);

        Mat detections = outputs.get(0).reshape(1, (int)outputs.get(0).total() / 85);

        for (int i = 0; i < detections.rows(); i++) {
            double confidence = detections.get(i, 4)[0];
            if (confidence > confThreshold) {
                int classId = -1;
                double maxClassScore = 0;

                for (int j = 5; j < 85; j++) {
                    double score = detections.get(i, j)[0];
                    if (score > maxClassScore) {
                        maxClassScore = score;
                        classId = j - 5;
                    }
                }

                if (classId == 2 && maxClassScore > confThreshold) { // class 2 = car
                    double cx = detections.get(i, 0)[0] * frame.cols();
                    double cy = detections.get(i, 1)[0] * frame.rows();
                    double w = detections.get(i, 2)[0] * frame.cols();
                    double h = detections.get(i, 3)[0] * frame.rows();

                    int left = (int)(cx - w / 2);
                    int top = (int)(cy - h / 2);
                    lastDetections.add(new Rect(left, top, (int)w, (int)h));
                }
            }
        }

        return !lastDetections.isEmpty();
    }

    @Override
    public boolean detectCars() {
        if (lastFrame == null) return false;
        return detectCars(lastFrame);
    }

    @Override
    public int getCarCount() {
        return lastDetections.size();
    }

    @Override
    public Rect[] getCars() {
        return lastDetections.toArray(new Rect[0]);
    }
}
