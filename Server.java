//Emir Adar
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server{

    private ServerSocket serverSocket;
    private List<Socket> clientSockets = new ArrayList<>();
    private static int port = 2000;

    /** This is the main method */
    public static void main(String[] args) {

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Unavailable port. Using port 2000.");
            }
        }

        Server server = new Server();
        server.startServer(port);
    }

    /** Method to start the server
     * @param port
     * */
    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Host: " + serverSocket.getInetAddress().getHostAddress() + " | Port: " + port);

            // always listening and accepting clients and creates a new thread for each
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                new Thread(new ClientHandler(clientSocket)).start();
                updateClientCount();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /** A private class to handle clients
     * @implements java.lang.Runnable
     * */
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        // constructor for the class
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        // run method
        @Override
        public void run() {

            // trying to connect to the client
            try {
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "ISO-8859-1"), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("New connection from: " + clientAddress);

                // broadcast message as long as there is one
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("From: " + clientAddress + ": " + message);
                    broadcastMessage(clientAddress + ": " + message);
                }

            // catches exception in case the socket disconnects abruptly or the streams don't work properly
            } catch (SocketException e) {
                System.out.println(clientSocket.getInetAddress().getHostAddress() + " has disconnected.");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                removeClient(clientSocket);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    /** Method for removing a client from the list
     * @param clientSocket
     * */
    public synchronized void removeClient(Socket clientSocket) {
        clientSockets.remove(clientSocket);
        updateClientCount();
    }

    // prints out the number of clients connected
    public void updateClientCount() {
        System.out.println("Number of clients: " + clientSockets.size());
    }

    /** Method for broadcasting a message to all clients
     * @param message
     * @throws IOException the method may not be able to send the message
     * @throws SocketException the method may be unable to listen to the socket
     * */
    public void broadcastMessage(String message){
        for (Socket clientSocket : clientSockets) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);
            } catch (SocketException e) {
                removeClient(clientSocket);
            } catch (IOException e){
                System.out.println("Failed to send message.");
            }
        }
    }
}