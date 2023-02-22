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

    private final static String CREWMATE_IMAGE = "assets/amongus.png"; // file with icon for a crewmate
    private final static String CREWMATE_RUNNERS = "assets/amongusRunners.png"; // file with icon for crewmates
    private static final String BACKGROUND_IMAGE = "assets/background.jpg";
    private static final String COLLIDE_MASK_IMAGE = "assets/collision.png";

    private ImageView collisionMask = null;

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

        initializeScene();

    }

    // start the game scene
    public void initializeScene() {
        crewmate = new Crewmate();
        crewmate.setPos(200, 200);

        collisionMask = new ImageView(new File(COLLIDE_MASK_IMAGE).toURI().toString());
        root.getChildren().add(collisionMask);
        root.getChildren().add(crewmate);

        // display the window
        scene = new Scene(root, 800, 500);
        // scene.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();



        // KEY PRESS
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent button) {
                switch(button.getCode()) {
                    case UP:
                        goUP = true;
                        break;

                    case DOWN:
                        goDOWN = true;
                        break;

                    case LEFT:
                        goLEFT = true;
                        break;

                    case RIGHT:
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
                        goUP = false;
                        break;

                    case DOWN:
                        goDOWN = false;
                        break;

                    case LEFT:
                        goLEFT = false;
                        break;

                    case RIGHT:
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
            double speed = 5;

            if(goUP) {
                if(!checkCollision(posX, posY-speed)) posY -= speed;
            }
            if(goDOWN) {
                if(!checkCollision(posX, posY+speed)) posY += speed;
            }
            if(goLEFT) {
                if(!checkCollision(posX-speed, posY)) posX -= speed;
            }if(goRIGHT) {
                if(!checkCollision(posX+speed, posY)) posX += speed;
            }

            // set image pos (centered so coords aren't top-left of the image)
            this.sprite.setTranslateX(posX - (imgWidth/2));
            this.sprite.setTranslateY(posY - (imgHeight/2));

            // loop at screen edges
            if (posX > 800) posX = 0;
            if (posY > 500) posY = 0;

            if(posX < 0) posX = 800;
            if(posY < 0) posY = 500;
        }

        public boolean checkCollision(double posX, double posY) {
            return !(collisionMask.getImage().getPixelReader().getColor((int)posX, (int)posY).equals(new Color(0, 1, 0, 1)));
        }
    }

} // end class Races