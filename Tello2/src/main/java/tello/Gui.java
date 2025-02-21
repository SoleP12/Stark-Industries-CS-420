package tello;

/*
 * FIXES:
 * 1.) Put Camera in Frame
 * 2.) Fix GUI to make it look better
 * 3.) Camera and Button inputs are laggy (can i fix???)
 * 
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import tellolib.camera.TelloCamera;
import tellolib.control.TelloControl;
import tellolib.drone.TelloDrone;

public class Gui extends JFrame implements KeyListener {
    private final Logger logger = Logger.getGlobal(); 

    //Instances for drone control,camera,and status
    private TelloControl telloControl;
    private TelloDrone drone;
    private TelloCamera camera;
    private boolean isFlying = false; //Track if drone in air

    //GUI components
    private JLabel statusLabel;
    private JLabel instructionLabel;
    private JPanel videoPanel;

    public Gui() {
        logger.info("Initializing Tello Drone Controller...");


        //Get instances of drone control and camera system
        telloControl = TelloControl.getInstance();
        drone = TelloDrone.getInstance();
        camera = TelloCamera.getInstance();

        //Set log to debug
        telloControl.setLogLevel(Level.FINE);

        try {
        	//Connect drone
            telloControl.connect();
            telloControl.enterCommandMode();
            telloControl.startStatusMonitor();
            
            //Turn video stream on
            telloControl.streamOn();
            camera.startVideoCapture(true);
            
            //Record video
            camera.startRecording(System.getProperty("user.dir") + "\\Photos");

            //Setup the GUI to display video/controls
            setupUI();
            

            logger.info("Ready for keyboard input...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Setup GUI for drone Controller
    
    private void setupUI() {
        setTitle("Traffic Bot Controller");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Video feed panel DOES NOT WORK YET
        videoPanel = new JPanel();
        videoPanel.setBackground(Color.BLACK);
        videoPanel.setPreferredSize(new Dimension(640, 480));
        
        //NEEDS TO SET VIDEO IN CONTAINER
   

        // Status bar that shows battery, altitude, and heading
        statusLabel = new JLabel("Battery: --  Altitude: --  Heading: --", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(statusLabel, BorderLayout.SOUTH);

        // Instruction panel for user to use the keyboard
        instructionLabel = new JLabel(
        	    "<html>" +
        	        "<div style='text-align:center; " +
        	        "font-family:Arial, sans-serif; " +
        	        "font-size:14px; " +
        	        "color:#FFFFFF; " +  // White text
        	        "background-color:#333333; " +  // Dark background
        	        "padding:10px; " +
        	        "border: 2px solid #00FFFF; " +  // Neon border
        	        "border-radius: 10px;'>" +
        	            "<h2>Camera View</h2>" +
        	            "<hr style='border:1px solid #00FFFF;'>" +
        	            "<h3>Controls</h3>" +
        	            "<b>W</b> = Forward &nbsp; <b>A</b> = Left &nbsp; <b>S</b> = Backward &nbsp; <b>D</b> = Right<br>" +
        	            "<b>↑</b> = Up &nbsp; <b>↓</b> = Down &nbsp; <b>←</b> = Rotate Left &nbsp; <b>→</b> = Rotate Right<br>" +
        	            "<b>Spacebar</b> = Takeoff / Land" +
        	        "</div>" +
        	    "</html>",
        	    SwingConstants.CENTER
        	);

        instructionLabel.setOpaque(true);
        add(instructionLabel,BorderLayout.SOUTH);

        //Enables the keyboard input for the controls
        addKeyListener(this);
        setFocusable(true);
        setVisible(true);
    }
    
    //Switch case to use keyboard input 

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_SPACE:
                    if (!isFlying) {
                    	for(int i=0; i<3;i++) {
                    		try {
                    			logger.info("Attempting Takeoff....");
                    			telloControl.takeOff();
                    			isFlying = true;
                    			break;
                    		} catch (Exception ex) {
                    			logger.warning("Takeoff failed, retrying..."+(i+1));
                    			Thread.sleep(500); //Delay before retrying to takeoff
                    		}
                    	}
                        telloControl.takeOff();
                        isFlying = true;
                    } else {
                        telloControl.land();
                        isFlying = false;
                    }
                    break;
                case KeyEvent.VK_W: telloControl.forward(50); break;
                case KeyEvent.VK_S: telloControl.backward(50); break;
                case KeyEvent.VK_A: telloControl.left(50); break;
                case KeyEvent.VK_D: telloControl.right(50); break;
                case KeyEvent.VK_UP: telloControl.up(50); break;
                case KeyEvent.VK_DOWN: telloControl.down(50); break;
                case KeyEvent.VK_LEFT: telloControl.rotateLeft(45); break;
                case KeyEvent.VK_RIGHT: telloControl.rotateRight(45); break;
                case KeyEvent.VK_P: 
                    camera.takePicture(System.getProperty("user.dir") + "\\Photos");
                    logger.info("Picture taken!");
                    break;
                case KeyEvent.VK_ESCAPE:
                    if (isFlying) telloControl.land();
                    telloControl.disconnect();
                    System.exit(0);
                    break;
            }
            
            updateStatus();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    //Methods required for KeyListner to work but are not used

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    //Updates status bar with the latest drone info.
    private void updateStatus() {
        statusLabel.setText(String.format("Battery: %d%%  Altitude: %dcm  Heading: %d°", 
            drone.getBattery(), drone.getHeight(), drone.getHeading()));
    }

}