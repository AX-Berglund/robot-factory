package com.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tp.inf112.projects.robotsim.model.Factory;

public class TestSerialization {
    public static void main(String[] args) {
        try {
            // Step 1: Create a Factory object
            Factory factory = new Factory(200, 200, "Test Factory");

            // Step 2: Add components to the Factory (optional for testing relationships)
            // factory.addComponent(new Room(...)); // Uncomment and add as needed

            // Step 3: Serialize the Factory object to JSON
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(factory);
            System.out.println("Serialized JSON:");
            System.out.println(json);

            // Step 4: Deserialize the JSON back to a Factory object
            Factory deserializedFactory = mapper.readValue(json, Factory.class);
            System.out.println("Deserialized Factory:");
            System.out.println(deserializedFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
