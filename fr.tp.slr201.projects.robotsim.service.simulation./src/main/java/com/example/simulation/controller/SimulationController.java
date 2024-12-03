package com.example.simulation.controller;
//package fr.tp.slr201.projects.robotsim.service.simulation;
//package fr.tp.slr201.projects.robotsim.service.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.tp.inf112.projects.robotsim.model.Factory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/simulation")
public class SimulationController {

    private static final Logger logger = LoggerFactory.getLogger(SimulationController.class);
    private final ConcurrentMap<String, Factory> simulatedModels = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String PERSISTENCE_SERVER_URL = "http://localhost:50001";

    @PostMapping("/start/{id}")
    public boolean startSimulation(@PathVariable String id) {
        try {
            // Fetch factory model from persistence server
            Factory factory = retrieveFactoryModelFromPersistence(id);
            if (factory == null) {
                logger.error("Factory model with ID {} not found", id);
                return false;
            }

            // Add to simulated models and start simulation
            simulatedModels.put(id, factory);
            factory.startSimulation(); // Assuming Factory has a `startSimulation` method
            logger.info("Started simulation for factory model with ID {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Failed to start simulation for factory model with ID {}", id, e);
            return false;
        }
    }

    @GetMapping("/retrieve/{id}")
    public Factory retrieveSimulatedModel(@PathVariable String id) {
        return simulatedModels.get(id);
    }

    @DeleteMapping("/stop/{id}")
    public boolean stopSimulation(@PathVariable String id) {
        try {
            Factory factory = simulatedModels.remove(id);
            if (factory == null) {
                logger.warn("Factory model with ID {} not found", id);
                return false;
            }
            factory.stopSimulation(); // Assuming Factory has a `stopSimulation` method
            logger.info("Stopped simulation for factory model with ID {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Failed to stop simulation for factory model with ID {}", id, e);
            return false;
        }
    }

    private Factory retrieveFactoryModelFromPersistence(String id) {
        try {
            URI uri = new URI(PERSISTENCE_SERVER_URL + "/retrieve/" + id);
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new ObjectMapper().readValue(response.body(), Factory.class);
            } else {
                logger.error("Failed to fetch factory model: HTTP {}", response.statusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error retrieving factory model from persistence server", e);
            return null;
        }
    }
    
    @GetMapping("/test")
    public String test() {
        return "Controller is working!";
    }

    @ControllerAdvice
    public class CustomErrorHandler {

        @ExceptionHandler(Exception.class)
        @ResponseBody
        public String handleError(Exception ex) {
            return "Custom Error: " + ex.getMessage();
        }
    }

}

