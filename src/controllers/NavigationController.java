package controllers;

import component.PaneStack;
import controllers.register.LoginPageController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * A navigation controller handles events happened on common navigation area of admin user interface
 * and cardholder interface of this application.
 */
public abstract class NavigationController extends WindowController {

  /**
   * Display log in page when the cardholder/admin user click on log out button.
   *
   * @param mouseEvent a mouse click on log out button.
   */
  public void onToLogInPageButtonClicked(MouseEvent mouseEvent) throws Exception {
    PaneStack.clear();
    /* get the stage where this mouse click event happened */
    Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
    /* set up scene for user info page on this stage */
    FXMLLoader loginPageControllerLoader =
        new FXMLLoader(getClass().getResource("/views/register/loginPage.fxml"));
    Pane root = loginPageControllerLoader.load();
    Scene scene = new Scene(root, 600, 400);
    primaryStage.setScene(scene);
    /* set up transit system model for the controller of this scene */
    LoginPageController loginPageController = loginPageControllerLoader.getController();
    loginPageController.setTransitSystem(getTransitSystem());
    /* set up stage */
    setWindowMovable(scene, primaryStage);
  }

  /**
   * Display the latest previous pane at center of current scene if there is one when the cardholder
   * clicked on go back button.
   *
   * @param mouseEvent a mouse click event on go back button on dash board navigation area.
   */
  @FXML
  public void onGoBackButtonClicked(MouseEvent mouseEvent) {
    Pane prev = PaneStack.pop();
    if (prev != null) { // if there is a previous pane
      Scene scene = ((Node) mouseEvent.getSource()).getScene();
      ((BorderPane) scene.getRoot()).setCenter(prev);
    }
  }
}
