package tellolib.camera;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.videoio.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import tellolib.communication.TelloConnection;
import tellolib.drone.TelloDrone;

/**
 * Convenience functions for Tello camera.
 */
public class TelloCamera implements TelloCameraInterface {
    private final Logger logger = Logger.getLogger("Tello");
    private boolean recording;
    private Thread videoCaptureThread;
    private VideoCapture camera;
    private Mat image;
    private VideoWriter videoWriter;
    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private Size videoFrameSize = new Size(screenSize.width - 400, screenSize.height - 100);
    private double videoFrameRate = 30;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd.HHmmss");
    private JFrame jFrame;
    private JLabel jLabel;
    private String statusBar = null;
    private Supplier<String> statusBarMethod = null;
    private Object lockObj = new Object();

    private ArrayList<Rect> targetRectangles;
    private ArrayList<MatOfPoint> contours = null;
    private Scalar targetColor = new Scalar(0, 0, 255), contourColor = new Scalar(255, 0, 0);
    private int targetWidth = 1, contourWidth = 1;

    private TelloCamera() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(Core.getBuildInformation());
        image = new Mat();
    }

    private static class SingletonHolder {
        public static final TelloCamera INSTANCE = new TelloCamera();
    }

    public static TelloCamera getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public void startVideoCapture(boolean liveWindow) {
        logger.fine("starting video capture");

        if (camera != null) return;

        camera = new VideoCapture();
        camera.setExceptionMode(true);

        String streamUrl = "udp://0.0.0.0:" + TelloDrone.UDP_VIDEO_PORT;
        camera.open(streamUrl, Videoio.CAP_FFMPEG);

        if (!camera.isOpened()) {
            logger.severe("❌ Failed to open video stream. Check FFMPEG DLL and WiFi connection.");
            return;
        }

        logger.fine("video camera open:" + camera.isOpened());

        if (liveWindow) {
            jFrame = new JFrame("Tello Controller Test");
            jFrame.setPreferredSize(new Dimension((int) videoFrameSize.width, (int) videoFrameSize.height));
            jLabel = new JLabel();
            jFrame.getContentPane().add(jLabel);
            jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            jFrame.pack();
            jFrame.setVisible(true);
        }

        videoCaptureThread = new VideoCaptureThread();
        videoCaptureThread.start();
    }

    @Override
    public void stopVideoCapture() {
        if (camera == null) return;
        if (recording) stopRecording();

        if (videoCaptureThread != null) {
            logger.fine("stopping video capture thread");
            try {
                videoCaptureThread.interrupt();
                videoCaptureThread.join(2000);
                logger.fine("after join");
            } catch (Exception e) { e.printStackTrace(); }
        }

        if (jFrame != null) {
            jFrame.setVisible(false);
            jFrame.dispose();
        }

        camera.release();
        image.release();
        image = null;
        camera = null;
    }

    @Override
    public Mat getImage() {
        synchronized (lockObj) {
            if (image == null) return null;
            else return image.clone();
        }
    }

    private class VideoCaptureThread extends Thread {
        VideoCaptureThread() {
            logger.fine("video thread constructor");
            this.setName("VideoCapture");
        }

        public void run() {
            Mat imageRaw = new Mat();
            logger.fine("video capture thread started");

            try {
                while (!isInterrupted()) {
                    camera.read(imageRaw);
                    synchronized (lockObj) {
                        Imgproc.resize(imageRaw, image, videoFrameSize);

                        if (targetRectangles != null)
                            for (Rect rect : targetRectangles)
                                Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), targetColor, targetWidth);

                        if (contours != null) Imgproc.drawContours(image, contours, -1, contourColor, contourWidth);

                        if (statusBar != null && statusBarMethod == null)
                            Imgproc.putText(image, statusBar, new Point(0, image.height() - 25), Imgproc.FONT_HERSHEY_PLAIN,
                                    1.5, new Scalar(255, 255, 255), 2, Imgproc.FILLED);

                        if (statusBarMethod != null)
                            Imgproc.putText(image, statusBarMethod.get(), new Point(0, image.height() - 25), Imgproc.FONT_HERSHEY_PLAIN,
                                    1.5, new Scalar(255, 255, 255), 2, Imgproc.FILLED);
                    }

                    if (jFrame != null) updateLiveWindow(image);
                    if (recording) videoWriter.write(image);
                }
            } catch (Exception e) {
                logger.severe("video capture failed: " + e.getMessage());
                TelloDrone.getInstance().setConnection(TelloConnection.DISCONNECTED);
            }

            logger.fine("Video capture thread ended");
            videoCaptureThread = null;
        }
    }

    @Override
    public boolean takePicture(String folder) {
        String fileName;
        boolean result = false;
        Mat img = getImage();

        if (camera == null) {
            logger.warning("No video stream");
            return false;
        }

        if (img != null && !img.empty()) {
            fileName = folder + "\\" + df.format(new Date()) + ".jpg";
            logger.info("h=" + img.height() + ";w=" + img.width());

            if (Imgcodecs.imwrite(fileName, img)) {
                logger.fine("Picture saved to " + fileName);
                result = true;
            } else {
                logger.warning("Picture file save failed");
            }
        } else {
            logger.warning("Take Picture failed: image not available");
        }

        return result;
    }

    private void updateLiveWindow(Mat image) {
        try {
            videoFrameSize = new Size(jFrame.getWidth(), jFrame.getHeight());
            Image img = HighGui.toBufferedImage(image);
            jLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            logger.warning("live window update failed: " + e.toString());
        }
    }

    @Override
    public boolean startRecording(String folder) {
        if (camera == null) {
            logger.warning("No video stream");
            return false;
        }

        String fileName = folder + "\\" + df.format(new Date()) + ".avi";
        videoWriter = new VideoWriter(fileName, VideoWriter.fourcc('M', 'J', 'P', 'G'), videoFrameRate, videoFrameSize, true);

        if (videoWriter != null && videoWriter.isOpened()) {
            recording = true;
            logger.fine("Video recording started to " + fileName);
            return true;
        } else {
            logger.warning("Video recording failed");
            return false;
        }
    }

    @Override
    public void stopRecording() {
        if (camera == null || !recording) return;
        recording = false;
        videoWriter.release();
        logger.fine("Video recording stopped");
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    @Override
    public void addTarget(Rect target) {
        synchronized (lockObj) {
            if (target == null) {
                targetRectangles = null;
                return;
            }

            if (targetRectangles == null) targetRectangles = new ArrayList<>();
            targetRectangles.add(target);
        }
    }

    @Override
    public void addTarget(Rect target, int width, Scalar color) {
        targetWidth = width;
        targetColor = color;
        addTarget(target);
    }

    @Override
    public void setContours(ArrayList<MatOfPoint> contours) {
        synchronized (lockObj) {
            this.contours = contours;
        }
    }

    @Override
    public void setContours(ArrayList<MatOfPoint> contours, int width, Scalar color) {
        contourWidth = width;
        contourColor = color;
        setContours(contours);
    }

    public Size getImageSize() {
        synchronized (lockObj) {
            if (image == null) return new Size(0, 0);
            return new Size(image.width(), image.height());
        }
    }

    @Override
    public void setStatusBar(String message) {
        synchronized (lockObj) {
            statusBar = message;
        }
    }

    @Override
    public void setStatusBar(Supplier<String> method) {
        synchronized (lockObj) {
            statusBarMethod = method;
        }
    }

    @Override
    public void setVideoFrameSize(int width, int height) {
        videoFrameSize = new Size(width, height);
    }
}
