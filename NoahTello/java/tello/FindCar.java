package tello;

//Import I/O classes
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

//Import OpenCV classes
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

//Import gamepad support (controller support)
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

//Import Tello lib for car detection and drone control
import tellolib.camera.CarDetection;
import tellolib.camera.TelloCamera;
import tellolib.command.TelloFlip;
import tellolib.control.TelloControl;
import tellolib.drone.TelloDrone;

public class FindCar {
    //Declare logger instance to log information
    private final Logger logger = Logger.getGlobal();

    //Drone control variables
    private TelloControl telloControl;
    private TelloDrone drone;
    private TelloCamera camera;
    private ControllerManager controllers;

    //Car detection variables
    private CarDetection carDetector;
    private boolean detectCars = false;
    private boolean useKeyboard = false;

    /*
    Main function to execute car detection program. 
    Initializes the controllers, drone, camera, and car detection
    Processes controller/keyboard inputs for the drone to listen and perform the action
    */
    public void execute() throws Exception {
        int leftX, leftY, rightX, rightY, deadZone = 10;
        boolean found = false;

        //Log start of program
        logger.info("start");

        //Initialize controller 
        controllers = new ControllerManager();
        controllers.initSDLGamepad();

        //Check for controller, if controller is NOT connected, switch to keyboard controls
        ControllerState currState = controllers.getState(0);
        if (!currState.isConnected) {
            logger.info("controller not connected, enabling keyboard controls");
            useKeyboard = true;
        }

        //Initialize instances of drone and the car detection components
        telloControl = TelloControl.getInstance();
        drone = TelloDrone.getInstance();
        camera = TelloCamera.getInstance();
        carDetector = CarDetection.getInstance();

        //set logging level for drone control
        telloControl.setLogLevel(Level.FINE);

        //Initialize keyboard listner
        Thread keyboardThread = new Thread(() -> listenForKeyboard());
        if (useKeyboard) keyboardThread.start();

        //try to connect the tello drone and turn camera on 
        try {
            telloControl.connect();
            telloControl.enterCommandMode();
            telloControl.startStatusMonitor();
            telloControl.streamOn();

            //start video and make the camera's status bar viewable
            camera.setStatusBar(this::updateWindow);
            camera.startVideoCapture(true);

            //Main loop to control the drone while it is connected
            while (drone.isConnected()) {
                currState = controllers.getState(0);

                if (currState.backJustPressed) {
                    logger.info("back button");
                    if (drone.isFlying()) telloControl.land(); //kill drone 
                    break;
                }

                // start button = takeoff / land
                if (currState.startJustPressed) {
                    logger.info("start button");
                    if (drone.isFlying()) telloControl.land();
                    else telloControl.takeOff();
                }

                /*
                Controls:
                A = Take picture
                B = Record video
                X = car detection on 
                Y = stop drone
                D-PAD FORWARD = front flip
                */
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

                //If the car detectio is on, perform the car detection operations.
                if (detectCars) {
                    found = carDetector.detectCars();
                    if (found) {
                        Rect[] cars = carDetector.getCars(); //Get detected cars as an array 
                        camera.addTarget(null);
                        for (Rect car : cars) { //Iterate through each detected car.
                    //Filter the car detection to only add cars if the dimensions meet the criteria.
                            if (car.width >= 250 && car.height >= 80) { 
                                //Highlight car in the camera view with thickness and color
                                camera.addTarget(car, 2, new Scalar(255, 255, 0));
                            }
                        }
                    } else {
                        //No cars found, remove previous targets detected.
                        camera.addTarget(null);
                    }
                    //Update the status bars to keep count of how many cars are detected.
                    camera.setStatusBar("Cars detected: " + carDetector.getCarCount());
                }

                if (drone.isFlying()) {
                    //Deadzone calculations to filter small input movements
                    leftX = deadZone((int) (currState.leftStickX * 100.0), deadZone);
                    leftY = deadZone((int) (currState.leftStickY * 100.0), deadZone);
                    rightX = deadZone((int) (currState.rightStickX * 100.0), deadZone);
                    rightY = deadZone((int) (currState.rightStickY * 100.0), deadZone);

                    telloControl.flyRC(rightX, rightY, leftY, leftX);

                    if (currState.dpadUpJustPressed) telloControl.doFlip(TelloFlip.forward);
                    if (currState.yJustPressed) telloControl.stop();
                }

                //Wait 1 second before next iteration for responsive control
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace(); //print stack trace errors
        } finally {
            //ensure drone lands safely on exit
            if (drone.isConnected() && drone.isFlying()) {
                try {
                    telloControl.land();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            telloControl.disconnect(); //disconnect drone 
        }

        logger.info("end"); //Log end of the execution
    }
    /*
    Applies the deadzones to joystick for no stick drift from controller
    If the abs value is less than the minimum threshhold, return 0
    */

    private int deadZone(int value, int minValue) {
        if (Math.abs(value) < minValue) value = 0;
        return value;
    }

    /*
    Updates the view window for the user to see.
    Updates the following:
        Battery, Altitude, Heading, Flight Status, and num of Cars detected.
    */
    private String updateWindow() {
        return String.format("Batt: %d  Alt: %d  Hdg: %d  Rdy: %b  CarDetect: %b",
                drone.getBattery(),
                drone.getHeight(),
                drone.getHeading(),
                drone.isFlying(),
                detectCars);
    }
    /*
    Keyboard listner if Controller does not work.
    Below is the mapping for how to control the drone with the keyboard:

    t = takeoff
    l = land
    c = car detection on/off
    a = take picture
    v = video start/stop recording
    f = front flip
    h = stop the drone
    anything else: throws error for an unknown command.
    */

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
            e.printStackTrace(); //print stack trace for any exception in reading inputs
        }
    }
}
