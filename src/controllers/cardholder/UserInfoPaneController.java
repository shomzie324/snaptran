package controllers.cardholder;

import cardholder.Cardholder;
import component.MessageBox;
import component.PaneStack;
import controllers.Controller;
import exception.NoSuchCardholderException;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * A user info page can display and update the view at center of the scene when cardholder view user
 * info.
 */
public class UserInfoPaneController extends Controller {

  @FXML private Label userName;
  @FXML private Label userEmail;
  @FXML private Label userRegisterDate;

  /** Initialize view of user info dash board. */
  @FXML
  public void initialize() {
    Platform.runLater(
        () -> {
          Cardholder cardholder = getLoggedInCardholder();
          userName.setText(cardholder.getName());
          userEmail.setText(cardholder.getEmail());
          userRegisterDate.setText(cardholder.registerDateToString());
        });
  }

  /**
   * Get the logged in cardholder from cardholderManager of this transit system. Display a message
   * box to tell the user that this email has not been registered as cardholder if the cardholder
   * manager cannot find this cardholder in the cardholder pool.
   *
   * @return the Cardholder instance that logged in this transit system
   */
  private Cardholder getLoggedInCardholder() {
    Cardholder loggedInCardholder = null;
    try {
      loggedInCardholder = getTransitSystem().getCardholderManager().checkCardholder(getUserId());
    } catch (NoSuchCardholderException e) {
      MessageBox.display("Error", "Cardholder Email " + getUserId() + " has not been registered!");
    }
    return loggedInCardholder;
  }

  /**
   * Display change name pane when the cardholder click on edit button on this user info pane.
   *
   * @param mouseEvent a mouse click event on edit button on this user info pane.
   */
  @FXML
  public void onEditButtonClicked(MouseEvent mouseEvent) throws IOException {
    /* load change name pane and get current scene displayed */
    FXMLLoader changeNamePaneControllerLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/changeNamePaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* store previous pane displayed at centre of current scene in pane stack */
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* display user info pane on current scene */
    ((BorderPane) scene.getRoot()).setCenter(changeNamePaneControllerLoader.load());
    /* set up model for this user info pane */
    ChangeNamePaneController changeNamePaneController =
        changeNamePaneControllerLoader.getController();
    changeNamePaneController.setTransitSystem(getTransitSystem());
    changeNamePaneController.setUserId(getUserId());
    getLoggedInCardholder().addObserver(changeNamePaneController);
  }

  /** Get the average month fare when user clicks on the get average month fare button. */
  @FXML
  public void onGetFareButtonClicked() {
    if (!getTransitSystem().getCardholderManager().hasRecord(getUserId())) {
      MessageBox.display("Error", "No Record Yet.");
    } else {
      try {
        MessageBox.display(
            "Message",
            "The Average Month Fare Is "
                + getTransitSystem().getCardholderManager().getAverageMonthFare(getUserId()));
      } catch (NoSuchCardholderException e) {
        MessageBox.display("Error", "Cannot Find Cardholder In System!");
      }
    }
  }
}
