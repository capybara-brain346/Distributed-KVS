package kvs;

import java.io.*;
import java.net.*;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Node node;

    public ClientHandler(Socket socket, Node node) {
        this.clientSocket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] parts = inputLine.trim().split(" ");
                if (parts.length < 2) {
                    out.println("ERROR: Invalid request format");
                    continue;
                }

                String operation = parts[0].toUpperCase();
                String key = parts[1];
                String response;

                switch (operation) {
                    case "GET":
                        response = node.get(key);
                        out.println(response != null ? response : "404 Key not found");
                        break;

                    case "PUT":
                        if (parts.length < 3) {
                            out.println("ERROR: PUT operation requires a key and value");
                        } else {
                            String value = parts[2];
                            node.put(key, value);
                            out.println("200 OK");
                        }
                        break;

                    case "DELETE":
                        boolean removed = node.delete(key);
                        out.println(removed ? "200 Deleted" : "404 Key not found");
                        break;

                    default:
                        out.println("ERROR: Unsupported operation");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
