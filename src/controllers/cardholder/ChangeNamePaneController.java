package controllers.cardholder;

import cardholder.Cardholder;
import component.ConfirmBox;
import component.MessageBox;
import component.PaneStack;
import controllers.Controller;
import exception.NoSuchCardholderException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/** A change name pane controller handles the events happened on a change name pane. */
public class ChangeNamePaneController extends Controller implements Observer {

  /* a text field where the cardholder enters new name */
  @FXML private TextField enterNameTextField;

  /**
   * Update to display message to user if got feedback in model said change is successfully made.
   *
   * @param observable Observable class that notified this change
   * @param arg message notified in this change
   */
  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof Cardholder && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("Change") && message.contains("Name")) {
        MessageBox.display("Message", (String) arg);
      }
    }
  }

  /**
   * If the cardholder entered a new name (not empty) and clicked save button, the change name pane
   * controller will talk to model about the change and display a message box to tell the cardholder
   * that name is successfully changed. If the cardholder left the text field empty and click save,
   * the change name pane controller will display a message box to notice the cardholder that name
   * cannot be empty.
   *
   * @param mouseEvent a mouse click event on save button on this change name pane.
   */
  @FXML
  public void onSaveButtonClicked(MouseEvent mouseEvent) throws IOException {
    if (enterNameTextField.getText().isEmpty()) { // invalid new name
      MessageBox.display("Notice", "Name Can Not Be Empty!");
    } else {
      String newName = enterNameTextField.getText();
      try {
        getTransitSystem().getCardholderManager().changeName(getUserId(), newName);
        loadUserInfoPane(mouseEvent);
      } catch (NoSuchCardholderException e) {
        MessageBox.display("Error", "Cannot Find Cardholder In System!");
      }
    }
  }

  /**
   * If the cardholder entered text on text field while clicked cancel button, the change name pane
   * controller will display a confirm box to notice user that there is unsaved change and confirm
   * whether the user want to leave. If the cardholder is sure to leave, display user info pane,
   * otherwise stay on this pane. If the cardholder left the text field empty and clicked cancel
   * button, the change name pane controller will display user info pane.
   *
   * @param mouseEvent a mouse click event on cancel button on this change name pane.
   */
  @FXML
  public void onCancelButtonClicked(MouseEvent mouseEvent) throws IOException {
    if (!enterNameTextField.getText().isEmpty()) {
      boolean leave = ConfirmBox.display("Confirm", "Unsaved changes. Sure to Leave?");
      if (leave) {
        loadUserInfoPane(mouseEvent);
      }
    } else {
      loadUserInfoPane(mouseEvent);
    }
  }

  /**
   * Load the user info pane to display on the center of current scene.
   *
   * @param mouseEvent a mouse click event on save/cancel button on this change name pane.
   */
  private void loadUserInfoPane(MouseEvent mouseEvent) throws IOException {
    /* load the user info pane and get the current scene displayed */
    FXMLLoader userInfoPaneLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/userInfoPaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* store the current pane displayed at the center of this scene in PaneStack */
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* display user info pane at the center of this scene */
    ((BorderPane) scene.getRoot()).setCenter(userInfoPaneLoader.load());
    /* set up transit system model and current logged in cardholder for the user info pane */
    UserInfoPaneController userInfoPaneController = userInfoPaneLoader.getController();
    userInfoPaneController.setTransitSystem(getTransitSystem());
    userInfoPaneController.setUserId(getUserId());
  }
}
