import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.omg.CORBA.BAD_TYPECODE;

public class Game2DServer extends Application {

    private Server server;
    private static int maxPlayers = 4;
    private static ArrayList<Player> players = new ArrayList<>();
    private static String mapName = "movingtest";
    private static String gameID = "12345";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FlowPane fpTop = new FlowPane();
        FlowPane fpMid = new FlowPane();
        FlowPane fpBot = new FlowPane();

        fpTop.setAlignment(Pos.CENTER_RIGHT);
        fpMid.setAlignment(Pos.CENTER);
        fpBot.setAlignment(Pos.CENTER);

        Button btnSettings = new Button("Settings");

        btnSettings.setOnAction(event -> {
            openSettings();
        });

        Label lblIP = new Label("IP: ");
        TextField tfIP = new TextField();

        Label lblPort = new Label("Port: ");
        TextField tfPort = new TextField();

        Button btnSet = new Button("Set");

        btnSet.setOnAction(event -> {

        });

        TextArea logTextArea = new TextArea();
        logTextArea.setEditable(false);
        
        fpTop.getChildren().add(btnSettings);
        fpMid.getChildren().addAll(lblIP, tfIP, lblPort, tfPort, btnSet);
        fpBot.getChildren().add(logTextArea);

        VBox root = new VBox();
        
        root.getChildren().addAll(fpTop, fpMid, fpBot);
        Scene scene = new Scene(root, 600, 400);


        primaryStage.setTitle("Game 2D Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        server = new Server(logTextArea);
        new Thread(server).start();
    }

    public void openSettings() {
        
        Stage settingsStage = new Stage();
        FlowPane fp = new FlowPane(Orientation.VERTICAL);
        fp.setAlignment(Pos.CENTER);
        settingsStage.setTitle("Enter player name:");
        VBox vbox = new VBox(50);
        fp.setVgap(10);
        
        Label lblMainV = new Label("Main volume: ");
        Slider sldMainVolume = new Slider(0, 100, 100);
        Label lblMusicV = new Label("Music volume: ");
        Slider sldMusicVolume = new Slider(0, 100, 100);
        Label lblSFXV = new Label("SFX volume: ");
        Slider sldSFXVolume = new Slider(0, 100, 100);


        


        Button btnFinish = new Button("Back");

       

        fp.getChildren().addAll(lblMainV, sldMainVolume, lblMusicV, sldMusicVolume, lblSFXV, sldSFXVolume, btnFinish);
        vbox.getChildren().add(fp);

        Scene taskScene = new Scene(vbox, 300, 600);
        settingsStage.setScene(taskScene);
        settingsStage.showAndWait();
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
                    if(players.size() >= maxPlayers) break; // stop accepting new connections when enough players are present
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

        public ClientHandler(Socket clientSocket) {
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

                        if(player != null) {
                            // determine which other players are visible to the player
                            // todo look in visibility radius for other players
                            visiblePlayers = new Player[players.size()];
                            for(Player otherPlayer : players) {
                                double distance = calculateDistance(player.getPosX(), player.getPosY(), otherPlayer.getPosX(), otherPlayer.getPosY());
                                if(distance <= 600) {
                                    otherPlayers.add(otherPlayer);
                                }
                            }

                            for(int i = 0; i <= otherPlayers.size(); i++) {
                                visiblePlayers = otherPlayers.toArray(new Player[i]);
                            }
                        }

                        // get player information
                        player = (Player) message;

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
