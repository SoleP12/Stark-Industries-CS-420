import org.opencv.core.*;
import org.opencv.dnn.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

public class CarDetectionTEST {
    public static void main(String[] args) {
        // 1. Load native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 2. Load the YOLOv5s ONNX model
        String modelPath = "yolov5s.onnx";
        Net net = Dnn.readNetFromONNX(modelPath);

        // 3. Open the default camera
        VideoCapture cap = new VideoCapture(0);
        if (!cap.isOpened()) {
            System.err.println("❌ Cannot open camera");
            return;
        }

        String windowName = "YOLOv5 Car Detection";
        HighGui.namedWindow(windowName, HighGui.WINDOW_NORMAL);
        System.out.println("🕵️‍♀️ Starting car detection. Close the window to stop.");

        // 4. Detection loop
        Mat frame = new Mat();
        while (true) {
            if (!cap.read(frame) || frame.empty()) {
                System.err.println("⚠️  Can't receive frame. Exiting...");
                break;
            }

            // 4a. Pre‑process: resize & normalize
            Size inpSize = new Size(640, 640);
            Mat blob = Dnn.blobFromImage(frame, 1 / 255.0, inpSize,
                                         new Scalar(0, 0, 0), /* swapRB */ true, /* crop */ false);
            net.setInput(blob);

            // 4b. Forward pass
            List<Mat> outputs = new ArrayList<>();
            List<String> outNames = net.getUnconnectedOutLayersNames();
            net.forward(outputs, outNames);

            // 4c. Parse detections (YOLOv5 outputs a single [1×N×85] tensor)
            Mat det = outputs.get(0); // shape: [1, 25200, 85] for yolov5s
            float confThreshold = 0.5f;
            List<Rect> boxes = new ArrayList<>();
            List<Float> confidences = new ArrayList<>();

            int rows = det.size(1);
            int cols = det.size(2);
            // iterate rows = number of detections
            for (int i = 0; i < rows; i++) {
                float[] data = new float[cols];
                det.get(0, i, data);
                float objectness = data[4];
                if (objectness < confThreshold) continue;

                // find best class
                float maxClassScore = -1;
                int classId = -1;
                for (int c = 5; c < cols; c++) {
                    if (data[c] > maxClassScore) {
                        maxClassScore = data[c];
                        classId = c - 5;
                    }
                }
                float score = objectness * maxClassScore;
                if (score < confThreshold) continue;
                if (classId != 2) continue;  // only keep “car”

                // decode box (center x,y,width,height)
                float cx = data[0], cy = data[1], w = data[2], h = data[3];
                int x = (int) ((cx - w / 2) * frame.cols());
                int y = (int) ((cy - h / 2) * frame.rows());
                int width  = (int) (w * frame.cols());
                int height = (int) (h * frame.rows());

                boxes.add(new Rect(x, y, width, height));
                confidences.add(score);
            }

            // 4d. Apply Non‑Maximum Suppression
            MatOfRect matBoxes = new MatOfRect();
            matBoxes.fromList(boxes);
            MatOfFloat matScores = new MatOfFloat();
            matScores.fromList(confidences);
            MatOfInt indices = new MatOfInt();
            Dnn.NMSBoxes(matBoxes, matScores, confThreshold, 0.4f, indices);

            // 4e. Draw final boxes
            for (int idx : indices.toArray()) {
                Rect box = boxes.get(idx);
                float conf = confidences.get(idx);
                Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 255, 0), 2);
                Imgproc.putText(frame,
                                String.format("Car %.2f", conf),
                                new Point(box.x, box.y - 5),
                                Imgproc.FONT_HERSHEY_SIMPLEX,
                                0.6, new Scalar(0, 255, 0), 2);
            }

            // 4f. Show
            HighGui.imshow(windowName, frame);
            // exit if window closed
            if (HighGui.getWindowProperty(windowName, HighGui.WND_PROP_VISIBLE) < 1)
                break;

            HighGui.waitKey(1);
        }

        cap.release();
        HighGui.destroyAllWindows();
    }
}