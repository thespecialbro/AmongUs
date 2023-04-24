import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Game2DServer extends Application {

    private Server server;
    private static int maxPlayers = 4;
    private static Map<Integer, Player> players = new HashMap<>();
    private static String mapName = "newtest";
    private static String gameID = "12345";
    private static int clientCount = 0;

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
                    new ClientHandler(clientSocket, clientCount+1).start();
                    clientCount++;
                    if(clientCount >= maxPlayers) break; // stop accepting new connections when enough players are present
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
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private int id;

        public ClientHandler(Socket clientSocket, int id) {
            this.id = id;
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                input = new ObjectInputStream(clientSocket.getInputStream());
                output = new ObjectOutputStream(clientSocket.getOutputStream());

                Player player = null;

                while (true) {
                    Object message = input.readObject();
                    if(message instanceof Player) {
                        Player[] visiblePlayers = null;
                        ArrayList<Player> otherPlayers = new ArrayList<Player>();
                        Player out = (Player) message;
                        out.setId(id);

                        if(player != null) {
                            // determine which other players are visible to the player
                            for(Player otherPlayer : players.values()) {
                                if(id != otherPlayer.getId()) {
                                    double distance = calculateDistance(player.getPosX(), player.getPosY(), otherPlayer.getPosX(), otherPlayer.getPosY());
                                    if(distance <= 600) {
                                        otherPlayers.add(otherPlayer);
                                    }
                                    // otherPlayers.add(otherPlayer);
                                }
                            }
                            
                            
                        }
                        players.put(id, out);
                        visiblePlayers = new Player[otherPlayers.size()];
                        for(int i = 0; i < otherPlayers.size(); i++) {
                            visiblePlayers[i] = otherPlayers.get(i);
                        }

                        // get player information
                        player = out;

                        GameInfo game = new GameInfo(gameID, mapName, player.getPosX(), player.getPosY(), visiblePlayers);
                        output.writeObject(game);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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
        private double calculateDistance(double x1, double y1, double x2, double y2) {
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }
    }
}
