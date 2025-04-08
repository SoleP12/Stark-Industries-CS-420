package tello;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private boolean detectCars = false;
    private boolean useKeyboard = false;

    public void execute() throws Exception {
        int leftX, leftY, rightX, rightY, deadZone = 10;
        boolean found = false;

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

        Thread keyboardThread = new Thread(() -> listenForKeyboard());
        if (useKeyboard) keyboardThread.start();

        try {
            telloControl.connect();
            telloControl.enterCommandMode();
            telloControl.startStatusMonitor();
            telloControl.streamOn();

            camera.setStatusBar(this::updateWindow);
            camera.startVideoCapture(true);

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
                    detectCars = !detectCars;
                    if (!detectCars) camera.addTarget(null);
                }

                if (detectCars) {
                    found = carDetector.detectCars();
                    if (found) {
                        Rect[] cars = carDetector.getCars();
                        camera.addTarget(null);
                        for (Rect car : cars) {
                            if (car.width >= 250 && car.height >= 80) {
                                camera.addTarget(car, 2, new Scalar(255, 255, 0));
                            }
                        }
                    } else {
                        camera.addTarget(null);
                    }
                    camera.setStatusBar("Cars detected: " + carDetector.getCarCount());
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
                detectCars);
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
                        detectCars = !detectCars;
                        if (!detectCars) camera.addTarget(null);
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
