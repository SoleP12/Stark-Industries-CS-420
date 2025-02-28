package tellolib.camera;

import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/*
 *car detection with OpenCV. Got confused with yolov5 into java so did something simple instead
 */
public class CarDetection implements CarDetectionInterface
{
    private final Logger logger = Logger.getLogger("Tello");
    
    // classifier for car detection
    private CascadeClassifier carCascade = new CascadeClassifier();

    // stored tfound cars in array
    private Rect[] carsArray = null;
    
    
    private CarDetection()
    {
        // path changes depending on device so change accordingly
    	//usually is the root directory
        // e.g. C:\whatever\to\ourproject 
    	// shouldn't need to be changed
        String basePath = System.getProperty("user.dir");
        
        // location of the cars.xml file i got from someone's git, but we might need to make our own?
        // edit code to correct directory
        String classifierPath = basePath + "\\src\\resources\\cars.xml";
        
        logger.finer("Car classifier path=" + classifierPath);
        
        boolean loaded = carCascade.load(classifierPath);
        if (!loaded) {
            logger.severe("Failed to load car cascade from " + classifierPath);
        }
        else {
            logger.info("Car cascade loaded from " + classifierPath);
        }
    }
    
    private static class SingletonHolder 
    {
        public static final CarDetection INSTANCE = new CarDetection();
    }
    
    /*
     *get the global instance of CarDetection
     *global CarDetection instance
     */
    public static CarDetection getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    /*
     *detect cars on the current image from the Tello camera
     */
    @Override
    public boolean detectCars()
    {
        Mat image = TelloCamera.getInstance().getImage();
        return detectCars(image);
    }

    /*
     *detect cars on a given Mat image
     */
    @Override
    public boolean detectCars(Mat image)
    {
        if (image == null || image.empty()) {
            return false;
        }
        
        MatOfRect cars = new MatOfRect();
        Mat grayFrame = new Mat();
        
        logger.finer("detectCars");
        
        //converts to grayscale
        Imgproc.cvtColor(image, grayFrame, Imgproc.COLOR_BGR2GRAY);
        //equalize histogram to improve detection in varying lightings might be an issue with our frames
        Imgproc.equalizeHist(grayFrame, grayFrame);
        
        //compute a minimum car size although this might be arbitrary ngl
        //basically like if 2% of the image height to take out tiny false positives
        int height = grayFrame.rows();
        int absoluteCarSize = (int) Math.round(height * 0.02f);
        
        // detect cars
        carCascade.detectMultiScale(
            grayFrame,
            cars,
            1.1,         // scaleFactor
            2,           // minNeighbors
            0 | Objdetect.CASCADE_SCALE_IMAGE,
            new Size(absoluteCarSize, absoluteCarSize),
            new Size()   // no max size may need to add maybe one
        );
        
        carsArray = cars.toArray();
        
        logger.finer("cars detected = " + carsArray.length);
        
        return carsArray.length > 0;
    }

    @Override
    public int getCarCount()
    {
        if (carsArray == null) return 0;
        return carsArray.length;
    }

    @Override
    public Rect[] getCars()
    {
        return carsArray;
    }
}
