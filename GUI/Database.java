import java.util.HashSet;
import java.util.Set;

/*
 * Database for the simulation, used to keep track of car IDs.
 * This is a singleton class to ensure only one instance exists.
 * It provides methods to add car IDs and check the size of the database.
 */
public class Database {
    // Singleton instance
    private static Database instance;
    private final Set<Integer> carIDs = new HashSet<>();

    private Database() {
        // private constructor for singleton
    }

    // Method to get the singleton instance of Database
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    // Method to clear the car IDs in the database
    public static void clearCars() {
        if (instance != null) {
            instance.carIDs.clear();
        }
    }
    
    // Method to add a car ID to the database
    public boolean addCar(int carID) {
        if (carIDs.contains(carID)) {
            return false;
        }
        carIDs.add(carID);
        System.out.println("Car " + carID + " added to database.");
        return true;
    }

    //Method to get the size of the database
    // Returns the number of unique car IDs in the database
    public int getSize() {
        return carIDs.size();
    }
}