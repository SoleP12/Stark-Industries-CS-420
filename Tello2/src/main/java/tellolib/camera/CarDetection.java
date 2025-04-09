package tellolib.camera;

//Import OpenCV 
import org.opencv.core.*;
import org.opencv.dnn.*;
import org.opencv.imgproc.Imgproc;
//Import ArrayList and List for dynamic arrays and list interface
import java.util.ArrayList;
import java.util.List;

/* 
 * Purpose: The main implementation for the CarDetection.
 * Uses Singleton Method/instance for easier usage in the demo
 * Refactored the code to implement YOLOv5M in object detection for accuracy
 */
public class CarDetection implements CarDetectionInterface {
    private static CarDetection instance;    //Singleton instance for CarDetection
    private final Net net; //OpenCV deep nerual network model for detecting objs.
    private final float confThreshold = 0.5f; //Filter for weak detection
    private final float nmsThreshold = 0.4f;  //Filter overlapping boxes from detections
    private List<Rect> lastDetections = new ArrayList<>(); //Stores list of detected cars boxes.
    private Mat lastFrame = null; //stores last frame memory for reuse if needed

    /*
    Private constructor to initialize CarDetection instance.
    Loads the OpenCV lib and Yolov5M model.
    */
    private CarDetection() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); //OpenCV native library
        this.net = Dnn.readNetFromONNX("resources/yolov5m.onnx"); //Loads the Yolov5 :)
    }
    /* 
     * Singleton Method implementation
     * provides a global point of access to the CarDetection instance.
     */
    public static CarDetection getInstance() {
        if (instance == null) {
            instance = new CarDetection();
        }
        return instance;
    }

    /*
    Processes a given frame to detect cars using YOLOv5
    Parameter: the given frame from the video stream
    Return: true if a car is detected in the frame, otherwise false
    */
    @Override
    public boolean detectCars(Mat frame) {
        //save current frame
        this.lastFrame = frame;
        //clear previous detections
        lastDetections.clear();

        //Define the size for which the image will be resized for the neural network
        Size inputSize = new Size(640, 640);
        //Create blob from the frame ti scale the pixel values and converts to a standardized format
        Mat blob = Dnn.blobFromImage(frame, 1.0 / 255.0, inputSize, new Scalar(0, 0, 0), true, false);
        //set the blob as input to the model
        net.setInput(blob);

        //initialize variables to collect outputs from the network
        List<Mat> outputs = new ArrayList<>();
        List<String> outNames = new ArrayList<>();
        outNames.add("output");
        //Run forward pass of neural network and capture outputs
        net.forward(outputs, outNames);

        //Reshape the output to a more managable form. Each row represents a car detection.
        Mat detections = outputs.get(0).reshape(1, (int)outputs.get(0).total() / 85);

        //Iterate through all the detections
        for (int i = 0; i < detections.rows(); i++) {
            //Confidence score for the detection is in index 4
            double confidence = detections.get(i, 4)[0];
            if (confidence > confThreshold) {
                //Initialize the best lass score and the class ID.
                int classId = -1;
                double maxClassScore = 0;

                //Iterate over the class scores (in index 5) 
                for (int j = 5; j < 85; j++) {
                    double score = detections.get(i, j)[0];
                    if (score > maxClassScore) {
                        maxClassScore = score;
                        classId = j - 5; //Adjust index offset as first five values are bbox data
                    }
                }
                //Check if the detection is classified as a car and the lass score exceeds the threshold
                if (classId == 2 && maxClassScore > confThreshold) { // class 2 = car
                    //Extract bounding box coordinates relative to the frame dimensions
                    double cx = detections.get(i, 0)[0] * frame.cols();
                    double cy = detections.get(i, 1)[0] * frame.rows();
                    double w = detections.get(i, 2)[0] * frame.cols();
                    double h = detections.get(i, 3)[0] * frame.rows();

                    //Convert the center coordinates to the top-left coordinates to draw the rectangle.
                    int left = (int)(cx - w / 2);
                    int top = (int)(cy - h / 2);
                    //add the bounding box to the list of detections
                    lastDetections.add(new Rect(left, top, (int)w, (int)h));
                }
            }
        }

        //return true if at least one car is detected, false otherwise
        return !lastDetections.isEmpty();
    }

    /*
    Overload method that attempts to detect cars on the last frame processed.
    Helps reprocess the frame and stores it if needed.
    */
    @Override
    public boolean detectCars() {
        if (lastFrame == null) return false;
        return detectCars(lastFrame);
    }

    /*
    Gets the number of cars detected in the most recent detection pass.
    returns the count of detected cars.
    */
    @Override
    public int getCarCount() {
        return lastDetections.size();
    }

    /*
    Returns an array of Rectangle objects that represent the detected car bounding boxes.
    */
    @Override
    public Rect[] getCars() {
        return lastDetections.toArray(new Rect[0]);
    }
}
