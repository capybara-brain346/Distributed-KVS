package kvs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private final String serverAddress;
    private final int serverPort;

    // Constructor to initialize server address and port
    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    // Method to send a PUT request to the server
    public void put(String key, String value) throws IOException {
        try (
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println("PUT " + key + " " + value);
            String response = in.readLine();
            if (response != null) {
                System.out.println("Server response: " + response);
            } else {
                System.err.println("Error: No response from server for PUT operation.");
            }
        }
    }

    // Method to send a GET request to the server
    public String get(String key) throws IOException {
        try (
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println("GET " + key);
            String response = in.readLine();
            if (response != null) {
                return response;
            } else {
                return "Error: No response from server for GET operation.";
            }
        }
    }

    // Method to send a DELETE request to the server
    public void delete(String key) throws IOException {
        try (
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println("DELETE " + key);
            String response = in.readLine();
            if (response != null) {
                System.out.println("Server response: " + response);
            } else {
                System.err.println("Error: No response from server for DELETE operation.");
            }
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        try {
            Client client = new Client("127.0.0.1", 8080);

            // Test PUT operation
            client.put("name", "Alice");

            // Test GET operation
            String value = client.get("name");
            System.out.println("GET response: " + value);

            // Test DELETE operation
            client.delete("name");

            // Verify DELETE by trying to GET the deleted key
            String deletedValue = client.get("name");
            System.out.println("GET after DELETE: " + deletedValue);
        } catch (IOException e) {
            System.err.println("Error during client operation: " + e.getMessage());
        }
    }
}
