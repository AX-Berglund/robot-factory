package fr.tp.inf112.projects.robotsim.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.robotsim.model.Area;
import fr.tp.inf112.projects.robotsim.model.Battery;
import fr.tp.inf112.projects.robotsim.model.Door;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.Machine;
import fr.tp.inf112.projects.robotsim.model.Robot;
import fr.tp.inf112.projects.robotsim.model.Room;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class TestRobotSimSerializationJSON {

    private static final Logger LOGGER = Logger.getLogger(TestRobotSimSerializationJSON.class.getName());

    private final ObjectMapper objectMapper;
    
    private static String factoryAsJsonString; // Class-level variable to share JSON between tests
    private static Factory factory;

    public TestRobotSimSerializationJSON() {
        // Initialize the ObjectMapper with polymorphic type handling
        objectMapper = new ObjectMapper();
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        	    .allowIfSubType("fr.tp.inf112.projects.robotsim.model") // Include the package for Position
        	    .allowIfSubType("java.util")
        	    .build();
        	objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

    }

    // Helper method to create a factory model
    private Factory createFactoryModel() {
        LOGGER.fine("Creating Factory model for testing...");
        final Factory factory = new Factory(200, 200, "Simple Test Puck Factory");

        try {
            final Room room1 = new Room(factory, new RectangularShape(20, 20, 75, 75), "Production Room 1");
            LOGGER.fine("Created Room: " + room1);

            final Door door = new Door(room1, Room.WALL.BOTTOM, 10, 20, true, "Entrance");
            LOGGER.fine("Created Door: " + door);

            final Area area1 = new Area(room1, new RectangularShape(35, 35, 50, 50), "Production Area 1");
            LOGGER.fine("Created Area: " + area1);

            final Machine machine1 = new Machine(area1, new RectangularShape(50, 50, 15, 15), "Machine 1");
            LOGGER.fine("Created Machine: " + machine1);

            final FactoryPathFinder jgraphPathFinder = new JGraphTDijkstraFactoryPathFinder(factory, 5);
            final Robot robot1 = new Robot(factory, jgraphPathFinder, new CircularShape(5, 5, 2), new Battery(10), "Robot 1");
            robot1.addTargetComponent(machine1);
            LOGGER.fine("Created Robot: " + robot1);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while creating Factory model", e);
        }

        return factory;
    }
    
    @Test
    public void test1_Serialization() {
        LOGGER.info("Starting serialization test...");
//        Factory myFactory = createFactoryModel();
        factory = createFactoryModel();
        try {
            // Serialize the factory to JSON
            factoryAsJsonString = objectMapper.writeValueAsString(factory);
            
//            LOGGER.info("Serialized JSON: " + factoryAsJsonString);
            LOGGER.info("\n");

            LOGGER.info("Serialized JSON String: " + factory + "\n");


            // Store JSON for deserialization test
            LOGGER.info("Serialization test completed successfully.\n");
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error during serialization", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error occurred during serialization", e);
        }
    }

    @Test
    public void test2_DeSerialization() {
        try {
            // Check if serialization was performed
            if (factoryAsJsonString == null) {
                throw new IllegalStateException("Serialized JSON is null. Ensure testSerialization runs first.");
            }
            LOGGER.info("Starting deserialization test...");

            // Deserialize the JSON back into a Factory object
            Factory roundTrip = objectMapper.readValue(factoryAsJsonString, Factory.class);

            LOGGER.info("Deserialized Factory  : " + roundTrip + "\n");

            // Create the original factory for comparison
            Factory originalFactory = createFactoryModel();
            LOGGER.info("Original Factory      : " + originalFactory + "\n");

            // Check for equality and log differences
            if (!originalFactory.equals(roundTrip)) {
                LOGGER.warning("The original and deserialized factories are not equal.");
                logDifferences(originalFactory, roundTrip);
            } else {
                LOGGER.info("Success: The original and deserialized factories are equal.");
            }

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error during deserialization", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error occurred during deserialization", e);
        }
    }

    /**
     * Logs differences between two Factory objects.
     */
    private void logDifferences(Factory original, Factory roundTrip) {
        try {
            // Convert objects to JSON strings for comparison
            ObjectMapper mapper = new ObjectMapper();
            String originalJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(original);
            String roundTripJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(roundTrip);

            LOGGER.info("Original Factory JSON:\n" + originalJson);
            LOGGER.info("Deserialized Factory JSON:\n" + roundTripJson);

            // Use JSON diff library or manual comparison
            JsonNode originalNode = mapper.readTree(originalJson);
            JsonNode roundTripNode = mapper.readTree(roundTripJson);

            // Compare nodes and log differences
            if (!originalNode.equals(roundTripNode)) {
                List<String> differences = findJsonDifferences(originalNode, roundTripNode);
                for (String diff : differences) {
                    LOGGER.warning("Difference: " + diff);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error comparing Factory objects", e);
        }
    }

    /**
     * Finds differences between two JsonNode objects.
     */
    private List<String> findJsonDifferences(JsonNode originalNode, JsonNode roundTripNode) {
        List<String> differences = new ArrayList<>();
        Iterator<String> fieldNames = originalNode.fieldNames();

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode originalValue = originalNode.get(fieldName);
            JsonNode roundTripValue = roundTripNode.get(fieldName);

            if (!originalValue.equals(roundTripValue)) {
                differences.add(String.format("Field '%s' differs: Original = %s, Deserialized = %s",
                        fieldName, originalValue, roundTripValue));
            }
        }
        return differences;
    }

}
