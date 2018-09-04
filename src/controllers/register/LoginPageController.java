package controllers.register;

import cardholder.Cardholder;
import component.MessageBox;
import controllers.WindowController;
import controllers.admin.AdminDashBoardController;
import controllers.cardholder.DashBoardMainController;
import controllers.cardholder.DashBoardPaneController;
import exception.NoSuchAdminUserException;
import exception.NoSuchCardholderException;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import system.AdminUser;

/** A log in page controller handles events happened on log in page. */
public class LoginPageController extends WindowController {

  @FXML private TextField userIdTextField;
  @FXML private TextField passwordTextField;

  /**
   * Display cardholder dash board when the user click the cardholder log in button.
   *
   * @param mouseEvent a mouse click on cardholder log in button.
   */
  @FXML
  public void onCardholderLogInButtonClicked(MouseEvent mouseEvent) throws IOException {
    if (validateCardholder()) { // if this cardholder is validated to log in to cardholder dashboard
      /* get the stage where this mouse click event happened */
      Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
      /* set up scene for user info page on this stage */
      FXMLLoader cardholderViewLoader =
          new FXMLLoader(getClass().getResource("/views/cardholder/dashBoardMainView.fxml"));
      Pane root = cardholderViewLoader.load();
      Scene scene = new Scene(root, 800, 500);
      primaryStage.setScene(scene);
      /* set up transit system model and current user for the controller of this scene */
      DashBoardMainController dashBoardMainController = cardholderViewLoader.getController();
      dashBoardMainController.setTransitSystem(getTransitSystem());
      dashBoardMainController.setUserId(getUserId());
      /* add this dash board main controller to Observable class to update changes */
      getTransitSystem().getCardManager().addObserver(dashBoardMainController);
      /* load dash board pane and set it to the center of current scene */
      FXMLLoader dashBoardPaneLoader =
          new FXMLLoader(getClass().getResource("/views/cardholder/dashBoardPaneView.fxml"));
      /* display dash board pane on current scene */
      BorderPane center = dashBoardPaneLoader.load();
      ((BorderPane) scene.getRoot()).setCenter(center);
      /* set up model for this dash board pane */
      DashBoardPaneController dashBoardPaneController = dashBoardPaneLoader.getController();
      dashBoardPaneController.setTransitSystem(getTransitSystem());
      dashBoardPaneController.setUserId(getUserId());
      dashBoardPaneController.setLayout(center);
      /* set up stage */
      setWindowMovable(scene, primaryStage);
    }
  }

  /**
   * Check whether this cardholder has registered in this transit system and whether the password
   * entered is correct. Display notice about which part of information is invalid to this
   * cardholder if needed.
   *
   * @return whether this cardholder is validated to log in to cardholder dash board.
   */
  private boolean validateCardholder() {
    String cardholderEmail = userIdTextField.getText();
    String password = passwordTextField.getText();
    if (cardholderEmail.isEmpty() | password.isEmpty()) {
      displayEmptyInputNoticeToCardholder(cardholderEmail, password);
    } else if (!cardholderEmail.matches("^[a-z]+@[a-z]+\\.[a-z]+$")) {
      MessageBox.display("Error", "Please enter valid email address!");
    } else {
      Cardholder cardholder = null;
      try {
        cardholder = getTransitSystem().getCardholderManager().checkCardholder(cardholderEmail);
      } catch (NoSuchCardholderException e) {
        MessageBox.display("Error", "This email has not registered as a cardholder!");
      }
      if (cardholder != null) {
        if (cardholder.verifyPassword(password)) {
          setUserId(cardholder.getEmail()); // record current user
          return true;
        } else {
          MessageBox.display("Error", "Password is incorrect!");
        }
      }
    }
    return false;
  }

  /**
   * Display a specific notice to this cardholder about which part of information should not be
   * empty.
   *
   * @param cardholderEmail cardholderEmail entered by this cardholder
   * @param password password entered by this cardholder
   */
  private void displayEmptyInputNoticeToCardholder(String cardholderEmail, String password) {
    String message = "";
    if (cardholderEmail.isEmpty()) {
      message += "email ";
    }
    if (password.isEmpty()) {
      message += "password ";
    }
    MessageBox.display("Error", "Cardholder " + message + "cannot be empty!");
  }

