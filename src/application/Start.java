package application;

import controllers.register.LoginPageController;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import system.TransitSystem;

/** An application of transit system. */
public class Start extends Application {

  /**
   * Launch this application.
   *
   * @param args argument
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Initialize and display the application window and initial scene when this application is
   * launched.
   *
   * @param primaryStage the stage to display on.
   */
  @Override
  public void start(Stage primaryStage) throws IOException {
    /* get an instance of the transit system model */
    TransitSystem transitSystem = TransitSystem.getInstance();

    /* load view from fxml file and set up view of this scene */
    FXMLLoader loginPageLoader =
        new FXMLLoader(getClass().getResource("/views/register/loginPage.fxml"));
    Pane root = loginPageLoader.load();
    Scene scene = new Scene(root, 600, 400);
    primaryStage.setScene(scene);

    /* set up transit system model for the controller of log in page */
    LoginPageController loginPageController = loginPageLoader.getController();
    loginPageController.setTransitSystem(transitSystem);
    /* set up this stage */
    loginPageController.setWindowMovable(scene, primaryStage);
    primaryStage.initStyle(StageStyle.UNDECORATED);
    primaryStage.setResizable(true);
    primaryStage.show();
  }
}
