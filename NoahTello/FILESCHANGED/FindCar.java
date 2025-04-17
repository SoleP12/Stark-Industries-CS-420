package tello;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

import tellolib.camera.CarDetection;
import tellolib.camera.TelloCamera;
import tellolib.command.TelloFlip;
import tellolib.control.TelloControl;
import tellolib.drone.TelloDrone;

public class FindCar {
    private final Logger logger = Logger.getGlobal();

    private TelloControl telloControl;
    private TelloDrone drone;
    private TelloCamera camera;
    private ControllerManager controllers;

    private CarDetection carDetector;
    private AtomicBoolean detectCars = new AtomicBoolean(false);
    private AtomicReference<Mat> latestFrame = new AtomicReference<>();
    private boolean useKeyboard = false;

    public void execute() throws Exception {
        int leftX, leftY, rightX, rightY, deadZone = 10;

        logger.info("start");

        controllers = new ControllerManager();
        controllers.initSDLGamepad();

        ControllerState currState = controllers.getState(0);
        if (!currState.isConnected) {
            logger.info("controller not connected, enabling keyboard controls");
            useKeyboard = true;
        }

        telloControl = TelloControl.getInstance();
        drone = TelloDrone.getInstance();
        camera = TelloCamera.getInstance();
        carDetector = CarDetection.getInstance();

        telloControl.setLogLevel(Level.FINE);

        Thread keyboardThread = new Thread(this::listenForKeyboard);
        if (useKeyboard) keyboardThread.start();

        try {
            telloControl.connect();
            telloControl.enterCommandMode();
            telloControl.startStatusMonitor();
            telloControl.streamOn();
            Thread.sleep(1500); // Warm-up delay so drone sends valid SPS/PPS before decoding

            camera.setStatusBar(this::updateWindow);
            camera.setFrameConsumer(frame -> {
                if (detectCars.get()) {
                    latestFrame.set(frame.clone());
                }
            });
            camera.startVideoCapture(true);

            Thread detectionThread = new Thread(() -> {
                int frameCounter = 0;
                int frameSkip = 5;

                while (true) {
                    try {
                        Mat frame = latestFrame.getAndSet(null);
                        if (frame != null) {
                            frameCounter++;
                            if (frameCounter % frameSkip == 0) {
                                boolean found = carDetector.detectCars(frame);
                                if (found) {
                                    Rect[] cars = carDetector.getCars();
                                    camera.addTarget(null);
                                    for (Rect car : cars) {
                                        if (car.width >= 250 && car.height >= 80) {
                                            camera.addTarget(car, 2, new Scalar(255, 255, 0));
                                        }
                                    }
                                    camera.setStatusBar("Cars detected: " + carDetector.getCarCount());
                                } else {
                                    camera.addTarget(null);
                                    camera.setStatusBar("Cars detected: 0");
                                }
                            }
                            frame.release();
                        }
                        Thread.sleep(50);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            detectionThread.setDaemon(true);
            detectionThread.start();

            while (drone.isConnected()) {
                currState = controllers.getState(0);

                if (currState.backJustPressed) {
                    logger.info("back button");
                    if (drone.isFlying()) telloControl.land();
                    break;
                }

                if (currState.startJustPressed) {
                    logger.info("start button");
                    if (drone.isFlying()) telloControl.land();
                    else telloControl.takeOff();
                }

                if (currState.aJustPressed) {
                    camera.takePicture(System.getProperty("user.dir") + "\\Photos");
                }

                if (currState.bJustPressed) {
                    if (camera.isRecording()) camera.stopRecording();
                    else camera.startRecording(System.getProperty("user.dir") + "\\Photos");
                }

                if (currState.xJustPressed) {
                    detectCars.set(!detectCars.get());
                    if (!detectCars.get()) camera.addTarget(null);
                }

                if (drone.isFlying()) {
                    leftX = deadZone((int) (currState.leftStickX * 100.0), deadZone);
                    leftY = deadZone((int) (currState.leftStickY * 100.0), deadZone);
                    rightX = deadZone((int) (currState.rightStickX * 100.0), deadZone);
                    rightY = deadZone((int) (currState.rightStickY * 100.0), deadZone);

                    telloControl.flyRC(rightX, rightY, leftY, leftX);

                    if (currState.dpadUpJustPressed) telloControl.doFlip(TelloFlip.forward);
                    if (currState.yJustPressed) telloControl.stop();
                }

                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (drone.isConnected() && drone.isFlying()) {
                try {
                    telloControl.land();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            telloControl.disconnect();
        }

        logger.info("end");
    }

    private int deadZone(int value, int minValue) {
        if (Math.abs(value) < minValue) value = 0;
        return value;
    }

    private String updateWindow() {
        return String.format("Batt: %d  Alt: %d  Hdg: %d  Rdy: %b  CarDetect: %b",
                drone.getBattery(),
                drone.getHeight(),
                drone.getHeading(),
                drone.isFlying(),
                detectCars.get());
    }

    private void listenForKeyboard() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                switch (line) {
                    case "t":
                        telloControl.takeOff();
                        break;
                    case "l":
                        telloControl.land();
                        break;
                    case "c":
                        detectCars.set(!detectCars.get());
                        if (!detectCars.get()) camera.addTarget(null);
                        break;
                    case "a":
                        camera.takePicture(System.getProperty("user.dir") + "\\Photos");
                        break;
                    case "v":
                        if (camera.isRecording()) camera.stopRecording();
                        else camera.startRecording(System.getProperty("user.dir") + "\\Photos");
                        break;
                    case "f":
                        telloControl.doFlip(TelloFlip.forward);
                        break;
                    case "h":
                        telloControl.stop();
                        break;
                    default:
                        System.out.println("Unknown command. Use T/L/C/A/V/F/H");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
