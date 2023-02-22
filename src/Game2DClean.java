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

      // display the window
      scene = new Scene(root, 800, 500);
      // scene.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());
      stage.setScene(scene);
      stage.show();
   }

   class Crewmate extends Pane {
      private int posX = 0;
      private int posY = 0;
      private ImageView aPicView = null;

      public Crewmate() {
         aPicView = new ImageView(CREWMATE_IMAGE);
         this.getChildren().add(aPicView);
      }

      public void update() {
         double speed = 5;

         posX += (Math.random() - 0.5) * speed;
         posY += (Math.random() - 0.5) * speed;

         // set image pos
         this.aPicView.setTranslateX(posX);
         this.aPicView.setTranslateY(posY);

         // loop at screen edges
         if(posX > 800) posX = 0;
         if(posY > 500) posY = 0;
      }
   }

} // end class Races