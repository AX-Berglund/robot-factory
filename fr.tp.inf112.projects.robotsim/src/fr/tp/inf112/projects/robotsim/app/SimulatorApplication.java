package fr.tp.inf112.projects.robotsim.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import fr.tp.inf112.projects.canvas.view.CanvasViewer;
import fr.tp.inf112.projects.robotsim.model.*;
import fr.tp.inf112.projects.robotsim.model.path.*;
import fr.tp.inf112.projects.robotsim.model.shapes.*;
import fr.tp.inf112.projects.robotsim.client.RemoteFactoryPersistenceManager;

public class SimulatorApplication {
    private static final Logger LOGGER = Logger.getLogger(SimulatorApplication.class.getName());

    // Singleton pattern to ensure the factory is initialized only once
    private static Factory factoryInstance = null;

    public static void main(String[] args) {
        // Log startup
        LOGGER.info("Starting the robot simulator...");
        LOGGER.config("With parameters: " + Arrays.toString(args) + ".");

        try {
            LOGGER.info("Initializing factory...");
            // Get or create the factory instance
            final Factory factory = getFactory();
            LOGGER.info("Factory initialized: " + factory);

            // Setup the remote persistence manager
            final RemoteFactoryPersistenceManager remotePersistenceManager =
                new RemoteFactoryPersistenceManager("localhost", 50002);

            // Launch the CanvasViewer
            SwingUtilities.invokeLater(() -> {
                try {
                    // Instantiate and display the CanvasViewer
                    new CanvasViewer(new SimulatorController(factory, remotePersistenceManager));
                    LOGGER.info("Simulator launched successfully.");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to launch the simulator.", e);
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during simulator setup.", e);
        }
    }

    // Factory instance getter with singleton pattern
    public static Factory getFactory() {
        if (factoryInstance == null) {
            factoryInstance = initializeFactory();
        }
        return factoryInstance;
    }

    public static Factory initializeFactory() {
        // Create the factory and its rooms, areas, and components
        LOGGER.info("Creating factory instance...");
        final Factory factory = new Factory(200, 200, "Simple Test Puck Factory");

        LOGGER.info("Adding components to factory...");

        // Production room 1
        final Room room1 = new Room(factory, new RectangularShape(20, 20, 75, 75), "Production Room 1");
        new Door(room1, Room.WALL.BOTTOM, 10, 20, true, "Entrance");
        final Area area1 = new Area(room1, new RectangularShape(35, 35, 50, 50), "Production Area 1");
        final Machine machine1 = new Machine(area1, new RectangularShape(50, 50, 15, 15), "Machine 1");

        // Production room 2
        final Room room2 = new Room(factory, new RectangularShape(120, 22, 75, 75), "Production Room 2");
        new Door(room2, Room.WALL.LEFT, 10, 20, true, "Entrance");
        final Area area2 = new Area(room2, new RectangularShape(135, 35, 50, 50), "Production Area 1");
        final Machine machine2 = new Machine(area2, new RectangularShape(150, 50, 15, 15), "Machine 2");

        // Charging room
        final Room chargingRoom = new Room(factory, new RectangularShape(125, 125, 50, 50), "Charging Room");
        new Door(chargingRoom, Room.WALL.RIGHT, 10, 20, false, "Entrance");
        final ChargingStation chargingStation = new ChargingStation(factory, new RectangularShape(150, 145, 15, 15), "Charging Station");

        LOGGER.info("Adding robots and pathfinders...");

        // Robots and pathfinders
        final FactoryPathFinder jgraphPathFinder = new JGraphTDijkstraFactoryPathFinder(factory, 5);
        final Robot robot1 = new Robot(factory, jgraphPathFinder, new CircularShape(5, 5, 2), new Battery(10), "Robot 1");
        robot1.addTargetComponent(machine1);
        robot1.addTargetComponent(machine2);
        robot1.addTargetComponent(chargingStation);

        final FactoryPathFinder customPathFinder = new CustomDijkstraFactoryPathFinder(factory, 5);
        final Robot robot2 = new Robot(factory, customPathFinder, new CircularShape(45, 5, 2), new Battery(10), "Robot 2");
        robot2.addTargetComponent(machine1);
        robot2.addTargetComponent(machine2);
        robot2.addTargetComponent(chargingStation);

        LOGGER.info("Finalizing factory initialization...");
        factory.initialize(); // Mark factory as fully initialized
        return factory;
    }
    
    public void delete_all_remote_files() {
        try {
            System.out.println("Are you sure you want to delete all saved canvases? (yes/no)");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String confirmation = reader.readLine();

            if ("yes".equalsIgnoreCase(confirmation)) {
                System.out.println("All canvases deleted successfully.");
            } else {
                System.out.println("Operation canceled.");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
