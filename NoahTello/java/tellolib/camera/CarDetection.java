package tellolib.camera;

import ai.onnxruntime.*;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Purpose: Implementation of Car Detection using ONNX Runtime (YOLOv8s model)
 */
public class CarDetection implements CarDetectionInterface {
    private static CarDetection instance;
    private final YoloInterface yolo;
    private final float confThreshold = 0.5f;
    private List<Rect> lastDetections = new ArrayList<>();
    private Mat lastFrame = null;

    private CarDetection() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            yolo = new YoloInterface("src/resources/yolov8s.onnx");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YOLO model", e);
        }
    }

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

        // Resize and normalize image to [1, 3, 640, 640]
        Mat resized = new Mat();
        Imgproc.resize(frame, resized, new Size(640, 640));
        resized.convertTo(resized, CvType.CV_32F, 1.0 / 255.0);

        float[][][][] inputBlob = new float[1][3][640][640];
        for (int y = 0; y < 640; y++) {
            for (int x = 0; x < 640; x++) {
                double[] pixel = resized.get(y, x);
                inputBlob[0][0][y][x] = (float) pixel[2]; // R
                inputBlob[0][1][y][x] = (float) pixel[1]; // G
                inputBlob[0][2][y][x] = (float) pixel[0]; // B
            }
        }

        try {
            float[][][] detections = yolo.detect(inputBlob);
            for (float[] det : detections[0]) {
                float conf = det[4];
                if (conf > confThreshold) {
                    int classId = -1;
                    float maxScore = 0;
                    for (int j = 5; j < det.length; j++) {
                        if (det[j] > maxScore) {
                            maxScore = det[j];
                            classId = j - 5;
                        }
                    }
                    if (classId == 2 && maxScore > confThreshold) { // Class 2 = Car
                        int left = (int) ((det[0] - det[2] / 2) * frame.cols());
                        int top = (int) ((det[1] - det[3] / 2) * frame.rows());
                        int width = (int) (det[2] * frame.cols());
                        int height = (int) (det[3] * frame.rows());
                        lastDetections.add(new Rect(left, top, width, height));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return !lastDetections.isEmpty();
    }

    @Override
    public boolean detectCars() {
        return lastFrame != null && detectCars(lastFrame);
    }

    @Override
    public int getCarCount() {
        return lastDetections.size();
    }

    @Override
    public Rect[] getCars() {
        return lastDetections.toArray(new Rect[0]);
    }

    static class YoloInterface {
        private final OrtEnvironment env;
        private final OrtSession session;

        public YoloInterface(String modelPath) throws OrtException, IOException {
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(Files.readAllBytes(Paths.get(modelPath)), new OrtSession.SessionOptions());
        }

        public float[][][] detect(float[][][][] input) throws OrtException {
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, input);
            OrtSession.Result output = session.run(Collections.singletonMap(session.getInputNames().iterator().next(), inputTensor));
            float[][][] outputData = (float[][][]) output.get(0).getValue();
            inputTensor.close();
            output.close();
            return outputData;
        }

        public void close() throws OrtException {
            session.close();
            env.close();
        }
    }
}
