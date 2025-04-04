package tello;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Rect;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

import tellolib.camera.CarDetection;
import tellolib.camera.TelloCamera;
import tellolib.command.TelloFlip;
import tellolib.control.TelloControl;
import tellolib.drone.TelloDrone;
import org.opencv.core.Scalar;

/*
 * based off of facedetection
 */
public class FindCar
{
    private final Logger logger = Logger.getGlobal(); 

    private TelloControl      telloControl;
    private TelloDrone        drone;
    private TelloCamera       camera;
    private ControllerManager controllers;
    
    //our CarDetection singleton
    private CarDetection      carDetector;
    
    //toggle car detection with a gamepad button
    private boolean           detectCars = false;
    
    public void execute() throws Exception
    {
        int     leftX, leftY, rightX, rightY, deadZone = 10;
        int     carCount;
        boolean found = false;

        logger.info("start");
        
        controllers = new ControllerManager();
        controllers.initSDLGamepad();
        
        ControllerState currState = controllers.getState(0);
        
        //if controller does not exist than it won't work 
        if (!currState.isConnected) throw new Exception("controller not connected");

        telloControl = TelloControl.getInstance();
        drone        = TelloDrone.getInstance();
        camera       = TelloCamera.getInstance();
        
        // creates instance of CarDetection
        carDetector  = CarDetection.getInstance();
                
        telloControl.setLogLevel(Level.FINE);
        
        // Controller mapping (copied and pasted FindFace, but if we can change if need be):
        // Start button = take off
        // Back button  = land
        // A button     = take picture
        // B button     = toggle video recording
        // X button     = toggle car detection
        // Y button     = stop, go into hover
        // Dpad.up      = flip forward
        //
        // right joystick Y axis = forward/backward
        // right joystick X axis = left/right
        //
        // left joystick Y axis  = up/down
        // left joystick X axis  = rotate left/right

        try 
        {
            telloControl.connect();
            telloControl.enterCommandMode();
            telloControl.startStatusMonitor();
            telloControl.streamOn();
            
            //shows drone status info at bottom of the video feed
            camera.setStatusBar(this::updateWindow);
            
            //start the live video feed
            camera.startVideoCapture(true);
            
            //loops until user lands drone (back button) or connection lost
            while(drone.isConnected()) 
            {
                //reads current state of the controller
                currState = controllers.getState(0);
                
                //back button lands drone & exits loop
                if (currState.backJustPressed) 
                {
                    logger.info("back button");
                    
                    if (drone.isFlying()) telloControl.land();
                    break;
                }
                
                //start button toggles takeoff/land
                if (currState.startJustPressed)
                {
                    logger.info("start button");
                    
                    if (drone.isFlying())
                        telloControl.land();
                    else
                        telloControl.takeOff();
                }

                // A button takes a picture
                if (currState.aJustPressed)
                {
                    camera.takePicture(System.getProperty("user.dir") + "\\Photos");
                }
                
                // B button toggles video recording
                if (currState.bJustPressed)
                {
                    if (camera.isRecording())
                        camera.stopRecording();
                    else
                        camera.startRecording(System.getProperty("user.dir") + "\\Photos");
                }

                // X button toggles car detection
                if (currState.xJustPressed)
                {
                    detectCars = !detectCars;
                    
                    //clear any target rectangles if car detection is off
                    if (!detectCars) camera.addTarget(null);
                }
                
                //if car detection is active, run it
                if (detectCars)
                {
                    found = carDetector.detectCars();
                    
                    if (found)
                    {
                        //retrieves the detected car bounding boxes
                        Rect[] cars = carDetector.getCars();
                        
                        // clear previous target drawings
                        camera.addTarget(null);
                        
                        // draw all detected cars (can be edited for just one)
                        for (Rect car : cars){
                        if (car.width >= 250 && car.height >= 80){
                            camera.addTarget(car, 2, new Scalar(255, 255, 0));
                        }
                    }
                    }
                    else
                    {
                        //clear if no cars found
                        camera.addTarget(null);
                    }
                    
                    // updates status info
                    camera.setStatusBar("Cars detected: " + carDetector.getCarCount());
                }

                
                // if flying, pass the controller joystick deflection to the drone
                if (drone.isFlying())
                {
                    leftX  = deadZone((int) (currState.leftStickX  * 100.0), deadZone);
                    leftY  = deadZone((int) (currState.leftStickY  * 100.0), deadZone);
                    rightX = deadZone((int) (currState.rightStickX * 100.0), deadZone);
                    rightY = deadZone((int) (currState.rightStickY * 100.0), deadZone);
                    
                    //                   L/R     F/B    U/D    YAW
                    telloControl.flyRC(rightX, rightY, leftY, leftX);
                    
                    // Dpad.up = flip forward
                    if (currState.dpadUpJustPressed) telloControl.doFlip(TelloFlip.forward);
                    
                    // Y button = stop/hover
                    if (currState.yJustPressed) telloControl.stop();
                }
                
                Thread.sleep(100); // around 10 updates per second can change prob
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        } 
        finally 
        {
            if (drone.isConnected() && drone.isFlying())
            {
                try
                {
                    telloControl.land();
                }
                catch(Exception e) 
                {
                    e.printStackTrace();
                }
            }
            telloControl.disconnect();
        }
        
        logger.info("end");
    }
    
    // applies a dead zone to the input. Input below min value forced to zero
    private int deadZone(int value, int minValue)
    {
        if (Math.abs(value) < minValue) value = 0;
        return value;
    }   
    
    // returns a string of info for the status area on video feed.
    private String updateWindow()
    {
        return String.format("Batt: %d  Alt: %d  Hdg: %d  Rdy: %b  CarDetect: %b",
                drone.getBattery(), 
                drone.getHeight(), 
                drone.getHeading(), 
                drone.isFlying(), 
                detectCars);
    }
    
}
