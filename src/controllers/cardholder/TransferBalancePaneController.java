package controllers.cardholder;

import card.Card;
import cardholder.CardholderManager;
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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class TransferBalancePaneController extends Controller implements Observer {

  @FXML private Label cardId1;
  @FXML private TextField cardId2;
  private String id;

  /** Initialize the transfer balance page when user load it. */
  @FXML
  public void initialize() {
    Platform.runLater(
        () -> {
          cardId1.setText(id);
        });
  }

  /**
   * Update to display message to user if got feedback in model said change is successfully made.
   *
   * @param observable Observable class that notified this change
   * @param arg message notified in this change
   */
  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof CardholderManager && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("Transfer") && message.contains("Balance")) {
        MessageBox.display("Message", "Successfully transfer balance!");
      }
    }
  }

  /**
   * Set the card id of this page.
   *
   * @param id the id of this card.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Transfer the balance from this card to another card. After transferring, this card will be
   * removed and user will return to the cards page. User will see the message box corresponding to
   * changes.
   *
   * @param mouseEvent a mouse click event on save button.
   */
  public void onSaveButtonClicked(MouseEvent mouseEvent) throws IOException {
    if (cardId2.getText().isEmpty()) {
      MessageBox.display("Error", "Card ID Cannot Be Empty!");
    } else {
      try {
        if (!getTransitSystem()
            .getCardholderManager()
            .viewState(getUserId(), id)
            .equals("SUSPENDED")) {
          MessageBox.display("Error", "Cannot Transfer: Card Is Not Suspended!");
        } else if (Double.valueOf(
                getTransitSystem().getCardholderManager().viewBalance(getUserId(), id))
            < 0) {
          MessageBox.display("Error", "Cannot Transfer: Card Balance Is Negative!");
        } else {
          Card card2 = getTransitSystem().getCardManager().checkCard(cardId2.getText());
          getTransitSystem().getCardholderManager().transferBalance(getUserId(), id, card2);
          loadCardsPane(mouseEvent);
        }
      } catch (NoSuchCardException e) {
        MessageBox.display("Error", "Cannot Find This Card In System!");
      } catch (NoSuchCardholderException e) {
        MessageBox.display("Error", "Cannot Find Cardholder In System!");
      }
    }
  }

  /**
   * Display the card edit page when user clicks on transfer balance button.
   *
   * @param mouseEvent a mouse click event on cancel button.
   */
  public void onCancelButtonClicked(MouseEvent mouseEvent) throws IOException {
    FXMLLoader cardEditPaneControllerLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/cardEditPaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    ((BorderPane) ((BorderPane) scene.getRoot()).getCenter())
        .setCenter(cardEditPaneControllerLoader.load());
    CardEditPaneController cardEditPaneController = cardEditPaneControllerLoader.getController();
    cardEditPaneController.setTransitSystem(getTransitSystem());
    cardEditPaneController.setUserId(getUserId());
    cardEditPaneController.setCardId(id);
  }

  /**
   * Display the cards page when user clicks on save button and saves changes successfully.
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
