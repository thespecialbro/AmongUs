import javafx.application.*;
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
import java.net.Socket;
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

    Crewmate crewmate = null;

    // client stuff
    public static final int SERVER_PORT = 12345;
    Socket socket = null;
    private String ip = "127.0.0.1";

    // main program
    public static void main(String[] _args) {
        args = _args;
        launch(args);
    }

    public Game2DClean(String ip) {
        
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

            backgroundImage = String.format("levels/%s/background.png", settingsMap.get("level"));
            collideMaskImage = String.format("levels/%s/collide_mask.png", settingsMap.get("level"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // start the game scene
    public void initializeScene() {
        crewmate = new Crewmate();
        crewmate.setPos(500, 500);

        collisionMask = new ImageView(new File(collideMaskImage).toURI().toString());
        background = new ImageView(new File(backgroundImage).toURI().toString());
        
        root.getChildren().add(background);
        //root.getChildren().add(collisionMask); // debug
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
                switch(button.getCode()) {
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

        public Crewmate() {
            sprite = new ImageView(new File(CREWMATE_IMAGE).toURI().toString());
            this.getChildren().add(sprite);

            imgWidth = sprite.getImage().getWidth();
            imgHeight = sprite.getImage().getHeight();
        }

        public void setPos(double x, double y) {
            this.posX = x;
            this.posY = y;
        }

        public void update() {
            double speed = 2;
        
            if (goUP || goDOWN || goRIGHT || goLEFT) {
                // do animation
            }
        
            // Check diagonal movement first
            if (goUP && goLEFT) {
                if (!checkCollision(posX - speed, posY - speed)) {
                    posX -= speed;
                    posY -= speed;
                }
            } else if (goUP && goRIGHT) {
                if (!checkCollision(posX + speed, posY - speed)) {
                    posX += speed;
                    posY -= speed;
                }
            } else if (goDOWN && goLEFT) {
                if (!checkCollision(posX - speed, posY + speed)) {
                    posX -= speed;
                    posY += speed;
                }
            } else if (goDOWN && goRIGHT) {
                if (!checkCollision(posX + speed, posY + speed)) {
                    posX += speed;
                    posY += speed;
                }
            } else {
                // Check individual movements
                if (goUP) {
                    if (!checkCollision(posX, posY - speed)) posY -= speed;
                }
                if (goDOWN) {
                    if (!checkCollision(posX, posY + speed)) posY += speed;
                }
                if (goLEFT) {
                    if (!checkCollision(posX - speed, posY)) posX -= speed;
                }
                if (goRIGHT) {
                    if (!checkCollision(posX + speed, posY)) posX += speed;
                }
            }
        
            // set image pos (centered so coords aren't top-left of the image)
            this.sprite.setTranslateX(posX - (imgWidth / 2));
            this.sprite.setTranslateY(posY - (imgHeight / 2));
        }

        public boolean checkCollision(double posX, double posY) {
            return !(collisionMask.getImage().getPixelReader().getColor((int)posX, (int)posY).equals(new Color(200.0/255, 162.0/255, 200.0/255, 1)));
        }
    }

} // end class Races