  /**
   * Display admin user dash board when the user click the admin user log in button.
   *
   * @param mouseEvent a mouse click on admin user log in button.
   */
  @FXML
  public void onAdminButtonLogInClicked(MouseEvent mouseEvent) throws IOException {
    if (validateAdminUser()) { // if this admin user is validated to log in to admin user dashboard
      /* get the stage where this mouse click event happened */
      Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
      /* set up scene for user info page on this stage */
      FXMLLoader adminPageControllerLoader =
          new FXMLLoader(getClass().getResource("/views/admin/adminDashBoard.fxml"));
      Pane root = adminPageControllerLoader.load();
      Scene scene = new Scene(root, 800, 500);
      primaryStage.setScene(scene);
      /* set up transit system model and current user for the controller of this scene */
      AdminDashBoardController adminDashBoardController = adminPageControllerLoader.getController();
      adminDashBoardController.setTransitSystem(getTransitSystem());
      adminDashBoardController.setUserId(getUserId());
      try {
        getTransitSystem().getAdminUser(getUserId()).addObserver(adminDashBoardController);
      } catch (NoSuchAdminUserException e) {
        MessageBox.display("Error", "Admin User " + getUserId() + " has not been registered!");
      }
      /* set up stage */
      setWindowMovable(scene, primaryStage);
    }
  }

  /**
   * Check whether this admin user has registered in this transit system and whether the password
   * entered is correct. Display notice about which part of information is invalid to this admin
   * user if needed.
   *
   * @return whether this admin is validated to log in to admin user dash board.
   */
  private boolean validateAdminUser() {
    String employeeId = userIdTextField.getText();
    String password = passwordTextField.getText();
    if (employeeId.isEmpty() | password.isEmpty()) {
      displayEmptyInputNoticeToAdminUser(employeeId, password);
    } else if (!employeeId.matches("[0-9]+")) {
      MessageBox.display("Error", "Please enter valid employee ID!");
    } else {
      AdminUser adminUser = null;
      try {
        adminUser = getTransitSystem().getAdminUser(employeeId);
      } catch (NoSuchAdminUserException e) {
        MessageBox.display("Error", "This employee ID has not been registered!");
      }
      if (adminUser != null) {
        if (adminUser.verifyPassword(password)) {
          setUserId(employeeId); // record current user
          return true;
        } else {
          MessageBox.display("Error", "Password is incorrect!");
        }
      }
    }
    return false;
  }

  /**
   * Display a specific notice to this admin user about which part of information should not be
   * empty.
   *
   * @param employeeId employee ID entered by this admin user
   * @param password password entered by this admin user
   */
  private void displayEmptyInputNoticeToAdminUser(String employeeId, String password) {
    String message = "";
    if (employeeId.isEmpty()) {
      message += "employee ID ";
    }
    if (password.isEmpty()) {
      message += "password ";
    }
    MessageBox.display("Error", "AdminUser " + message + "cannot be empty!");
  }

  /**
   * Display register page on current stage.
   *
   * @param mouseEvent a mouse click event on register button
   */
  @FXML
  public void onRegisterButtonClicked(MouseEvent mouseEvent) throws Exception {
    /* get the stage where this mouse click event happened */
    Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
    /* set up scene for user info page on this stage */
    FXMLLoader registerAccountControllerLoader =
        new FXMLLoader(getClass().getResource("/views/register/registerAccountPage.fxml"));
    Pane root = registerAccountControllerLoader.load();
    Scene scene = new Scene(root, 600, 400);
    primaryStage.setScene(scene);
    /* set up transit system model for the controller of this scene */
    RegisterAccountPageController registerAccountPageController =
        registerAccountControllerLoader.getController();
    registerAccountPageController.setTransitSystem(getTransitSystem());
    /* add this register page controller to corresponding Observable class */
    getTransitSystem().addObserver(registerAccountPageController);
    getTransitSystem().getCardholderManager().addObserver(registerAccountPageController);
    /* set up stage */
    setWindowMovable(scene, primaryStage);
  }
}
