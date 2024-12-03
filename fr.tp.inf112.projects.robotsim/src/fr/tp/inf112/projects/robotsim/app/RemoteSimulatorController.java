package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URISyntaxException;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.canvas.controller.Observer;


public class RemoteSimulatorController extends SimulatorController {
    
    private static final String SIMULATION_SERVER_URL = "http://localhost:8080/simulation";
    private final HttpClient httpClient;
    private final String factoryId;

    public RemoteSimulatorController(String factoryId, CanvasPersistenceManager persistenceManager) {
        super(null, persistenceManager);
        this.httpClient = HttpClient.newHttpClient();
        this.factoryId = factoryId;
    }

    @Override
    public void startAnimation() {
        try {
            URI uri = new URI(SIMULATION_SERVER_URL + "/start/" + factoryId);
            HttpRequest request = HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && Boolean.parseBoolean(response.body())) {
                System.out.println("Simulation started successfully for factory ID: " + factoryId);
            } else {
                System.err.println("Failed to start simulation: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopAnimation() {
        try {
            URI uri = new URI(SIMULATION_SERVER_URL + "/stop/" + factoryId);
            HttpRequest request = HttpRequest.newBuilder(uri).DELETE().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && Boolean.parseBoolean(response.body())) {
                System.out.println("Simulation stopped successfully for factory ID: " + factoryId);
            } else {
                System.err.println("Failed to stop simulation: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateViewer() {
        try {
            while (isAnimationRunning()) {
                URI uri = new URI(SIMULATION_SERVER_URL + "/retrieve/" + factoryId);
                HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Factory remoteFactoryModel = new ObjectMapper().readValue(response.body(), Factory.class);
                    setCanvas(remoteFactoryModel);
                } else {
                    System.err.println("Failed to retrieve factory model: " + response.body());
                }
                Thread.sleep(100); // Polling interval
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCanvas(Canvas canvasModel) {
        super.setCanvas(canvasModel);
        List<Observer> observers = ((Factory) getCanvas()).getObservers();
        for (Observer observer : observers) {
            ((Factory) getCanvas()).addObserver(observer);
        }
        ((Factory) getCanvas()).notifyObservers();
    }
}

