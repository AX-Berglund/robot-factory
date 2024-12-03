package fr.tp.inf112.projects.robotsim.client;

import fr.tp.inf112.projects.canvas.model.Canvas;


import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
//import fr.tp.inf112.projects.robotsim.model.Factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.core.JsonProcessingException;


import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.logging.Level;

public class RemoteFactoryPersistenceManager implements CanvasPersistenceManager {
    private static final Logger LOGGER = Logger.getLogger(RemoteFactoryPersistenceManager.class.getName());

    private final String serverAddress;
    private final int serverPort;
    private final CanvasChooser canvasChooser;
    private static final ObjectMapper objectMapper;

    // Enum to define the current implementation choice
    private enum PersistImplementation {
        OBJECT_STREAM,
        JSON_STREAM
    }

    // Set the desired implementation here
    private final PersistImplementation currentImplementation = PersistImplementation.OBJECT_STREAM;
//    private final PersistImplementation currentImplementation = PersistImplementation.JSON_STREAM;
    
    static {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("fr.tp.inf112.projects.robotsim.model")
                .allowIfSubType("fr.tp.inf112.projects.canvas.model") // If needed for Canvas subclasses
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    public RemoteFactoryPersistenceManager(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.canvasChooser = new RemoteFileCanvasChooser("localhost", serverPort, "Remote Factory Models", serverAddress);
    }

    @Override
    public CanvasChooser getCanvasChooser() {
        return canvasChooser;
    }

    @Override
    public void persist(Canvas canvas) {
        switch (currentImplementation) {
            case OBJECT_STREAM:
                persistUsingObjectStream(canvas);
                break;
            case JSON_STREAM:
                persistUsingJsonStream(canvas);
                break;
            default:
                LOGGER.warning("Unknown persist implementation selected.");
        }
    }

    // Implementation 1: Using Object Streams
    private void persistUsingObjectStream(Canvas canvas) {
        LOGGER.info("Persist method using Object Stream started.");
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            LOGGER.info("Socket connection established with server.");

            // Send the canvas to the server
            oos.writeObject(canvas);
            oos.flush();
            LOGGER.info("Factory model sent to server successfully.");

            // Read the server's response
            Object response = ois.readObject();
            if (response instanceof String) {
                LOGGER.info("Server response: " + response);
            } else {
                LOGGER.warning("Unexpected response type from server: " + response.getClass().getName());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during Object Stream persist operation.", e);
        }
    }

    // Implementation 2: Using JSON Streams
    private void persistUsingJsonStream(Canvas canvas) {
        LOGGER.info("Persisting factory model using JSON serialization...");
        try (Socket socket = new Socket(serverAddress, serverPort);
             OutputStream os = socket.getOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {

            // Serialize the Canvas object to JSON
            String json = objectMapper.writeValueAsString(canvas);
            LOGGER.info("JSON being sent to server: " + json);

            writer.write(json);
            writer.flush();
            LOGGER.info("Factory model serialized and sent to server.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during JSON serialization persist operation.", e);
        }
    }
    
    @Override
    public Canvas read(String id) {
        switch (currentImplementation) {
            case OBJECT_STREAM:
                return readUsingObjectStream(id);
            case JSON_STREAM:
                return readUsingJsonStream(id);
            default:
                LOGGER.warning("Unknown read implementation selected.");
                return null;
        }
    }

    // Implementation 1: Using Object Streams
    private Canvas readUsingObjectStream(String id) {
        LOGGER.info("Read method using Object Stream started.");
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            LOGGER.info("Socket connection established with server.");

            // Send the identifier to the server
            oos.writeObject(id);
            oos.flush();
            LOGGER.info("Identifier sent to server successfully.");

            // Read the server's response
            Object response = ois.readObject();
            if (response instanceof Canvas) {
                LOGGER.info("Canvas retrieved successfully: " + id);
                return (Canvas) response;
            } else {
                LOGGER.warning("Unexpected response type from server: " + response.getClass().getName());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during Object Stream read operation.", e);
        }
        return null;
    }

    // Implementation 2: Using JSON Streams
    private Canvas readUsingJsonStream(String id) {
        LOGGER.info("Read method using JSON Stream started.");
        try (Socket socket = new Socket(serverAddress, serverPort);
             OutputStream os = socket.getOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
             InputStream is = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            // Send the identifier to the server
            writer.write(id);
            writer.newLine(); // Ensure the server reads the end of the line
            writer.flush();
            LOGGER.info("Identifier sent to server successfully.");

            // Read the server's JSON response
            String jsonResponse = reader.readLine();
            LOGGER.info("Received JSON response from server.");

            // Deserialize the JSON into a Canvas object
            return objectMapper.readValue(jsonResponse, Canvas.class);

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error during JSON deserialization.", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IOException occurred during read operation.", e);
        }
        return null;
    }
    
    public void deleteAllCanvases() {
      try (Socket socket = new Socket(serverAddress, serverPort);
           ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
           ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

          LOGGER.info("Sending DELETE_ALL command to server.");
          oos.writeObject("DELETE_ALL");
          oos.flush();

          // Read server response
          Object response = ois.readObject();
          if (response instanceof String) {
              LOGGER.info("Server response: " + response);
          } else {
              LOGGER.warning("Unexpected response type from server: " + response.getClass().getName());
          }

      } catch (IOException | ClassNotFoundException e) {
          LOGGER.log(Level.SEVERE, "Error while sending DELETE_ALL command to server.", e);
      }
  }

	@Override
	public boolean delete(Canvas canvasModel) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}

