package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tp.inf112.projects.robotsim.model.Position;

import java.util.List;

public class TestPositionSerialization {
    public static void main(String[] args) {
        try {
            // Create the Jackson ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            // 1. Test Basic Serialization
            Position position = new Position(10, 15);
            String jsonString = objectMapper.writeValueAsString(position);
            System.out.println("Serialized JSON: " + jsonString);

            // 2. Test Basic Deserialization
            Position deserializedPosition = objectMapper.readValue(jsonString, Position.class);
            System.out.println("Deserialized Object: " + deserializedPosition);

            // 3. Test `getNeighbours` Logic (Not Serialized)
            List<Position> neighbours = position.getNeighbours();
            System.out.println("Neighbours: ");
            for (Position neighbour : neighbours) {
                System.out.println(neighbour);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
