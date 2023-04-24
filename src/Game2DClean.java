import javafx.application.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.VideoTrack;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.animation.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import javax.xml.stream.events.Namespace;

import org.w3c.dom.ranges.Range;

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

    private static String[] args;

    private static final String SETTINGS = "settings.txt";

    private String CREWMATE_IMAGE = "assets/student.png"; // file with icon for a crewmate
    private final static String CREWMATE_RUNNING = "assets/run.gif"; // file with icon for crewmates
    private static String backgroundImage = "assets/background.jpg"; // default value for debug purposes
    private static String collideMaskImage = "assets/collision.png"; // default value for debug purposes
    private static String miniMapImage;

    private ImageView collisionMask = null;
    private ImageView background = null;
    private ImageView miniMap = null;
    private Label testLabel = new Label();

    private int taskCounter = 0;
    private ProgressBar pgTasks = null;

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

    String playerName = "steve";
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

        loadSettings();

        showPlayerNameScreen();

        initializeScene();
    }

    private void showPlayerNameScreen() {

        Stage nameStage = new Stage();
        FlowPane fpTop = new FlowPane();
        FlowPane fpMid = new FlowPane();
        FlowPane fpBot = new FlowPane();
        fpTop.setAlignment(Pos.CENTER);
        fpMid.setAlignment(Pos.CENTER);
        fpBot.setAlignment(Pos.CENTER);
        nameStage.setTitle("Enter player name:");
        VBox vbox = new VBox(10);

        Label lblName = new Label("Enter name: ");
        TextField tfName = new TextField();
        Button btnFinish = new Button("Finish");
        Button btnSettings = new Button("Settings");
        Label lblChar = new Label("Select character");

        ObservableList<String> characterList = FXCollections.observableArrayList(
                "Red",
                "Blue",
                "Green",
                "Black",
                "White",
                "Gray",
                "Brown",
                "Kenny");

        ComboBox<String> comboChar = new ComboBox(characterList);

        btnSettings.setOnAction(event -> {
            openSettings();
        });

        btnFinish.setOnAction(event -> {
            if (tfName.getText().equals("") || comboChar.getValue() == null) {
                System.out.println("No name or character selected");
            } else
                playerName = tfName.getText();
            nameStage.close();

            switch (comboChar.getValue()) {
                case "Red":
                    playerColor = "red";
                    CREWMATE_IMAGE = "assets/students/red.png";
                    break;
                case "Blue":
                    playerColor = "blue";
                    CREWMATE_IMAGE = "assets/students/blue.png";
                    break;
                case "Green":
                    playerColor = "green";
                    CREWMATE_IMAGE = "assets/students/green.png";
                    break;
                case "Black":
                    playerColor = "black";
                    CREWMATE_IMAGE = "assets/students/black.png";
                    break;
                case "White":
                    playerColor = "white";
                    CREWMATE_IMAGE = "assets/students/white.png";
                    break;
                case "Gray":
                    playerColor = "gray";
                    CREWMATE_IMAGE = "assets/students/gray.png";
                    break;
                case "Brown":
                    playerColor = "brown";
                    CREWMATE_IMAGE = "assets/students/brown.png";
                    break;
                case "Kenny":
                    playerColor = "kenny";
                    CREWMATE_IMAGE = "assets/students/kenny.png";
                    break;
            }
        });

        fpTop.getChildren().addAll(lblName, tfName);
        fpMid.getChildren().addAll(lblChar, comboChar);
        fpBot.getChildren().addAll(btnFinish, btnSettings);
        vbox.getChildren().addAll(fpTop, fpMid, fpBot);

        Scene taskScene = new Scene(vbox, 300, 200);
        nameStage.setScene(taskScene);
        nameStage.showAndWait();
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
        btnFinish.setOnAction(event -> {
            settingsStage.close();
        });

        fp.getChildren().addAll(lblMainV, sldMainVolume, lblMusicV, sldMusicVolume, lblSFXV, sldSFXVolume, btnFinish);
        vbox.getChildren().add(fp);

        Scene taskScene = new Scene(vbox, 300, 600);
        settingsStage.setScene(taskScene);
        settingsStage.showAndWait();
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
            miniMapImage = String.format("levels/%s/finalMiniMap.jpg", settingsMap.get("level"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // start the game scene
    public void initializeScene() {
        crewmate = new Crewmate();

        crewmate.setPos(1000, 1000);

        collisionMask = new ImageView(new File(collideMaskImage).toURI().toString());
        background = new ImageView(new File(backgroundImage).toURI().toString());
        miniMap = new ImageView(new File(miniMapImage).toURI().toString());

        Platform.runLater(() -> {
            miniMap.setTranslateX(-1400 / 2);
            miniMap.setTranslateY(-780 / 2);
        });

        Circle fov = new Circle();
        fov.setCenterX(1600 / 2);
        fov.setCenterY(900 / 2);
        fov.setRadius(750);
        fov.setFill(Color.TRANSPARENT);
        fov.setStroke(Color.BLACK);
        fov.setStrokeWidth(600);
        fov.setOpacity(0.7);

        Label lblName = new Label("" + playerName);

        Platform.runLater(() -> {
            lblName.setTranslateY(50);
            lblName.setTranslateX(-10);
        });

        pgTasks = new ProgressBar(0);

        Platform.runLater(() -> {
            pgTasks.setTranslateY(-440);
            pgTasks.setTranslateX(0);
        });

        root.getChildren().add(background);
        // root.getChildren().add(collisionMask); // debug
        root.getChildren().add(crewmate);
        root.getChildren().add(lblName);

        root.getChildren().add(fov);
        root.getChildren().add(miniMap);
        root.getChildren().add(pgTasks);
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
                        crewmate.doVent();
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
            long desiredFrameTime = (long) 1e7;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= desiredFrameTime) {
                    crewmate.update();
                    renderCounter++;
                    lastUpdate = now;
                    if(taskCounter == 4) {
                        pgTasks.setProgress(100);
                    }
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
            if (!connected)
                return;
            try {
                output.writeObject(player);

                Object response = input.readObject();
                // System.out.println(response);
                if (response instanceof String) {
                    String msg = (String) response;
                    switch (msg) {
                        case "disconnect":
                            disconnect();
                            break;
                    }
                } else if (response instanceof GameInfo) {

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

        private Image runGif = new Image(CREWMATE_RUNNING);

        private int lastDirection = 0;
        // private boolean doingTask = false;

        List<Task> tasks = Arrays.asList(
                new Task1(new Color(0, 0, 1, 1)),
                new Task2(new Color(109.0 / 255.0, 56.0 / 255.0, 1, 1)),
                new Task3(new Color(155.0 / 255.0, 116.0 / 255.0, 1, 1)),
                new Task4(new Color(155.0 / 255.0, 73.0 / 255.0, 1, 1)));

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
            if (emergencyColor) {
                chat.showChat();
            }
        }

        private Task findTaskByColor(Color color) {
            return tasks.stream().filter(task -> task.taskColor.equals(color)).findFirst().orElse(null);
        }

        public void doVent() {
            if (findVent()) {
                if(posX > 2030 && posX < 2500 && posY > 2900 && posY < 3260) {
                    Platform.runLater(() -> {
                        crewmate.setPos(7030, 3700);
                    });
                }
                
                if(posX > 6900 && posX < 7300 && posY > 3500 && posY < 4000) {
                    Platform.runLater(() -> {
                        crewmate.setPos(2200, 3000);
                    });
                }
            }
        }

        public boolean findVent() {
            Color colorAtPosition = collisionMask.getImage().getPixelReader().getColor((int) posX, (int) posY);
            return colorAtPosition.equals(new Color(1, 0, 0, 1));
        }

        public Crewmate() {
            sprite = new ImageView(new File(CREWMATE_IMAGE).toURI().toString());
            this.getChildren().add(sprite);

            imgWidth = sprite.getImage().getWidth();
            imgHeight = sprite.getImage().getHeight();

            Platform.runLater(() -> {
                sprite.setTranslateX((SCREENWIDTH / 2) - (imgWidth / 2));
                sprite.setTranslateY((SCREENHEIGHT / 2) - (imgHeight / 2));
            });

        }

        public void setPos(double x, double y) {
            this.posX = x;
            this.posY = y;
        }

        


        public void update() {
            // updateAnim.handle(System.nanoTime());

            double speed = 20;
            double sin45 = Math.sin(Math.PI / 2.0);

            if ((goUP ^ goDOWN) || (goLEFT ^ goRIGHT)) { // if actually moving
                // todo play animation
                sprite = new ImageView(new File(CREWMATE_RUNNING).toURI().toString());
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
                if (lastDirection == 0) {
                    sprite.setScaleX(-1);
                    lastDirection = 1;
                }
            }
            if (goRIGHT) {
                if (!checkCollision(posX + speed, posY))
                    posX += speed;
                if (lastDirection != 0) {
                    sprite.setScaleX(1);
                    lastDirection = 0;
                }
            }
            // move background to represent player movement
            background.setTranslateX(-posX + (background.getImage().getWidth() / 2));
            background.setTranslateY(-posY + (background.getImage().getHeight() / 2));

            client.sendPlayerInfo(new Player(playerName, playerColor, posX, posY));

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
            completeTaskButton.setOnAction(event -> {
                taskCounter++;
                taskStage.close();
            });

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
            completeTaskButton.setDisable(false);
            completeTaskButton.setOnAction(event -> {
                taskCounter++;
                taskStage.close();
            });
            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);

            Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };

            gridPane.add(completeTaskButton, 10, 8);
            Scene taskScene = new Scene(gridPane, 340, 340);
            taskStage.setScene(taskScene);
            taskStage.showAndWait();

            for (int i = 0; i < 4; i++) {
                Circle leftCircle = new Circle(25, colors[i]);
                Circle rightCircle = new Circle(25, colors[i]);
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
            Label lblPercent = new Label("0%");
            pg = new ProgressBar(0);
            completeTaskButton.setOnAction(event -> {
                taskCounter++;
                taskStage.close();
            });

            fpTop.getChildren().add(taskLabel);
            fpMid.getChildren().addAll(lblPercent, pg);
            fpBot.getChildren().add(completeTaskButton);
            vbox.getChildren().addAll(fpTop, fpMid, fpBot);

            AnimationTimer progTime = new AnimationTimer() {

                double progress = 0;

                @Override
                public void handle(long now) {
                    progress += Math.random() * 0.001;
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
            taskStage.showAndWait();

            progTime.start();
        }
    }

    class Task4 extends Task {
        Color taskColor;
        ProgressBar pg;

        public Task4(Color taskColor) {
            super(taskColor);
        }

        public void showTaskScreen() {
            Stage taskStage = new Stage();
            FlowPane fpTop = new FlowPane();
            FlowPane fpMid = new FlowPane();
            FlowPane fpBot = new FlowPane();
            FlowPane fpInfo = new FlowPane();
            fpTop.setAlignment(Pos.CENTER);
            fpMid.setAlignment(Pos.CENTER);
            fpBot.setAlignment(Pos.CENTER);
            fpInfo.setAlignment(Pos.CENTER_RIGHT);
            taskStage.setTitle("Login task");
            Label lblInfo = new Label("oqwck2");
            VBox vbox = new VBox(10);

            Label lblName = new Label("Enter name: ");
            TextField tfName = new TextField();
            Label lblPassword = new Label("Enter password: ");
            TextField tfPassword = new TextField();
            Button completeTaskButton = new Button("Done");
            pg = new ProgressBar(0);
            completeTaskButton.setOnAction(event -> {
                if (!tfName.getText().equals(" ") || !tfPassword.getText().equals(" ")) {
                    if (tfName.getText().equals(playerName) && tfPassword.getText().equals(lblInfo.getText())) {
                        taskCounter++;
                        taskStage.close();
                    }
                }
            });

            fpTop.getChildren().addAll(lblName, tfName);
            fpMid.getChildren().addAll(lblPassword, tfPassword);
            fpBot.getChildren().add(completeTaskButton);
            fpInfo.getChildren().add(lblInfo);
            vbox.getChildren().addAll(fpTop, fpMid, fpBot, fpInfo);

            Scene taskScene = new Scene(vbox, 300, 200);
            taskStage.setScene(taskScene);
            taskStage.show();

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
             * ArrayList<Player> playerButtons = new ArrayList<>();
             * 
             * 
             * for(Player p : players) {
             * Button btnPlayer = new Button("" + p.getPlayerName());
             * somepane.getChildren().add(btnPlayer);
             * playerButtons.add(btnPlayer);
             * btnPlayer.setOnAction(event -> {
             * btnVote.setDisable(false)
             * 
             * });
             * }
             * 
             * 
             * //ois isAlive - set name to red if dead
             * 
             * 
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
            taskStage.showAndWait();
        }
    }

    class showPlayerNameScreen {
        int id;
        String playerName;

        public showPlayerNameScreen(int id, String name) {
            this.id = id;
            this.playerName = name;
        }

        public void showTaskScreen() {
            Stage nameStage = new Stage();
            FlowPane fpTop = new FlowPane();
            FlowPane fpMid = new FlowPane();
            FlowPane fpBot = new FlowPane();
            fpTop.setAlignment(Pos.CENTER);
            fpMid.setAlignment(Pos.CENTER);
            fpBot.setAlignment(Pos.CENTER);
            nameStage.setTitle("Enter player name:");
            VBox vbox = new VBox(10);

            Label lblName = new Label("Enter name: ");
            TextField tfName = new TextField();
            Label lblPassword = new Label("Enter name: ");
            TextField tfPassword = new TextField();
            Button btnFinish = new Button("Finish");

            btnFinish.setOnAction(event -> {
                if (tfName.getText().equals("")) {
                    System.out.println("No name");
                } else
                    nameStage.close();

            });

            fpTop.getChildren().addAll(lblName, tfName);
            fpMid.getChildren().addAll(lblPassword, tfPassword);
            fpBot.getChildren().add(btnFinish);
            vbox.getChildren().addAll(fpTop, fpMid, fpBot);

            Scene taskScene = new Scene(vbox, 300, 200);
            nameStage.setScene(taskScene);
            nameStage.showAndWait();

        }
    }

} // end class Races