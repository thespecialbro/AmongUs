import javafx.application.*;
import javafx.beans.property.*;

import javafx.event.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.animation.*;
import java.io.*;
import java.util.*;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import java.util.stream.Collectors;

import javax.swing.text.html.MinimalHTMLWriter;

import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

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

    private final static String CREWMATE_IMAGE = "assets/amongus.png"; // file with icon for a crewmate
    private final static String CREWMATE_RUNNERS = "assets/amongusRunners.png"; // file with icon for crewmates
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

            backgroundImage = String.format("levels/%s/background1.png", settingsMap.get("level"));
            collideMaskImage = String.format("levels/%s/background1.png", settingsMap.get("level"));
            miniMapImage = String.format("levels/%s/miniMapImage.png", settingsMap.get("level"));

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
        root.getChildren().add(fov);
        root.getChildren().add(miniMap);
        // display the window
        scene = new Scene(root, 1600, 900);
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

        animTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                crewmate.update();
            }
        };

        animTimer.start();
    }

    class Crewmate extends Pane {
        private double posX = 0;
        private double posY = 0;
        private ImageView sprite = null;
        private double imgWidth;
        private double imgHeight;
        private AnimationTimer updateAnim;

        private int lastDirection = 0;
        private boolean doingTask = false;

        List<Task> tasks = Arrays.asList(
                new Task1(new Color(0, 0, 1, 1)),
                new Task2(new Color(109.0 / 255.0, 56.0 / 255.0, 1, 1)),
                new Task3(new Color(155.0 / 255.0, 73.0 / 255.0, 1, 1)));

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
            this.getChildren().add(sprite);

            imgWidth = sprite.getImage().getWidth();
            imgHeight = sprite.getImage().getHeight();

            updateAnim = new AnimationTimer() {
                long delta;
                long lastFrameTime;
                DoubleProperty framerate = new SimpleDoubleProperty(0);

                @Override
                public void handle(long time) {
                    // delta = time - lastFrameTime;
                    // lastFrameTime = time;
                    updateFrametime(time);
                    updateFramerate();
                    tick();
                    // update();
                }

                public double getFramerate() {
                    return framerate.get();
                }

                public DoubleProperty framerateProperty() {
                    return framerate;
                }

                public void updateFrametime(long nowNano) {
                    nowNano -= lastFrameTime;
                    lastFrameTime = nowNano;
                }

                public void updateFramerate() {
                    framerate.set(getFramerateHz());
                }

                public void tick() {

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

                }

                public long getDelta() {
                    return delta;
                }

                public double getFramerateHz() {
                    double frameRate = 1.0 / delta;
                    return frameRate * 1e9;
                }
            };

            Platform.runLater(() -> {
                sprite.setTranslateX((1600 / 2) - (imgWidth / 2));
                sprite.setTranslateY((900 / 2) - (imgHeight / 2));
            });
        }

        public void setPos(double x, double y) {
            this.posX = x;
            this.posY = y;
        }

        public void update() {
            updateAnim.handle(System.nanoTime());

            // set image pos (centered so coords aren't top-left of the image)
            // this.sprite.setTranslateX(posX - (imgWidth/2));
            // this.sprite.setTranslateY(posY - (imgHeight/2));
            //
            background.setTranslateX(-posX + (background.getImage().getWidth() / 2));
            background.setTranslateY(-posY + (background.getImage().getHeight() / 2));

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
                    progress += Math.random() * 0.03;
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
                    progress += Math.random() * 0.03;
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