import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Game2DServer extends Application {

    private Server server;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        TextArea logTextArea = new TextArea();
        logTextArea.setEditable(false);

        VBox root = new VBox(logTextArea);
        Scene scene = new Scene(root, 600, 400);

        primaryStage.setTitle("Game 2D Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        server = new Server(logTextArea);
        new Thread(server).start();
    }

    @Override
    public void stop() {
        server.stopServer();
    }

    static class Server extends Thread {

        private static final int PORT = 5000;
        private ServerSocket serverSocket;
        private TextArea logTextArea;
        private boolean running;

        public Server(TextArea logTextArea) {
            this.logTextArea = logTextArea;
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;
                log("Server is running...");

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    log("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                    new ClientHandler(clientSocket).start();
                }
            } catch (IOException e) {
                log("Error starting server: " + e.getMessage());
            } finally {
                stopServer();
            }
        }

        public void stopServer() {
            running = false;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                log("Error closing server socket: " + e.getMessage());
            }
        }

        private void log(String message) {
            System.out.println(message);
            logTextArea.appendText(message + "\n");
        }
    }

    static class ClientHandler extends Thread {

        private Socket clientSocket;
        private Scanner input;
        private PrintWriter output;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                input = new Scanner(clientSocket.getInputStream());
                output = new PrintWriter(clientSocket.getOutputStream(), true);

                while (input.hasNextLine()) {
                    String message = input.nextLine();
                    System.out.println("Client: " + message);
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.out.println("Error closing client resources: " + e.getMessage());
                }
            }
        }
    }
}