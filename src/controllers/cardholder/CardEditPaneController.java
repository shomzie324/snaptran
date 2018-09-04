package controllers.cardholder;

import card.Card;
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
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class CardEditPaneController extends Controller implements Observer {

  private String cardId;
  @FXML private Label id;
  @FXML private Label status;
  @FXML private Label balance;
  @FXML private ListView<Label> recentTrips;
  @FXML private ComboBox<String> reportPeriodComboBox;

  /** Initialize the card edit page when user load it. */
  @FXML
  public void initialize() {
    Platform.runLater(
        () -> {
          initializeReportPeriodComboBox();
          displayRideRecord();
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
    if (observable instanceof Card && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("ACTIVE.") | message.contains("SUSPENDED.")) {
        MessageBox.display("Message", (String) arg);
      }
    }
  }

  /** Initialize the combo box options and set selection listener. */
  private void initializeReportPeriodComboBox() {
    List<String> reportPeriods = new ArrayList<>();
    reportPeriods.add("RECENT 3");
    reportPeriods.add("DAY");
    reportPeriods.add("WEEK");
    reportPeriods.add("MONTH");
    reportPeriods.add("ALL");
    reportPeriodComboBox.setItems(FXCollections.observableList(reportPeriods));
    reportPeriodComboBox.getSelectionModel().select(0); // view recent 3 ride records by default
    reportPeriodComboBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((v, oldValue, newValue) -> onComboBoxClicked(oldValue, newValue));
  }

  /**
   * Update ride records according to selected report period in combo box.
   *
   * @param oldValue old value selected
   * @param newValue new value selected
   */
  private void onComboBoxClicked(Object oldValue, Object newValue) {
    if (!oldValue.equals(newValue)) {
      displayRideRecord();
    }
  }

  /** Display ride record according to selection of report period in combo box. */
  private void displayRideRecord() {
    try {
      id.setText(cardId);
      status.setText(getTransitSystem().getCardholderManager().viewState(getUserId(), cardId));
      balance.setText(getTransitSystem().getCardholderManager().viewBalance(getUserId(), cardId));
      recentTrips.getItems().addAll(getRideRecordLabels());
    } catch (NoSuchCardholderException e) {
      MessageBox.display("Error", "Cannot Find Cardholder In System!");
    } catch (NoSuchCardException e) {
      MessageBox.display("Error", "Cannot Find This Card In System!");
    }
  }

  /**
   * Get labels with ride records displayed.
   *
   * @return a list of labels with ride records displayed.
   */
  private List<Label> getRideRecordLabels() {
    List<Label> labels = new ArrayList<>();
    try {
      String report =
          getTransitSystem()
              .getCardholderManager()
              .viewRecentTrips(getUserId(), cardId, reportPeriodComboBox.getValue());
      List<String> rides =
          new ArrayList<>(
              Arrays.asList(report.split(System.lineSeparator() + System.lineSeparator())));
      for (String ride : rides) {
        if (!ride.isEmpty()) {
          Label label = new Label();
          label.setStyle(
              "-fx-background-color: #c5e1a5; -fx-background-radius: 20;"
                  + " -fx-text-fill: black; -fx-text-alignment: center");
          label.setPrefWidth(220);
          label.setText(ride);
          label.setAlignment(Pos.CENTER);
          labels.add(label);
        }
      }
    } catch (NoSuchCardholderException e) {
      MessageBox.display("Error", "Cannot Find Cardholder In System!");
    } catch (NoSuchCardException e) {
      MessageBox.display("Error", "Cannot Find This Card In System!");
    }
    return labels;
  }

  /**
   * set the card id for this card edit page to get the information of this card.
   *
   * @param id the id of this card.
   */
  void setCardId(String id) {
    this.cardId = id;
  }

  /**
   * Display the transfer balance page when user clicks on transfer balance button.
   *
   * @param mouseEvent a mouse click event on transfer balance button.
   */
  public void onTransferButtonClicked(MouseEvent mouseEvent) throws IOException {
    FXMLLoader transferBalanceControllerLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/transferBalancePaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    ((BorderPane) ((BorderPane) scene.getRoot()).getCenter())
        .setCenter(transferBalanceControllerLoader.load());
    TransferBalancePaneController transferBalanceController =
        transferBalanceControllerLoader.getController();
    transferBalanceController.setTransitSystem(getTransitSystem());
    transferBalanceController.setUserId(getUserId());
    transferBalanceController.setId(cardId);
    getTransitSystem().getCardholderManager().addObserver(transferBalanceController);
  }

  /** Reactivate this card when user clicks on reactivate button. */
  public void onReactivateButtonClicked() {
    try {
      getTransitSystem().getCardholderManager().reactivateCard(getUserId(), cardId);
      status.setText("ACTIVE");
    } catch (NoSuchCardholderException e) {
      MessageBox.display("Error", "Cannot Find Cardholder In System!");
    } catch (NoSuchCardException e) {
      MessageBox.display("Error", "Cannot Find This Card In System!");
    }
  }

  /**
   * Back to the cards info page when user clicks the back button.
   *
   * @param mouseEvent a mouse click event on back button.
   */
  public void onBackButtonClicked(MouseEvent mouseEvent) throws IOException {
    FXMLLoader cardsPaneLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/cardsPaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* store previous pane displayed at centre of current scene in pane stack */
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    ((BorderPane) scene.getRoot()).setCenter(cardsPaneLoader.load());
    CardsPaneController view = cardsPaneLoader.getController();
    view.setTransitSystem(getTransitSystem());
    view.setUserId(getUserId());
  }

  /** Suspend this card when user clicks on suspend button. */
  public void onSuspendButtonClicked() {
    try {
      getTransitSystem().getCardholderManager().suspendCard(getUserId(), cardId);
      status.setText("SUSPENDED");
    } catch (NoSuchCardholderException e) {
      MessageBox.display("Error", "Cannot Find Cardholder In System!");
    } catch (NoSuchCardException e) {
      MessageBox.display("Error", "Cannot Find This Card In System!");
    }
  }
}
