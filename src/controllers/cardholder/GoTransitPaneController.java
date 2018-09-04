package controllers.cardholder;

import card.Card;
import card.CardManager;
import cardholder.Cardholder;
import component.MessageBox;
import controllers.Controller;
import exception.NegativeBalanceException;
import exception.NoSuchCardholderException;
import fare.FareManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import map.Vertex;
import ride.Ride;

/** A go transit pane controller handles the events happened on go transit pane. */
public class GoTransitPaneController extends Controller implements Observer {

  @FXML private GridPane transitMap;
  @FXML private ComboBox<String> cards;
  @FXML private ComboBox<Ride.TransitType> transitType;
  private RadioButton selectedStation;
  private Circle markedStation;

  /** Set up ComboBox to let the user select transit type and card to tap with. */
  @FXML
  public void initialize() {
    List<Ride.TransitType> transitTypes = new ArrayList<>();
    transitTypes.add(Ride.TransitType.SUBWAY);
    transitTypes.add(Ride.TransitType.BUS);
    transitType.setItems(FXCollections.observableList(transitTypes));
    selectedStation = new RadioButton();

    Platform.runLater(
        () -> {
          displayTransitMap();

          List<String> cardInfo = new ArrayList<>();
          for (Card card : getLoggedInCardholder().getMyCards().values()) {
            String thisCardInfo = "Card ID: " + card.getCardId() + " $" + Double.valueOf(card.getBalance()).toString();
            cardInfo.add(thisCardInfo);
          }
          cards.setItems(FXCollections.observableList(cardInfo));
        });
  }

