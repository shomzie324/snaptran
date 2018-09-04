package controllers.cardholder;

import card.Card;
import component.ConfirmBox;
import component.MessageBox;
import component.PaneStack;
import controllers.Controller;
import exception.NoSuchCardException;
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

public class AddBalancePaneController extends Controller implements Observer {

  @FXML private TextField cardIdTextField;
  @FXML private TextField amountTextField;

  /**
   * Update to display message to user if got feedback in model said change is successfully made.
   *
   * @param observable Observable class that notified this change
   * @param arg message notified in this change
   */
  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof Card && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("Balance") && message.contains("Added")) {
        MessageBox.display("Message", (String) arg);
      }
    }
  }

  /**
   * Save the changes(adding balance) the user has made. User will see a message box when they
   * clicked the save button and get corresponding information.
   *
   * @param mouseEvent a mouse click event on save button.
   */
  @FXML
  public void onSaveButtonClicked(MouseEvent mouseEvent) throws IOException {
    if (cardIdTextField.getText().isEmpty()) { // invalid new name
      MessageBox.display("Notice", "Card Id Can Not Be Empty!");
    } else if (amountTextField.getText().isEmpty()) {
      MessageBox.display("Notice", "Amount Can Not Be Empty!");
    } else if (!Double.valueOf(amountTextField.getText()).equals(10.0)
        && !Double.valueOf(amountTextField.getText()).equals(20.0)
        && !Double.valueOf(amountTextField.getText()).equals(50.0)) {
      MessageBox.display("Notice", "Invalid Amount!");
    } else {
      String cardId = cardIdTextField.getText();
      try {
        boolean add = true;
        if (getTransitSystem()
                .getCardholderManager()
                .viewState(getUserId(), cardId)
                .equals("SUSPENDED")
            || getTransitSystem()
                .getCardholderManager()
                .viewState(getUserId(), cardId)
                .equals("REMOVED")) {
          add = ConfirmBox.display("Confirm", "This Card Is Not Active. Sure to Add Balance?");
        }
        if (add) {
          getTransitSystem()
              .getCardManager()
              .addBalance(cardId, Double.valueOf(amountTextField.getText()));
          loadCardsPane(mouseEvent);
        }
      } catch (NoSuchCardException e) {
        MessageBox.display("Notice", "Failed To Find Card: " + cardId + "!");
      } catch (NoSuchCardholderException e) {
        MessageBox.display("Notice", "Cannot Find Cardholder In System!");
      }
    }
  }

  /**
   * Back to the cards information page of a cardholder. User will see a message box if they make a
   * change but haven't save it.
   *
   * @param mouseEvent a mouse click event on cancel button.
   */
  @FXML
  public void onCancelButtonClicked(MouseEvent mouseEvent) throws IOException {
    boolean leave = true;
    if (!(cardIdTextField.getText().isEmpty() && amountTextField.getText().isEmpty())) {
      leave = ConfirmBox.display("Confirm", "Unsaved changes. Sure to Leave?");
    }
    if (leave) {
      loadCardsPane(mouseEvent);
    }
  }

  /**
   * Display cards information of a cardholder when successfully add the balance.
   *
   * @param mouseEvent a mouse click event on save button.
   */
  private void loadCardsPane(MouseEvent mouseEvent) throws IOException {
    FXMLLoader cardsPaneLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/cardsPaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* store previous pane displayed at centre of current scene in pane stack */
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* display user info pane on current scene */
    ((BorderPane) scene.getRoot()).setCenter(cardsPaneLoader.load());
    /* set up model for this user info pane */
    CardsPaneController view = cardsPaneLoader.getController();
    view.setTransitSystem(getTransitSystem());
    view.setUserId(getUserId());
  }
}
