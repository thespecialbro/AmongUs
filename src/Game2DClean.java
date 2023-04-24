import javafx.application.*;

import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.*;
import javafx.animation.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.geometry.*;

/**
 * AmongUSStarter with JavaFX and Threads
 * Loading imposters
 * Loading background
 * Control actors and backgrounds
 * Create many number of imposters - random controlled
 * RGB based collision
 * Collsion between two imposters
 */

public class Game2DClean extends Application {
    // Window attributes
    private Stage stage;
    private Scene scene;
    private StackPane root;
    private StackPane otherPlayersPane;

    private static String[] args;

    private static final String SETTINGS = "settings.txt";


    private final static String CREWMATE_IMAGE = "assets/student.png"; // file with icon for a crewmate
    private final static String CREWMATE_RUNNING = "assets/run.gif"; // file with icon for crewmates
    private static String backgroundImage = "assets/background.jpg"; // default value for debug purposes
    private static String collideMaskImage = "assets/collision.png"; // default value for debug purposes
    private static String miniMapImage;

    private ImageView collisionMask = null;
    private ImageView background = null;
    private ImageView miniMap = null;
    private Label testLabel = new Label();

    AnimationTimer animTimer = null;
    private long renderCounter = 0;
    boolean goUP, goDOWN, goRIGHT, goLEFT = false;

    Crewmate crewmate = null;
    
    double SCREENWIDTH = 1600;
    double SCREENHEIGHT = 900;
    
    // server stuff
    Client client = new Client();
    String ip = "localhost";
    static final int SERVER_PORT = 5000;

    String playerName = "sadfgfhg";
    String playerColor = "red";


    // main program
    public static void main(String[] _args) {
        args = _args;
        launch(args);
    }

