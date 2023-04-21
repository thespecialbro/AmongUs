import javafx.application.*;

import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.animation.*;
import java.io.*;
import java.util.*;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
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
    // private final static String CREWMATE_RUNNERS = "assets/amongusRunners.png"; // file with icon for crewmates
    private static String backgroundImage = "assets/background.jpg"; // default value for debug purposes
    private static String collideMaskImage = "assets/collision.png"; // default value for debug purposes

    private ImageView collisionMask = null;
    private ImageView background = null;
    private Label testLabel = new Label();

    AnimationTimer animTimer = null;
    // private long renderCounter = 0;
    boolean goUP, goDOWN, goRIGHT, goLEFT = false;

    Crewmate crewmate = null;

    double SCREENWIDTH = 1600;
    double SCREENHEIGHT = 900;

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

            backgroundImage = String.format("levels/%s/background.png", settingsMap.get("level"));
            collideMaskImage = String.format("levels/%s/collide_mask.png", settingsMap.get("level"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // start the game scene
    public void initializeScene() {
        crewmate = new Crewmate();
        crewmate.setPos(1000,1000);

        collisionMask = new ImageView(new File(collideMaskImage).toURI().toString());
        background = new ImageView(new File(backgroundImage).toURI().toString());
        
        root.getChildren().add(background);
        //root.getChildren().add(collisionMask); // debug
        root.getChildren().add(crewmate);
        root.getChildren().add(testLabel);


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
            long lastUpdate = 0;
            long desiredFrameTime = (long)1e7;

            @Override
            public void handle(long now) {
                if(now - lastUpdate >= desiredFrameTime) {
                    crewmate.update();
                    lastUpdate = now;
                }
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


        private int lastDirection = 0;
        // private boolean doingTask = false;

        List<Task> tasks = Arrays.asList(
                new Task("Task 1", new Color(0, 0, 1, 1)),
                new Task("Task 2", new Color(109.0 / 255.0, 56.0 / 255.0, 1, 1)),
                new Task("Task 3", new Color(155.0 / 255.0, 73.0 / 255.0, 1, 1)));

        public void doTask() {
            Color colorAtPosition = collisionMask.getImage().getPixelReader().getColor((int) posX, (int) posY);
            Task task = findTaskByColor(colorAtPosition);
            if (task != null) {
                task.showTaskScreen();
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

            Platform.runLater(() -> {
                sprite.setTranslateX((SCREENWIDTH / 2) - (imgWidth/2));
                sprite.setTranslateY((SCREENHEIGHT / 2) - (imgHeight/2));
            }
            );
        }


        public void setPos(double x, double y) {
            this.posX = x;
            this.posY = y;
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

        }

        public boolean checkCollision(double posX, double posY) {
            return (collisionMask.getImage().getPixelReader().getColor((int) posX, (int) posY)
                    .equals(new Color(0, 0, 0, 1)));
        }

        public void showTaskScreen() {
            Stage taskStage = new Stage();
            taskStage.setTitle("Task");

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10, 10, 10, 10));

            Label taskLabel = new Label("Perform the task here.");
            Button completeTaskButton = new Button("Complete Task");
            completeTaskButton.setOnAction(event -> taskStage.close());

            vbox.getChildren().addAll(taskLabel, completeTaskButton);

            Scene taskScene = new Scene(vbox, 300, 200);
            taskStage.setScene(taskScene);
            taskStage.show();
        }
    }

    class Task {
        String taskId;
        Color taskColor;

        public Task(String taskId, Color taskColor) {
            this.taskId = taskId;
            this.taskColor = taskColor;
        }

        public void showTaskScreen() {
            Stage taskStage = new Stage();
            taskStage.setTitle("Task: " + taskId);

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10, 10, 10, 10));

            Label taskLabel = new Label("Perform the task: " + taskId);
            Button completeTaskButton = new Button("Complete Task");
            completeTaskButton.setOnAction(event -> taskStage.close());

            vbox.getChildren().addAll(taskLabel, completeTaskButton);

            Scene taskScene = new Scene(vbox, 300, 200);
            taskStage.setScene(taskScene);
            taskStage.show();
        }
    }

} // end class Races