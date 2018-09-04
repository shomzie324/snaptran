package controllers.cardholder;

import card.Card;
import cardholder.Cardholder;
import component.MessageBox;
import component.PaneStack;
import controllers.Controller;
import exception.NoSuchCardException;
import exception.NoSuchCardholderException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class CardsPaneController extends Controller implements Observer {

  @FXML private ListView<HBox> cardsList;

  /** Initialize the cards page when user load it. */
  @FXML
  public void initialize() {
    Platform.runLater(
        () -> {
          HashMap<String, Card> myCards = getLoggedInCardholder().getMyCards();
          if (!myCards.isEmpty()) {
            ArrayList<String> allCardId = new ArrayList<>(myCards.keySet());
            for (String id : allCardId) {
              addLine(id);
            }
          }
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
    if (observable instanceof Cardholder && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("Got") && message.contains("New") && message.contains("Card")) {
        MessageBox.display("Message", "Successfully added new card!");
      } else if (message.contains("Removed") && message.contains("Card")) {
        MessageBox.display("Message", "Successfully removed card!");
      }
    }
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
   * Add a line when user clicks the add card button. The line contains the information of card id,
   * and user can enter the edit page or remove this card by clicking the button beside card id.
   *
   * @param cardId the id of the card.
   */
  private void addLine(String cardId) {
    Label cardIdLabel = new Label(cardId);
    cardIdLabel.setStyle("-fx-text-fill: black;");
    // Set style for edit button
    Button detailBtn = new Button("Detail");
    detailBtn.setStyle("-fx-background-color: #D84727;-fx-text-fill: white;");
    detailBtn.setOnMouseClicked(
        event -> {
          try {
            onEditButtonClicked(event, cardIdLabel.getText());
          } catch (IOException e) {
            e.getStackTrace();
          }
        });

    Button deleteBtn = new Button("Remove");
    deleteBtn.setStyle("-fx-background-color: #D84727;-fx-text-fill: white;");
    deleteBtn.setOnMouseClicked(event -> onRemoveButtonClicked(cardIdLabel.getText()));

    Label cardTextLabel = new Label("Card: ");
    HBox card = new HBox(50);
    Label cardBalanceLabel =
        new Label(
            "$" + Double.toString(getLoggedInCardholder().getMyCards().get(cardId).getBalance()));
    // Add all sub items to the HBox of each cell
    card.getChildren().addAll(cardTextLabel, cardIdLabel, cardBalanceLabel, detailBtn, deleteBtn);
    cardsList.getItems().add(card);
  }

  /**
   * Add a card when user clicks the add card button. User will see a message box if adding card
   * successfully.
   */
  public void onAddCardButtonClicked() {
    try {
      Card card = getTransitSystem().getCardManager().createNewCard();
      getTransitSystem().getCardholderManager().passNewCard(getUserId(), card);
      addLine(card.getCardId());
    } catch (NoSuchCardholderException e) {
      MessageBox.display("Error", "Cannot Find Cardholder In System!");
    }
  }

  /**
   * Display the card edit page when user clicks on edit button.
   *
   * @param mouseEvent a mouse click event on edit button.
   */
  private void onEditButtonClicked(MouseEvent mouseEvent, String cardId) throws IOException {
    FXMLLoader cardEditPaneControllerLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/cardEditPaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    ((BorderPane) ((BorderPane) scene.getRoot()).getCenter())
        .setCenter(cardEditPaneControllerLoader.load());
    CardEditPaneController cardEditPaneController = cardEditPaneControllerLoader.getController();
    cardEditPaneController.setTransitSystem(getTransitSystem());
    cardEditPaneController.setUserId(getUserId());
    cardEditPaneController.setCardId(cardId);
    for (Card card : getLoggedInCardholder().getMyCards().values()) {
      card.addObserver(cardEditPaneController);
    }
  }

  /**
   * Remove a card from the cardholder's cards. User will see a message box if remove the card
   * successfully.
   *
   * @param cardId the id of the card.
   */
  private void onRemoveButtonClicked(String cardId) {
    try {
      getTransitSystem().getCardholderManager().removeCard(getUserId(), cardId);
      for (HBox card : cardsList.getItems()) {
        if (((Label) card.getChildren().get(1)).getText().equals(cardId)) {
          cardsList.getItems().remove(card);
          break;
        }
      }
    } catch (NoSuchCardholderException e) {
      MessageBox.display("Error", "Cannot Find Cardholder In System!");
    } catch (NoSuchCardException e) {
      MessageBox.display("Error", "Cannot Find This Card In System!");
    }
  }

  /**
   * Display the add balance page when user clicks on add balance button.
   *
   * @param mouseEvent a mouse click event on add balance button.
   */
  public void onAddBalanceButtonClicked(MouseEvent mouseEvent) throws IOException {
    FXMLLoader addBalancePaneControllerLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/addBalancePaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    ((BorderPane) ((BorderPane) scene.getRoot()).getCenter())
        .setCenter(addBalancePaneControllerLoader.load());
    AddBalancePaneController controller = addBalancePaneControllerLoader.getController();
    controller.setTransitSystem(getTransitSystem());
    controller.setUserId(getUserId());
    for (Card card : getLoggedInCardholder().getMyCards().values()) {
      card.addObserver(controller);
    }
  }
}