    // start() method, called via launch
    public void start(Stage _stage) {
        // stage seteup
        stage = _stage;
        stage.setTitle("Game2D Starter");
        stage.setOnCloseRequest(
                new EventHandler<WindowEvent>() {
                    public void handle(WindowEvent evt) {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                });

        // root pane
        root = new StackPane();
        otherPlayersPane = new StackPane();

        loadSettings();

        initializeScene();
    }

    public void loadSettings() {
        try {
            Scanner settingsReader = new Scanner(new File(SETTINGS));

            Map<String, String> settingsMap = new HashMap<String, String>();
            while (settingsReader.hasNext()) {
                String[] s = settingsReader.nextLine().split("=");
                settingsMap.put(s[0], s[1]);
            }

            backgroundImage = String.format("levels/%s/background3.jpg", settingsMap.get("level"));
            collideMaskImage = String.format("levels/%s/collide_mask2.png", settingsMap.get("level"));
            miniMapImage = String.format("levels/%s/miniMapImage.png", settingsMap.get("level"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // start the game scene
    public void initializeScene() {
        crewmate = new Crewmate();

        crewmate.setPos(1000.0,1000.0);


        collisionMask = new ImageView(new File(collideMaskImage).toURI().toString());
        background = new ImageView(new File(backgroundImage).toURI().toString());
        miniMap = new ImageView(new File(miniMapImage).toURI().toString());

        Platform.runLater(() -> {
            miniMap.setTranslateX(-1400 / 2);
            miniMap.setTranslateY(-700 / 2);
        });

        Circle fov = new Circle();
        fov.setCenterX(1600 / 2);
        fov.setCenterY(900 / 2);
        fov.setRadius(750); 
        fov.setFill(Color.TRANSPARENT);
        fov.setStroke(Color.BLACK);
        fov.setStrokeWidth(600);
        fov.setOpacity(0.7);

        root.getChildren().add(background);
        // root.getChildren().add(collisionMask); // debug
        root.getChildren().add(crewmate);
        root.getChildren().add(testLabel);

        root.getChildren().add(otherPlayersPane);

        root.getChildren().add(fov);
        root.getChildren().add(miniMap);
        // display the window
        scene = new Scene(root, SCREENWIDTH, SCREENHEIGHT);
        // scene.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        // KEY PRESS
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent button) {

                switch (button.getCode()) {
                    case ESCAPE:
                        Platform.exit();
                        System.exit(0);
                        break;

                    case E:
                        crewmate.doTask();
                        crewmate.openChat();
                        break;
                    case UP:
                    case W:
                        goUP = true;
                        break;

                    case DOWN:
                    case S:
                        goDOWN = true;
                        break;

                    case LEFT:
                    case A:
                        goLEFT = true;
                        break;

                    case RIGHT:
                    case D:
                        goRIGHT = true;
                        break;

                    default:
                        ;
                }
            }
        });

        // KEY RELEASE
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent button) {
                switch (button.getCode()) {
                    case UP:
                    case W:
                        goUP = false;
                        break;

                    case DOWN:
                    case S:
                        goDOWN = false;
                        break;

                    case LEFT:
                    case A:
                        goLEFT = false;
                        break;

                    case RIGHT:
                    case D:
                        goRIGHT = false;
                        break;

                    default:
                        ;
                }
            }
        });

        
        doConnect();
    }

    private void startGame() {
        animTimer = new AnimationTimer() {
            long lastUpdate = 0;
            long desiredFrameTime = (long)1e7;

            @Override
            public void handle(long now) {
                if(now - lastUpdate >= desiredFrameTime) {
                    crewmate.update();
                    renderCounter++;
                    lastUpdate = now;
                }
            }
        };

        animTimer.start();
    }

    private void doConnect() {
        // todo
        client.start();
    }

    class Client extends Thread {
        private Socket socket;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        private boolean connected = false;

        private ArrayList<Crewmate> others = new ArrayList<Crewmate>();

        @Override
        public void run() {
            try {
                socket = new Socket(ip, SERVER_PORT);
            
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());

                connected = true;

                startGame();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendPlayerInfo(Player player) {
            if(!connected) return;
            try {
                output.writeObject(player);

                Object response = input.readObject();
                // System.out.println(response);
                if(response instanceof String) {
                    String msg = (String)response;
                    switch(msg) {
                        case "disconnect":
                        disconnect();
                        break;
                    }
                } else if(response instanceof GameInfo) {
                    // get game info from server
                    GameInfo game = (GameInfo)response;
                    // if(renderCounter % 10 == 0) {
                        if(game.getOthers().length != others.size()) {
                            // rebuild player list
                            for(Crewmate c : others) {
                                otherPlayersPane.getChildren().remove(c);
                            }

                            others = new ArrayList<>();
                            for(Player p : game.getOthers()) {
                                Crewmate c = new Crewmate();
                                c.setPos(p.getPosX(), p.getPosY());
                                others.add(c);
                                otherPlayersPane.getChildren().add(c);
                            }
                        }
                    // }

                    if(game.getOthers() != null && game.getOthers().length > 0) {
                        for(int i = 0; i < others.size(); i++) {
                            Player p = game.getOthers()[i];
                            ((Crewmate)otherPlayersPane.getChildren().get(i)).setPos(p.getPosX(), p.getPosY());
                            ((Crewmate)otherPlayersPane.getChildren().get(i)).moveSprite((p.getPosX() - crewmate.getPosX()), (p.getPosY() - crewmate.getPosY()));
                            ((Crewmate)otherPlayersPane.getChildren().get(i)).setFacing(p.getFacing());
                        }
                    }
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void disconnect() {
            try {
                input.close();
                output.close();
                socket.close();
                connected = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
    }


    class Crewmate extends Pane {
        private double posX = 0;
        private double posY = 0;
        private ImageView sprite = null;
        private double imgWidth;
        private double imgHeight;

        private Label lblPos = new Label();


        private int facing = 0;
        // private boolean doingTask = false;

        List<Task> tasks = Arrays.asList(
                new Task(new Color(0, 0, 1, 1)),
                new Task(new Color(109.0 / 255.0, 56.0 / 255.0, 1, 1)),
                new Task(new Color(155.0 / 255.0, 73.0 / 255.0, 1, 1)));

        public void doTask() {
            Color colorAtPosition = collisionMask.getImage().getPixelReader().getColor((int) posX, (int) posY);
            Task task = findTaskByColor(colorAtPosition);
            if (task != null) {
                task.showTaskScreen();
            }
        }

        public void openChat() {
            Boolean emergencyColor = collisionMask.getImage().getPixelReader().getColor((int) posX, (int) posY)
            .equals(new Color(0, 1, 0, 1));
            Chat chat = new Chat();
            if(emergencyColor) {
                chat.showChat();
            }
        }
        private Task findTaskByColor(Color color) {
            return tasks.stream().filter(task -> task.taskColor.equals(color)).findFirst().orElse(null);
        }

        public Crewmate() {
            sprite = new ImageView(new File(CREWMATE_IMAGE).toURI().toString());
            this.getChildren().addAll(sprite, lblPos);

            imgWidth = sprite.getImage().getWidth();
            imgHeight = sprite.getImage().getHeight();

            // Platform.runLater(() -> {
            //     sprite.setTranslateX((SCREENWIDTH / 2) - (imgWidth/2));
            //     sprite.setTranslateY((SCREENHEIGHT / 2) - (imgHeight/2));

        
            // }
            // );
            moveSprite(0, 0);
        }

        public double getPosX() {
            return posX;
        }

        public double getPosY() {
            return posY;
        }
    

        public void setPos(double x, double y) {
            this.posX = x;
            this.posY = y;

            lblPos.setText(String.format("%f, %f", x, y));
        }

        public void moveSprite(double x, double y) {
            Platform.runLater(() -> {
                sprite.setTranslateX((SCREENWIDTH / 2) - (imgWidth/2) + x);
                sprite.setTranslateY((SCREENHEIGHT / 2) - (imgHeight/2) + y);
                lblPos.setTranslateX((SCREENWIDTH / 2) + x);
                lblPos.setTranslateY((SCREENHEIGHT / 2) + y);
            });
        }

        public void setFacing(int direction) {
            facing = direction;
            if (facing == 0) {
                sprite.setScaleX(1);
            } else {
                sprite.setScaleX(-1);
            }
        }

        public void update() {
            //updateAnim.handle(System.nanoTime());

            double speed = 20;
            double sin45 = Math.sin(Math.PI / 2.0);

            if ((goUP ^ goDOWN) || (goLEFT ^ goRIGHT)) { // if actually moving
                // todo play animation
            }

            if ((goUP ^ goDOWN) && (goLEFT ^ goRIGHT)) { // if moving diagonally
                speed *= sin45;
            }
            if (goUP) {
                if (!checkCollision(posX, posY - speed))
                    posY -= speed;
            }
            if (goDOWN) {
                if (!checkCollision(posX, posY + speed))
                    posY += speed;
            }

            if (goLEFT) {
                if (!checkCollision(posX - speed, posY))
                    posX -= speed;
                if (facing == 0) {
                    sprite.setScaleX(-1);
                    facing = 1;
                }
            }
            if (goRIGHT) {
                if (!checkCollision(posX + speed, posY))
                    posX += speed;
                if (facing != 0) {
                    sprite.setScaleX(1);
                    facing = 0;
                }
            }
            lblPos.setText(String.format("%f, %f", posX, posY));
            // move background to represent player movement
            background.setTranslateX(-posX + (background.getImage().getWidth() / 2));
            background.setTranslateY(-posY + (background.getImage().getHeight() / 2));

            client.sendPlayerInfo(new Player(playerName, playerColor, posX, posY, facing));            
        }
        
        public boolean checkCollision(double posX, double posY) {
            return (collisionMask.getImage().getPixelReader().getColor((int) posX, (int) posY)
                    .equals(new Color(0, 0, 0, 1)));
        }
    }

    class Task {
        Color taskColor;

        public Task(Color taskColor) {
            this.taskColor = taskColor;
        }

        public void showTaskScreen() {
        }
    }

    class Task1 extends Task {
        Color taskColor;
        ProgressBar pg;

        public Task1(Color taskColor) {
            super(taskColor);
        }

        public void showTaskScreen() {
            Stage taskStage = new Stage();
            FlowPane fpTop = new FlowPane();
            FlowPane fpMid = new FlowPane();
            FlowPane fpBot = new FlowPane();
            fpTop.setAlignment(Pos.CENTER);
            fpMid.setAlignment(Pos.CENTER);
            fpBot.setAlignment(Pos.CENTER);
            taskStage.setTitle("Download task");
            VBox vbox = new VBox(10);

            Label taskLabel = new Label("Perform the download task: ");
            Button completeTaskButton = new Button("Done");
            completeTaskButton.setDisable(true);
            pg = new ProgressBar(0);
            completeTaskButton.setOnAction(event -> taskStage.close());

            fpTop.getChildren().add(taskLabel);
            fpMid.getChildren().add(pg);
            fpBot.getChildren().add(completeTaskButton);
            vbox.getChildren().addAll(fpTop, fpMid, fpBot);

            AnimationTimer progTime = new AnimationTimer() {

                double progress = 0;

                @Override
                public void handle(long now) {
                    progress += Math.random() * 0.001;
                    pg.setProgress(progress);
                    if (pg.getProgress() > 1) {
                        completeTaskButton.setDisable(false);
                        stop();
                        pg.setProgress(1);

                    }
                }
            };
            Scene taskScene = new Scene(vbox, 300, 200);
            taskStage.setScene(taskScene);
            taskStage.show();

            progTime.start();
        }
    }

    class Task2 extends Task {
        Color taskColor;
        int matchedColors = 0;
        Stage taskStage;
        Button completeTaskButton;

        public Task2(Color taskColor) {
            super(taskColor);
        }

        
        public void showTaskScreen() {
            Stage taskStage = new Stage();
            taskStage.setTitle("Wires Task");
            Button completeTaskButton = new Button("Done");
            completeTaskButton.setDisable(true);
            completeTaskButton.setOnAction(event -> taskStage.close());
            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);

            Color[] colors1 = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };
            Color[] colors2 = { Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW };

            gridPane.add(completeTaskButton, 10, 8);
            Scene taskScene = new Scene(gridPane, 340, 340);
            taskStage.setScene(taskScene);
            taskStage.show();

            for (int i = 0; i < 4; i++) {
                Circle leftCircle = new Circle(25, colors1[i]);
                Circle rightCircle = new Circle(25, colors2[i]);
                leftCircle.setStroke(Color.BLACK);
                rightCircle.setStroke(Color.BLACK);

                leftCircle.setOnMouseClicked(event -> {
                    if (leftCircle.getStrokeWidth() == 3) {
                        leftCircle.setStrokeWidth(1);
                    }
                    if (leftCircle.getStrokeWidth() == 1) {
                        leftCircle.setStrokeWidth(3);
                        checkMatch(leftCircle, rightCircle);
                        if (matchedColors == 4) {
                            completeTaskButton.setDisable(false);
                        }
                    }
                });

                rightCircle.setOnMouseClicked(event -> {
                    if (rightCircle.getStrokeWidth() == 3) {
                        rightCircle.setStrokeWidth(1);

                    }
                    if (rightCircle.getStrokeWidth() == 1) {
                        rightCircle.setStrokeWidth(3);
                        checkMatch(leftCircle, rightCircle);
                        if (matchedColors == 4) {
                            completeTaskButton.setDisable(false);
                        }
                    }

                });

                gridPane.add(leftCircle, 6, i);
                gridPane.add(rightCircle, 12, i);
            }

        }

        private void checkMatch(Circle leftCircle, Circle rightCircle) {

            if (leftCircle.getStrokeWidth() == 3 && rightCircle.getStrokeWidth() == 3) {
                if (leftCircle.getFill().equals(rightCircle.getFill())) {
                    matchedColors++;
                    leftCircle.setDisable(true);
                    rightCircle.setDisable(true);

                } else {
                    leftCircle.setStrokeWidth(1);
                    rightCircle.setStrokeWidth(1);
                }
            }
            if (leftCircle.getStrokeWidth() != 3 && rightCircle.getStrokeWidth() == 3) {
                rightCircle.setStrokeWidth(3);
            }
            if (leftCircle.getStrokeWidth() == 3 && rightCircle.getStrokeWidth() != 3) {
                leftCircle.setStrokeWidth(3);
            }
        }
    }

    class Task3 extends Task {
        Color taskColor;
        ProgressBar pg;

        public Task3(Color taskColor) {
            super(taskColor);
        }

        public void showTaskScreen() {
            Stage taskStage = new Stage();
            FlowPane fpTop = new FlowPane();
            FlowPane fpMid = new FlowPane();
            FlowPane fpBot = new FlowPane();
            fpTop.setAlignment(Pos.CENTER);
            fpMid.setAlignment(Pos.CENTER);
            fpBot.setAlignment(Pos.CENTER);
            taskStage.setTitle("Upload task");
            VBox vbox = new VBox(10);

            Label taskLabel = new Label("Perform the upload task: ");
            Button completeTaskButton = new Button("Done");
            completeTaskButton.setDisable(true);
            Label lblPercent = new Label("0%");
            pg = new ProgressBar(0);
            completeTaskButton.setOnAction(event -> taskStage.close());

            fpTop.getChildren().add(taskLabel);
            fpMid.getChildren().addAll(lblPercent, pg);
            fpBot.getChildren().add(completeTaskButton);
            vbox.getChildren().addAll(fpTop, fpMid, fpBot);

            AnimationTimer progTime = new AnimationTimer() {

                double progress = 0;

                @Override
                public void handle(long now) {
                    progress += Math.random() * 0.03;
                    pg.setProgress(progress);
                    lblPercent.setText(String.format("%.1f", progress * 100) + "%");
                    if (pg.getProgress() >= 1) {
                        completeTaskButton.setDisable(false);
                        stop();
                        pg.setProgress(1);

                    }
                }
            };
            Scene taskScene = new Scene(vbox, 300, 200);
            taskStage.setScene(taskScene);
            taskStage.show();

            progTime.start();
        }
    }

    class Chat {
        TextArea chat;

        public Chat() {

        }

        public void showChat() {
            Stage taskStage = new Stage();
            FlowPane fpTop = new FlowPane();
            FlowPane fpMid = new FlowPane();
            FlowPane fpBot = new FlowPane();
            FlowPane fpVote = new FlowPane();
            fpTop.setAlignment(Pos.CENTER);
            fpMid.setAlignment(Pos.CENTER);
            fpBot.setAlignment(Pos.CENTER);
            fpVote.setAlignment(Pos.CENTER);
            taskStage.setTitle("Chat");
            VBox vbox = new VBox(10);
            chat = new TextArea();
            Button btnSend = new Button("Send");

            /*
            ArrayList<Player> playerButtons = new ArrayList<>();


            for(Player p : players) {
                Button btnPlayer = new Button("" + p.getPlayerName());
                somepane.getChildren().add(btnPlayer);
                playerButtons.add(btnPlayer);
                btnPlayer.setOnAction(event -> {
                    btnVote.setDisable(false)
                    
                });
            }
            
            
            //ois isAlive - set name to red if dead

            
            */

            Button lbl1 = new Button("Player1");
            Button lbl2 = new Button("Player2");
            Button lbl3 = new Button("Player3");
            Button lbl4 = new Button("Player4");
            Button lbl5 = new Button("Player5");
            Button lbl6 = new Button("Player6");
            Button lbl7 = new Button("Player7");
            Button lbl8 = new Button("Player8");
            Button lbl9 = new Button("Player9");
            Button lbl10 = new Button("Player10");



            fpTop.getChildren().addAll(lbl1, lbl2, lbl3, lbl4, lbl5);
            fpMid.getChildren().addAll(lbl6, lbl7, lbl8, lbl9, lbl10);
            
            fpBot.getChildren().addAll(chat, btnSend);

            vbox.getChildren().addAll(fpTop, fpMid, fpBot);
            
            Scene taskScene = new Scene(vbox, 800, 400);
            taskStage.setScene(taskScene);
            taskStage.show();
        }
    }


} // end class Races