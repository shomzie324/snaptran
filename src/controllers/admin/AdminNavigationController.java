package controllers.admin;

import component.MessageBox;
import component.PaneStack;
import controllers.NavigationController;
import exception.NoSuchAdminUserException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import system.AdminUser;

/**
 * A AdminNavigationController handles events happened on common navigation area on the admin user
 * interface of this application, including mouse clicks on go back to dash board button.
 */
public class AdminNavigationController extends NavigationController implements Observer {

  @FXML private Label loggedInEmployeeId;
  @FXML private ImageView profilePic;
  private AdminUser loggedInAdminUser;

  /** Initialize the admin user page when admin user load it. */
  @FXML
  public void initialize() {
    Platform.runLater(
        () -> {
          loggedInAdminUser = setLoggedInAdminUser();
          updateLoggedInAdminUserEmployeeId();
          updateProfilePic();
        });
  }

  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof AdminUser && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("Profile") && message.contains("Updated")) {
        updateProfilePic();
      }
    }
  }

  /** Update the UI display with the admin user's ID. */
  private void updateLoggedInAdminUserEmployeeId() {
    loggedInEmployeeId.setText("Employee ID: " + getUserId());
  }

  /** Update the UI with the admin user's profile picture. */
  protected void updateProfilePic() {
    profilePic.setImage(new Image(loggedInAdminUser.getProfilePic().toURI().toString()));
  }

  /**
   * get the current logged in admin user.
   *
   * @return the logged in AdminUser.
   */
  protected AdminUser getLoggedInAdminUser() {
    return loggedInAdminUser;
  }

  private AdminUser setLoggedInAdminUser() {
    AdminUser loggedInAdminUser = null;
    try {
      loggedInAdminUser = getTransitSystem().getAdminUser(getUserId());
    } catch (NoSuchAdminUserException e) {
      MessageBox.display("Error", "Employee ID " + getUserId() + " has not been registered!");
    }
    return loggedInAdminUser;
  }

  /**
   * Display admin user dash board page when the user click on dash board button.
   *
   * @param mouseEvent a mouse click on dash board button.
   */
  @FXML
  public void onDashBoardButtonClicked(MouseEvent mouseEvent) throws IOException {
    /* get the stage where this mouse click event happened */
    Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* add previous pane displayed to history*/
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* set up scene for policy management page on this stage */
    FXMLLoader adminDashBoardControllerLoader =
        new FXMLLoader(getClass().getResource("/views/admin/adminDashBoard.fxml"));
    ((BorderPane) scene.getRoot()).setCenter(adminDashBoardControllerLoader.load());
    primaryStage.setScene(scene);
    /* set up transit system model and current user for the controller of this scene */
    AdminDashBoardController adminDashBoardController =
        adminDashBoardControllerLoader.getController();
    adminDashBoardController.setTransitSystem(getTransitSystem());
    adminDashBoardController.setUserId(getUserId());
    /* add this policy management page controller to corresponding Observable class*/
    addObserverOfLoggedInAdminUser(adminDashBoardController);
    /* make this stage borderless movable */
    setWindowMovable(scene, primaryStage);
  }

  /** Add this observer to observers of this logged in observer. */
  protected void addObserverOfLoggedInAdminUser(Observer observer) {
    try {
      getTransitSystem().getAdminUser(getUserId()).addObserver(observer);
    } catch (NoSuchAdminUserException e) {
      MessageBox.display("Error", "Admin User " + getUserId() + " has not been registered!");
    }
  }
}
