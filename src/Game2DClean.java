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
    private static String backgroundPath = "assets/background.jpg"; // default value for debug purposes
    private static String collisionMaskPath = "assets/collision.png"; // default value for debug purposes

    private ImageView collisionMask = null;
    private ImageView background = null;
    private Label lblFramerate = new Label("<FPS>");

    AnimationTimer animTimer = null;
    private long renderCounter = 0;
    boolean goUP, goDOWN, goRIGHT, goLEFT = false;

    Crewmate crewmate = null;

    double windowWidth = 1600;
    double windowHeight = 900;

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
            while(settingsReader.hasNext()) {
                String[] s = settingsReader.nextLine().split("=");
                settingsMap.put(s[0], s[1]);
            }

            backgroundPath = String.format("levels/%s/background.png", settingsMap.get("level"));
            collisionMaskPath = String.format("levels/%s/collide_mask.png", settingsMap.get("level"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // start the game scene
    public void initializeScene() {
        crewmate = new Crewmate();
        crewmate.setPos(1000, 1000);

        collisionMask = new ImageView(new File(collisionMaskPath).toURI().toString());
        background = new ImageView(new File(backgroundPath).toURI().toString());

        // collisionMask.setScaleX(.5);
        // collisionMask.setScaleY(.5);

        // background.setScaleX(.5);
        // background.setScaleY(.5);
        
        root.getChildren().add(background);
        //root.getChildren().add(collisionMask); // debug
        root.getChildren().add(crewmate);
        //root.getChildren().add(lblFramerate);

        //lblframerate.setTe

        // display the window
        scene = new Scene(root, windowWidth, windowHeight);
        // scene.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        // KEY PRESS
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent button) {
                switch(button.getCode()) {
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

                    default:;
                }
            } 
        });

        // KEY RELEASE
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent button) {
                switch(button.getCode()) {
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

                    default:;
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

                    if((goUP ^ goDOWN) || (goLEFT ^ goRIGHT)) { // if actually moving
                        // todo play animation
                    }
                    
                    if((goUP ^ goDOWN) && (goLEFT ^ goRIGHT)) { // if moving diagonally
                        speed *= sin45;
                    }
                    if(goUP) {
                        if(!checkCollision(posX, posY-speed)) posY -= speed;
                    }
                    if(goDOWN) {
                        if(!checkCollision(posX, posY+speed)) posY += speed;
                    }
                    if(goLEFT) {
                        if(!checkCollision(posX-speed, posY)) posX -= speed;
                        if(lastDirection == 0) {
                            sprite.setScaleX(-1);
                            lastDirection = 1;
                        }
                    }
                    if(goRIGHT) {
                        if(!checkCollision(posX+speed, posY)) posX += speed;
                        if(lastDirection != 0) {
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
                // sprite.setTranslateX((background.getImage().getWidth() / 2) - (imgWidth/2));
                // sprite.setTranslateY((background.getImage().getHeight() / 2) - (imgHeight/2));
                
                sprite.setTranslateX((windowWidth / 2) - (imgWidth/2));
                sprite.setTranslateY((windowHeight / 2) - (imgHeight/2));
            }
            );
        }

        public void doTask() {
            if(!doingTask && (collisionMask.getImage().getPixelReader().getColor((int)posX, (int)posY).equals(new Color(0,0,1, 1)))) {
                //doingTask = true;

                System.out.println("task");
            }
        }

        public void setPos(double x, double y) {
            this.posX = x;
            this.posY = y;
        }

        public void update() {
            updateAnim.handle(System.nanoTime());

            // set image pos (centered so coords aren't top-left of the image)
            //this.sprite.setTranslateX(posX - (imgWidth/2));
            //this.sprite.setTranslateY(posY - (imgHeight/2));
            //
            background.setTranslateX(-posX + (background.getImage().getWidth() / 2));
            background.setTranslateY(-posY + (background.getImage().getHeight() / 2));
            
        }

        public boolean checkCollision(double posX, double posY) {
            return (collisionMask.getImage().getPixelReader().getColor((int)posX, (int)posY).equals(new Color(0,0,0, 1)));
        }
    }

} // end class Races