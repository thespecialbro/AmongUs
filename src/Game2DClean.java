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

    private final static String CREWMATE_IMAGE = "amongus.png"; // file with icon for a crewmate
    private final static String CREWMATE_RUNNERS = "amongusRunners.png"; // file with icon for crewmates
    private static final String BACKGROUND_IMAGE = "background.jpg"; //

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
        private int posX = 0;
        private int posY = 0;
        private ImageView aPicView = null;
        private double imgWidth;
        private double imgHeight;

        public Crewmate() {
            aPicView = new ImageView(CREWMATE_IMAGE);
            this.getChildren().add(aPicView);

            imgWidth = aPicView.getImage().getWidth();
            imgHeight = aPicView.getImage().getHeight();
        }

        public void update() {
            double speed = 5;

            if(goUP) posY -= speed;
            if(goDOWN) posY += speed;
            if(goLEFT) posX -= speed;
            if(goRIGHT) posX += speed;

            // set image pos (centered so coords aren't top-left of the image)
            this.aPicView.setTranslateX(posX - (imgWidth/2));
            this.aPicView.setTranslateY(posY - (imgHeight/2));

            // loop at screen edges
            if (posX > 800) posX = 0;
            if (posY > 500) posY = 0;

            if(posX < 0) posX = 800;
            if(posY < 0) posY = 500;
        }
    }

} // end class Races