  /**
   * Update if this go transit pane controller is notified change.
   *
   * @param observable observable
   * @param arg argument
   */
  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof CardManager && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("Tapped") && message.contains("At")) {
        MessageBox.display("Message", (String) arg);
      }
    }
    if (observable instanceof FareManager && arg instanceof String) {
      MessageBox.display("Message", (String) arg);
    }
  }

  /** Initialize view of transit map on this go transit pane. */
  private void displayTransitMap() {
    for (Vertex vertex : getTransitSystem().getSystemMap().getGraph()) {
      RadioButton station = new RadioButton();
      setStationRadioButtonOnMouseClicked(station, vertex);
      Tooltip stationNameTip = new Tooltip(vertex.getValue());
      Tooltip.install(station, stationNameTip);
      GridPane.setColumnSpan(station, 5);
      GridPane.setColumnIndex(station, vertex.getCoordinate().getKey());
      GridPane.setRowIndex(station, vertex.getCoordinate().getValue());
      transitMap.getChildren().add(station);
    }
  }

  /**
   * Set up how to handle an event if a station radio button is clicked, so when a station is
   * clicked, the selected station will be marked.
   *
   * @param station station to be set up how to handle a mouse click event
   * @param vertex this station
   */
  private void setStationRadioButtonOnMouseClicked(RadioButton station, Vertex vertex) {
    station.setOnMouseClicked(
        event -> {
          if (selectedStation != null) {
            selectedStation.setText("");
            transitMap.getChildren().remove(markedStation);
          }
          selectedStation = station;
          station.setText(vertex.getValue());
          station.setTextFill(Color.DARKSLATEGRAY);
          station.setFont(Font.font(14));
          Circle circle = new Circle();
          markedStation = circle;
          circle.setRadius(10);
          circle.setFill(Color.RED);
          GridPane.setColumnIndex(circle, GridPane.getColumnIndex(station));
          GridPane.setRowIndex(circle, GridPane.getRowIndex(station));
          transitMap.getChildren().add(circle);
        });
  }

  /**
   * Validate tap in information entered by the user, display message if any part of information is
   * invalid. update model if all information is valid.
   */
  @FXML
  public void onTapInButtonClicked() throws Exception {
    String tappedInStationName = selectedStation.getText();
    if (validateTapInfo(tappedInStationName)) {
      Calendar tapInTime = Calendar.getInstance();
      tapInTime.setTimeInMillis(System.currentTimeMillis());
      selectedStation.setText("");
      transitMap.getChildren().remove(markedStation);
      String cardId = cards.getValue().split("$")[0].split("\\s+")[2];
      tapInInModel(cardId, tappedInStationName, tapInTime, transitType.getValue());
    }
  }

  /**
   * Check whether transit type, card, tap station is valid, display message if any part of the
   * information is invalid and return false. return true if all information is valid.
   *
   * @param stationName name of station that is tapped
   * @return whether all information entered by the user is valid
   */
  private boolean validateTapInfo(String stationName) {
    if (stationName.isEmpty()) {
      MessageBox.display("Notice", "Please select one station/stop!");
      return false;
    } else if (cards.getValue() == null) {
      MessageBox.display("Notice", "Please select a card!");
      return false;
    } else if (transitType.getValue() == null) {
      MessageBox.display("Notice", "Please select one transit type!");
      return false;
    } else if (!getTransitSystem()
        .getSystemMap()
        .getVerticesByTransitType(transitType.getValue())
        .contains(getTransitSystem().getSystemMap().getVertex(stationName))) {
      MessageBox.display(
          "Notice", stationName + " is not on a " + transitType.getValue() + " route!");
      return false;
    }
    return true;
  }

  /**
   * Update model when all tap in information is valid.
   *
   * @param cardId ID of card that is used to tap in
   * @param tapInStation name of tap in station
   * @param tapInTime tap in time
   * @param transitType transit type
   */
  private void tapInInModel(
      String cardId, String tapInStation, Calendar tapInTime, Ride.TransitType transitType)
      throws Exception {
    try {
      FareManager fareManager = getTransitSystem().getFareManager();
      fareManager.takeCharge(
              getTransitSystem().getCardManager().tapIn(cardId, tapInStation, tapInTime, transitType));
    } catch (NegativeBalanceException e) {
      MessageBox.display("Notice", "Card Balance is negative! Please add balance before tap in!");
    }

  }

  /**
   * Validate tap out information entered by the user, display message if any part of information is
   * invalid. update model if all information is valid.
   */
  @FXML
  public void onTapOutButtonClicked() throws Exception {
    String tapOutStation = selectedStation.getText();
    if (validateTapInfo(tapOutStation)) {
      String cardId = cards.getValue().split("$")[0].split("\\s+")[2];
      if (getLoggedInCardholder().getMyCards().get(cardId).getRideRecords().getLatestRide()
          != null) {
        String tappedInStationName =
            getLoggedInCardholder()
                .getMyCards()
                .get(cardId)
                .getRideRecords()
                .getLatestRide()
                .getTapInLocation();
        if (tappedInStationName != null && !tappedInStationName.equals("(Missed Tap In)")) {
          if (Double.POSITIVE_INFINITY
              == getTransitSystem()
                  .getSystemMap()
                  .getShortestPath(tappedInStationName, tapOutStation)
                  .getLast()
                  .getDistance()) {
            MessageBox.display("Notice", "Invalid Tap Out Location! Cannot reach!");
          } else {
            Calendar tapOutTime = Calendar.getInstance();
            tapOutTime.setTimeInMillis(System.currentTimeMillis());
            selectedStation.setText("");
            transitMap.getChildren().remove(markedStation);
            tapOutInModel(cardId, tapOutStation, tapOutTime, transitType.getValue());
          }
        }
      }
    }
  }

  /**
   * Update model when all tap out information is valid.
   *
   * @param cardId ID of card that is used to tap out
   * @param tapOutStation name of tap out station
   * @param tapOutTime tap out time
   * @param transitType transit type
   */
  private void tapOutInModel(
      String cardId, String tapOutStation, Calendar tapOutTime, Ride.TransitType transitType)
      throws Exception {
    FareManager fareManager = getTransitSystem().getFareManager();
    fareManager.takeCharge(
        getTransitSystem().getCardManager().tapOut(cardId, tapOutStation, tapOutTime, transitType));
  }

  /**
   * Get the instance of this logged in cardholder.
   *
   * @return the instance of this logged in cardholder.
   */
  private Cardholder getLoggedInCardholder() {
    Cardholder result = null;
    try {
      result = getTransitSystem().getCardholderManager().checkCardholder(getUserId());
    } catch (NoSuchCardholderException e) {
      MessageBox.display("Notice", "Cardholder Email " + getUserId() + " has not been registered!");
    }
    return result;
  }
}
