package kvs;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

public class Node {
    private final String nodeId;
    private final String address;
    private final int port;
    private final ConcurrentHashMap<String, String> dataStore;
    private final TreeMap<Integer, Node> hashRing;
    private final ExecutorService executor;
    private boolean isRunning;

    public Node(String address, int port) {
        this.address = address;
        this.port = port;
        this.nodeId = generateNodeId(address + ":" + port);
        this.dataStore = new ConcurrentHashMap<>();
        this.hashRing = new TreeMap<>();
        this.executor = Executors.newFixedThreadPool(10);
        this.isRunning = false;
    }

    // Generate a unique node ID using SHA-256
    private String generateNodeId(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not generate node ID", e);
        }
    }

    // Start the node server
    public void start() {
        isRunning = true;
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Node started on port " + port);
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(new ClientHandler(clientSocket, this));
                }
            } catch (IOException e) {
                System.err.println("Could not listen on port " + port);
            }
        }).start();
    }

    // Stop the node server
    public void stop() {
        isRunning = false;
        executor.shutdown();
    }

    // Store a key-value pair
    public void put(String key, String value) {
        Node responsibleNode = getResponsibleNode(key);
        if (responsibleNode == this) {
            dataStore.put(key, value);
            replicateData(key, value); // Replicate to successor nodes
        } else {
            forwardRequest(responsibleNode, "PUT", key, value);
        }
    }

    // Retrieve a value by key
    public String get(String key) {
        Node responsibleNode = getResponsibleNode(key);
        if (responsibleNode == this) {
            return dataStore.get(key);
        } else {
            return forwardRequest(responsibleNode, "GET", key, null);
        }
    }

    public boolean delete(String key) {
        Node responsibleNode = getResponsibleNode(key);
        if (responsibleNode == this) {
            return dataStore.remove(key) != null;
        } else {
            String response = forwardRequest(responsibleNode, "DELETE", key, null);
            return "200 Deleted".equals(response);
        }
    }

    // Get the node responsible for a key based on consistent hashing
    private Node getResponsibleNode(String key) {
        int hash = getHash(key);
        Map.Entry<Integer, Node> entry = hashRing.ceilingEntry(hash);
        if (entry == null) {
            entry = hashRing.firstEntry();
        }
        return entry.getValue();
    }

    // Calculate hash for a key
    private int getHash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(key.getBytes());
            return Math.abs(bytesToInt(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not generate hash", e);
        }
    }

    private int bytesToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4 && i < bytes.length; i++) {
            value = (value << 8) | (bytes[i] & 0xFF);
        }
        return value;
    }

    // Handle replication to successor nodes
    private void replicateData(String key, String value) {
        int replicationFactor = 2; // Number of replicas
        SortedMap<Integer, Node> tailMap = hashRing.tailMap(getHash(nodeId));
        Iterator<Node> iterator = tailMap.values().iterator();

        for (int i = 0; i < replicationFactor && iterator.hasNext(); i++) {
            Node successorNode = iterator.next();
            if (successorNode != this) {
                forwardRequest(successorNode, "PUT", key, value);
            }
        }
    }

    // Forward request to responsible node
    private String forwardRequest(Node node, String operation, String key, String value) {
        try (Socket socket = new Socket(node.address, node.port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(operation + " " + key + " " + (value != null ? value : ""));
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Failed to forward request to node: " + node.nodeId);
            return null;
        }
    }

    public String getNodeId() {
        return nodeId;
    }
}

// ClientHandler class for handling incoming requests
class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Node node;

    public ClientHandler(Socket clientSocket, Node node) {
        this.clientSocket = clientSocket;
        this.node = node;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            if (request != null) {
                String[] parts = request.split(" ");
                String operation = parts[0];
                String key = parts[1];
                String value = parts.length > 2 ? parts[2] : null;

                if ("PUT".equalsIgnoreCase(operation)) {
                    node.put(key, value);
                    out.println("OK");
                } else if ("GET".equalsIgnoreCase(operation)) {
                    String result = node.get(key);
                    out.println(result != null ? result : "NOT_FOUND");
                } else {
                    out.println("ERROR: Unsupported operation");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client request");
        }
    }
}
