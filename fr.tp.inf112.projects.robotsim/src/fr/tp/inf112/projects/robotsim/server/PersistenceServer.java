package fr.tp.inf112.projects.robotsim.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.robotsim.model.Factory;

public class PersistenceServer {
    private static final Logger LOGGER = Logger.getLogger(PersistenceServer.class.getName());
    private static final String STORAGE_DIR = "factory_data/";

    // Enum for the implementation type
    private enum PersistImplementation {
        OBJECT_STREAM,
        JSON_STREAM
    }

    // Set the desired implementation type here
    private static final PersistImplementation currentImplementation = PersistImplementation.OBJECT_STREAM;
//    private static final PersistImplementation currentImplementation = PersistImplementation.JSON_STREAM;

    private static final ObjectMapper objectMapper;

    static {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("fr.tp.inf112.projects.robotsim.model")
                .allowIfSubType("java.util")
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    public static void main(String[] args) throws IOException {
        // Ensure the storage directory exists
        File storageDir = new File(STORAGE_DIR);
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new IOException("Failed to create storage directory: " + STORAGE_DIR);
        }

        try (ServerSocket serverSocket = new ServerSocket(50002)) {
            LOGGER.info("Server is waiting for clients on port 50002...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("Client connected.");

                // Handle the client in a new thread
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error starting the server", e);
        }
    }

    private static void handleClient(Socket clientSocket) {
        switch (currentImplementation) {
            case OBJECT_STREAM:
                handleClientObjectStream(clientSocket);
                break;
            case JSON_STREAM:
                handleClientJsonStream(clientSocket);
                break;
            default:
                LOGGER.warning("Unknown implementation selected.");
        }
    }

    // Handling client requests with Object Stream
    private static void handleClientObjectStream(Socket clientSocket) {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {

            Object received = ois.readObject();
            LOGGER.info("Received request via Object Stream");

            if (received instanceof Factory) {
                handleSaveRequest((Factory) received);
                oos.writeObject("Factory model saved successfully.");
            } else if (received instanceof String) {
                String request = (String) received;
                if ("LIST_FILES".equals(request)) {
                    sendFileList(oos);
                } else {
                    readFile(request, oos);
                }
            } else {
                LOGGER.warning("Unrecognized request: " + received);
                oos.writeObject("Unrecognized request.");
            }

            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error handling client request via Object Stream", e);
        }
    }

    // Handling client requests with JSON Stream
    private static void handleClientJsonStream(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String jsonRequest = reader.readLine();
            LOGGER.info("Received JSON request: " + jsonRequest);

            try {
                Factory factory = objectMapper.readValue(jsonRequest, Factory.class);
                LOGGER.info("Deserialied ::: " + factory + "\n");

                handleSaveRequest(factory);
                writer.write("Factory model saved successfully.");
            } catch (JsonProcessingException e) {
                LOGGER.warning("Failed to deserialize JSON to Factory. Checking for string request.");
                if ("LIST_FILES".equals(jsonRequest)) {
                    sendFileList(writer);
                } else {
                    readFile(jsonRequest, writer);
                }
            }

            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error handling client request via JSON Stream", e);
        }
    }

    private static void handleSaveRequest(Factory factory) throws IOException {
        String sanitizedFileName = sanitizeFileName(factory.getId());
        File file = new File(STORAGE_DIR, sanitizedFileName + ".json");
        try (FileWriter fileWriter = new FileWriter(file)) {
            objectMapper.writeValue(fileWriter, factory);
        }
        LOGGER.info("Saved factory model: " + sanitizedFileName);
    }

    private static void readFile(String fileName, ObjectOutputStream oos) throws IOException {
    	File file = new File(STORAGE_DIR + fileName);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream fileOis = new ObjectInputStream(fis)) {
                Factory factory = (Factory) fileOis.readObject();
                oos.writeObject(factory);
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error deserializing factory model", e);
                oos.writeObject("Error deserializing factory model.");
            }
        } else {
            LOGGER.warning("File not found: " + fileName);
            oos.writeObject("File not found.");
        }
    }

    private static void readFile(String fileName, BufferedWriter writer) throws IOException {
        File file = new File(STORAGE_DIR, fileName + ".json");
        if (file.exists()) {
            try (FileReader fileReader = new FileReader(file)) {
                Factory factory = objectMapper.readValue(fileReader, Factory.class);
                writer.write(objectMapper.writeValueAsString(factory));
            }
        } else {
            LOGGER.warning("File not found: " + fileName);
            writer.write("File not found.");
        }
    }
    
    	
    private static void sendFileList(ObjectOutputStream oos) throws IOException {
        File storageDir = new File(STORAGE_DIR);
        String[] files = storageDir.list((dir, name) -> name.endsWith(".ser"));
        oos.writeObject(files != null ? files : new String[0]);
    }

    private static void sendFileList(BufferedWriter writer) throws IOException {
        File storageDir = new File(STORAGE_DIR);
        String[] files = storageDir.list((dir, name) -> name.endsWith(".json"));
        writer.write(objectMapper.writeValueAsString(files != null ? files : new String[0]));
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}


