//package fr.tp.inf112.projects.robotsim.client;
//
//import javax.swing.JOptionPane;
//
//import java.io.*;
////import java.lang.System.Logger.Level;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Logger;
//
//import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
////import fr.tp.inf112.projects.robotsim.app.SimulatorApplication;
//
///**
// * RemoteFileCanvasChooser - A file chooser for remote file browsing.
// * It communicates with a server to fetch or save file names.
// */
//public class RemoteFileCanvasChooser extends FileCanvasChooser {
//    private static final Logger LOGGER = Logger.getLogger(RemoteFileCanvasChooser.class.getName());
//
//    private final String serverHost;
//    private final int serverPort;
//
//    /**
//     * Constructor for RemoteFileCanvasChooser.
//     *
//     * @param serverHost The server's hostname or IP address.
//     * @param serverPort The port on which the server is listening.
//     */
//    public RemoteFileCanvasChooser(String serverHost, int serverPort) {
//        super("remote-files", "Remote File Chooser");
//        this.serverHost = serverHost;
//        this.serverPort = serverPort;
//    }
//
//    @Override
//    public String browseCanvases(boolean open) {
//        LOGGER.info("browseCanvases called with open = " + open);
//
//        if (open) {
//            try {
//                LOGGER.info("Fetching file list from server...");
//                List<String> fileNames = fetchFileNamesFromServer();
//                LOGGER.info("Fetched file list: " + fileNames);
//
//                if (fileNames.isEmpty()) {
//                    LOGGER.warning("No files available on the server.");
//                    JOptionPane.showMessageDialog(null, "No files available on the server.", "Error", JOptionPane.ERROR_MESSAGE);
//                    return null;
//                }
//
//                LOGGER.info("Displaying file selection dialog...");
//                String selectedFile = (String) JOptionPane.showInputDialog(
//                        null,
//                        "Select a file to open:",
//                        "Open Remote File",
//                        JOptionPane.PLAIN_MESSAGE,
//                        null,
//                        fileNames.toArray(),
//                        fileNames.get(0)
//                );
//                LOGGER.info("File selected: " + selectedFile);
//                return selectedFile;
//
//            } catch (IOException e) {
//                LOGGER.severe("Error fetching file list from server.");
//                JOptionPane.showMessageDialog(null, "Error fetching file list from server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//                return null;
//            }
//        } else {
//            LOGGER.info("Prompting user for a file name to save...");
//            String fileName = JOptionPane.showInputDialog(null, "Enter a name for the new file:", "Save Remote File", JOptionPane.PLAIN_MESSAGE);
//            LOGGER.info("File name entered: " + fileName);
//            return fileName;
//        }
//    }
//
//
//    /**
//     * Fetches a list of file names from the server.
//     *
//     * @return A list of file names available on the server.
//     * @throws IOException If an error occurs during communication with the server.
//     */
//    @SuppressWarnings("unchecked")
//	private List<String> fetchFileNamesFromServer() throws IOException {
//        List<String> fileNames = new ArrayList<>();
//
//        try (
//            Socket socket = new Socket(serverHost, serverPort);
//            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//        ) {
//            // Request file names from the server
//            out.writeObject("LIST_FILES");
//
//            // Read the list of file names
//            Object response = in.readObject();
//            if (response instanceof List) {
//                fileNames = (List<String>) response;
//            } else {
//                throw new IOException("Invalid response from server: Expected List<String>");
//            }
//        } catch (ClassNotFoundException e) {
//            throw new IOException("Class not found during deserialization", e);
//        }
//
//        return fileNames;
//    }
//}
package fr.tp.inf112.projects.robotsim.client;
import javax.swing.JOptionPane;
import java.io.*;
import java.net.Socket;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

public class RemoteFileCanvasChooser extends FileCanvasChooser {
    private final String serverAddress;
    private final int port;

    public RemoteFileCanvasChooser(String serverAddress, int port, String fileExtension, String documentTypeLabel) {
        super(fileExtension, documentTypeLabel); // Call the superclass constructor
        this.serverAddress = serverAddress;
        this.port = port;
    }

    @Override
    public String browseCanvases(boolean open) {
        if (open) {
            // If "open" is true, request the list of files from the server
            return requestCanvasFileFromServer();
        } else {
            // If "open" is false, prompt the user to input a file name for saving
            return JOptionPane.showInputDialog(null, "Enter the name for the new model file:", "Save Model File", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private String requestCanvasFileFromServer() {
        try (Socket socket = new Socket(serverAddress, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send request for file list to the server
            out.writeObject("LIST_FILES");
            out.flush();

            // Read the server's response
            Object response = in.readObject();

            if (response instanceof String[]) {
                String[] fileNames = (String[]) response;

                // Handle empty file list
                if (fileNames.length == 0) {
                    JOptionPane.showMessageDialog(null, "No files available on the server.", "Information", JOptionPane.INFORMATION_MESSAGE);
                    return null;
                }

                // Display file names in a dialog and let the user choose one
                return (String) JOptionPane.showInputDialog(
                        null,
                        "Choose a model file to open:",
                        "Open Model File",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        fileNames,
                        fileNames[0] // Default selection
                );

            } else if (response instanceof String) {
                // Server sent a single string (e.g., an error message)
                JOptionPane.showMessageDialog(null, "Server Error: " + response, "Error", JOptionPane.ERROR_MESSAGE);
                return null;

            } else {
                // Unexpected response type
                throw new ClassCastException("Unexpected response type: " + response.getClass().getName());
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Unexpected server response: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }
}


