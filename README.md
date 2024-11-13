# Distributed Key-Value Store

## Overview

This project implements a **Distributed Key-Value Store** using Java. It is a basic system where nodes are organized in a **consistent hash ring**, enabling efficient key-value storage and retrieval. The system supports operations like `PUT`, `GET`, and `DELETE` while ensuring data consistency and replication across multiple nodes.

The project is designed to explore concepts of:

- **Distributed Systems**
- **Socket Programming**
- **Consistent Hashing**
- **Replication and Fault Tolerance**

---

## Features

- **Distributed Node Architecture**: Each node runs as a standalone server capable of storing and managing key-value pairs.
- **Consistent Hashing**: Ensures that keys are distributed uniformly across nodes.
- **Replication**: Supports data replication to ensure fault tolerance.
- **Client-Server Communication**: Clients interact with nodes over TCP sockets.
- **Basic Operations**:
  - `PUT`: Add or update a key-value pair.
  - `GET`: Retrieve the value of a key.
  - `DELETE`: Remove a key-value pair.

---

## System Components

### **Node**

- Represents a single server in the distributed system.
- Maintains:
  - A local `dataStore` (in-memory key-value map).
  - A `hashRing` to determine key ownership.
- Handles client requests (`PUT`, `GET`, `DELETE`) and forwards them if another node is responsible for the key.

### **Client**

- A command-line application that interacts with the distributed system.
- Sends requests (`PUT`, `GET`, `DELETE`) to a designated node.
- Parses and displays responses from the server.

### **ClientHandler**

- Runs on each node to manage incoming client requests.
- Supports multithreaded request handling using Java's `ExecutorService`.

---

## Prerequisites

- **Java Development Kit (JDK)**: Version 8 or above.
- **IDE or Command-Line Tools** for Java development.
- Basic understanding of distributed systems and Java networking.

---

## Project Structure

```
src/
  com/example/dkv/
    Node.java          # Node implementation
    Client.java        # Client application
    ClientHandler.java # Handles requests for a Node
```

---

## How to Run

### 1. Compile the Project

Navigate to the `src` directory and compile all classes:

```bash
javac com/example/dkv/*.java
```

### 2. Start a Node

Run the `Node` class to start a server node:

```bash
java com.example.dkv.Node
```

You can start multiple nodes by changing the port number in the constructor.

### 3. Interact with the System

Use the `Client` class to perform operations:

```bash
java com.example.dkv.Client
```

---

## Usage

### **Operations**

1. **PUT Operation**:
   Store a key-value pair.

   ```bash
   PUT key value
   ```

   Example:

   ```bash
   PUT name Alice
   ```

2. **GET Operation**:
   Retrieve the value of a key.

   ```bash
   GET key
   ```

   Example:

   ```bash
   GET name
   ```

3. **DELETE Operation**:
   Remove a key-value pair.
   ```bash
   DELETE key
   ```
   Example:
   ```bash
   DELETE name
   ```

### **Output**

- Successful operations return `OK` or the requested value.
- Errors (e.g., key not found) return appropriate messages.

---

## Example Workflow

1. Start Node A on `port 8080`:

   ```bash
   java com.example.dkv.Node
   ```

2. Start Node B on `port 8081`:

   ```bash
   java com.example.dkv.Node
   ```

3. Use the Client to interact with the system:

   - PUT key-value:
     ```bash
     java com.example.dkv.Client
     ```
     Input: `PUT user Bob`
   - GET key:
     ```bash
     GET user
     ```
     Output: `Bob`

4. DELETE key:
   ```bash
   DELETE user
   ```

---

## Advanced Topics

- **Consistent Hashing**:

  - Keys are mapped to nodes using SHA-1 hashing to ensure balanced distribution.
  - The `hashRing` determines which node is responsible for a key.

- **Replication**:

  - Data replication to successor nodes (not fully implemented; planned for future).

- **Fault Tolerance**:
  - In case of node failure, keys can still be retrieved from replicated nodes.

---

## Future Enhancements

1. **Replication**: Implement replication logic for fault tolerance.
2. **Node Join/Leave**: Dynamically handle nodes joining or leaving the network.
3. **Persistence**: Store data persistently using a database or file system.
4. **Advanced Communication**: Use HTTP or gRPC for client-server communication.
5. **Monitoring**: Add logs and metrics for node and key-value operations.

---

## License

This project is licensed under the MIT License. See the LICENSE file for more details.

---

## Acknowledgments

- Inspired by distributed storage systems like Amazon DynamoDB and Apache Cassandra.
- Concepts based on distributed systems research and consistent hashing techniques.

---
