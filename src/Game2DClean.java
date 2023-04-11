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

    private ImageView collisionMask = null;
    private ImageView background = null;
    private Label testLabel = new Label();

    AnimationTimer animTimer = null;
    private long renderCounter = 0;
    boolean goUP, goDOWN, goRIGHT, goLEFT = false;

    private double angle = 0;
    private boolean isSliding = false;

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

            backgroundImage = String.format("levels/%s/background.png", settingsMap.get("level"));
            collideMaskImage = String.format("levels/%s/collide_mask.png", settingsMap.get("level"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // start the game scene
    public void initializeScene() {
        crewmate = new Crewmate();
        crewmate.setPos(600, 600);

        collisionMask = new ImageView(new File(collideMaskImage).toURI().toString());
        background = new ImageView(new File(backgroundImage).toURI().toString());

        root.getChildren().add(background);
        // root.getChildren().add(collisionMask); // debug
        root.getChildren().add(crewmate);
        root.getChildren().add(testLabel);
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

                    double speed = 5;
                    double sin45 = Math.sin(Math.PI / 4.0);

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
                    }
                    if (goRIGHT) {
                        if (!checkCollision(posX + speed, posY))
                            posX += speed;
                    }

                    double dx = 0;
                    double dy = 0;
                    if (goLEFT) {
                        dx -= speed;
                    }
                    if (goRIGHT) {
                        dx += speed;
                    }
                    if (goUP) {
                        dy -= speed;
                    }
                    if (goDOWN) {
                        dy += speed;
                    }

                    // check for collision in the current moving direction
                    boolean collide = checkCollision(posX + dx, posY + dy);

                    if (collide) { // hit a wall and only one key is pressed
                        double angle = Math.round(Math.atan2(dy, dx));
                        if (angle == 0 || angle == 90 || angle == 180 || angle == 270) {
                            isSliding = false;
                        }
                        double deltaPosX = Math.cos(angle);
                        double deltaPosY = Math.sin(angle);

                        // check for collision in the direction perpendicular to the wall
                        boolean collidePerpendicular = checkCollision(posX + deltaPosY, posY - deltaPosX);
                        if (!collidePerpendicular) { // slide along the wall
                            posX += deltaPosY;
                            posY -= deltaPosX;
                            isSliding = true;
                        } else { // check for collision in the opposite direction perpendicular to the wall
                            collidePerpendicular = checkCollision(posX - deltaPosY, posY + deltaPosX);
                            if (!collidePerpendicular) { // slide along the opposite direction of the wall
                                posX -= deltaPosY;
                                posY += deltaPosX;
                                isSliding = true;
                            } else { // still colliding
                                isSliding = false;
                            }
                        }
                    } else { // no collision, not sliding
                        isSliding = false;
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
                sprite.setTranslateX((background.getImage().getWidth() / 2) - (imgWidth / 2));
                sprite.setTranslateY((background.getImage().getHeight() / 2) - (imgHeight / 2));
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
            Color color = new Color(0, 0, 0, 1);
            return (collisionMask.getImage().getPixelReader().getColor((int) posX, (int) posY).equals(color));
        }
    }

} // end class Races