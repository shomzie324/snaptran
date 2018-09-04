package controllers.cardholder;

import card.Card;
import cardholder.Cardholder;
import component.MessageBox;
import controllers.Controller;
import exception.NoSuchCardholderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import ride.Ride;
import ride.RideRecord;

/** A dash board layout view can update view of center of dash board page. */
public class DashBoardPaneController extends Controller {

  @FXML private LineChart<String, Double> fareLineChart;
  @FXML private PieChart costCompositionPieChart;
  @FXML private ComboBox<String> cardIdComboBox;
  @FXML private ComboBox<String> reportPeriodComboBox;
  private BorderPane layout; // layout of this dash board pane, update according to user's status

  /**
   * Set the layout of this dash board pane.
   *
   * @param layout lay out of this dash board pane.
   */
  public void setLayout(BorderPane layout) {
    this.layout = layout;
  }

  /** Initialize welcome title, cardId options, report period options and fare line chart. */
  @FXML
  public void initialize() {
    Platform.runLater(
        () -> {
          initializeWelcomeTitle();
          initializeCardIdComboBox();
          initializeReportPeriodComboBox();
          displayFareLineChart();
          displayCostCompositionPieChart();
        });
  }

  /** Helper method to initialize welcome title. */
  private void initializeWelcomeTitle() {
    HBox welcomeTitle = new HBox();
    Label cardholderName = new Label("Welcome" + "  " + getLoggedInCardholder().getName());
    cardholderName.setStyle("-fx-font-size: 36");
    cardholderName.setAlignment(Pos.CENTER);
    welcomeTitle.getChildren().add(cardholderName);
    welcomeTitle.setAlignment(Pos.CENTER);
    this.layout.setTop(welcomeTitle);
  }

  /** Helper method to initialize cardId options. */
  private void initializeCardIdComboBox() {
    int indexOfMostFrequentlyUsedCard = 0;
    int numOfRidesOfMostFrequentlyUsedCard = 0;
    List<String> cardIdList = new ArrayList<>();
    List cards =
        new ArrayList<>(Arrays.asList(getLoggedInCardholder().getMyCards().values().toArray()));
    for (int i = 0; i <= cards.size() - 1; i++) {
      Card card = (Card) cards.get(i);
      String thisCardId =
          "Card Id: " + card.getCardId() + " $" + Double.toString(card.getBalance());
      cardIdList.add(thisCardId);
      int numOfRideThisCard = card.getRideRecords().getAllRides().size();
      if (numOfRideThisCard >= numOfRidesOfMostFrequentlyUsedCard) {
        numOfRidesOfMostFrequentlyUsedCard = numOfRideThisCard;
        indexOfMostFrequentlyUsedCard = i;
      }
    }
    cardIdComboBox.getItems().addAll(cardIdList);
    cardIdComboBox.getSelectionModel().select(indexOfMostFrequentlyUsedCard);
    cardIdComboBox.setOnAction(event -> onComboBoxClicked());
  }

  /** Helper method to initialize report period options. */
  private void initializeReportPeriodComboBox() {
    List<String> reportPeriods = new ArrayList<>();
    reportPeriods.add("RECENT 10");
    reportPeriods.add("DAY");
    reportPeriods.add("WEEK");
    reportPeriods.add("MONTH");
    reportPeriodComboBox.setItems(FXCollections.observableList(reportPeriods));
    reportPeriodComboBox.getSelectionModel().select(0);
    reportPeriodComboBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((v, oldValue, newValue) -> onComboBoxClicked());
  }

  /** Update view of fare line chart if user make change on selection of combo box. */
  private void onComboBoxClicked() {
    displayFareLineChart();
    displayCostCompositionPieChart();
  }

  /** Display the fare line chart. */
  private void displayFareLineChart() {
    fareLineChart.getData().clear();
    String reportPeriod = reportPeriodComboBox.getValue();
    if (reportPeriod.equals("RECENT 10")) {
      displayFareLineChartOfRecentTenRides();
    } else {
      displayFareLineChartByTimePeriod(reportPeriod);
    }
    fareLineChart.setTitle(getLineChartTitle(reportPeriod));
  }

  /** Display the fare of most recent 10 rides. */
  private void displayFareLineChartOfRecentTenRides() {
    if (cardIdComboBox.getItems().size() != 0) {
      String cardId = cardIdComboBox.getValue().split("$")[0].split("\\s+")[2];
      List<Ride> rides =
          getTransitSystem()
              .getCardManager()
              .getCardPool()
              .get(cardId)
              .getRideRecords()
              .getAllRides();
      if (rides.size() != 0) {
        fareLineChart.getData().add(getDataOfRecentTenRides(rides));
      }
    }
  }

  /**
   * Get the data of 10 most recent rides to display on fare line chart.
   *
   * @param rides all ride records
   * @return data of 10 most recent rides to display on fare line chart.
   */
  private XYChart.Series<String, Double> getDataOfRecentTenRides(List<Ride> rides) {
    XYChart.Series<String, Double> series = new XYChart.Series<>();
    int counter = 0;
    for (int i = rides.size() - 1; i >= 0 && counter <= 7; i--) {
      Ride ride = rides.get(i);
      String category;
      Calendar time = ride.getTapOutTime();
      if (time == null) {
        time = ride.getTapInTime();
      }
      category =
          Integer.toString(time.get(Calendar.YEAR))
              + "-"
              + Integer.toString(time.get(Calendar.MONTH) + 1)
              + "-"
              + Integer.toString(time.get(Calendar.DAY_OF_MONTH))
              + System.lineSeparator()
              + Integer.toString(time.get(Calendar.HOUR_OF_DAY))
              + ":"
              + Integer.toString(time.get(Calendar.MINUTE))
              + ":"
              + Integer.toString(time.get(Calendar.SECOND));
      series.getData().add(0, new XYChart.Data<>(category, ride.getFare()));
      counter++;
    }
    return series;
  }

  /**
   * Display fare for each time period, divide time period by report period.
   *
   * @param reportPeriod report period, "DAY", "WEEK", "MONTH"
   */
  private void displayFareLineChartByTimePeriod(String reportPeriod) {
    XYChart.Series<String, Double> series = new XYChart.Series<>();
    String cardId = cardIdComboBox.getValue().split("$")[0].split("\\s+")[2];
    RideRecord rideRecord =
        getTransitSystem().getCardManager().getCardPool().get(cardId).getRideRecords();
    RideRecord.RideIterator rideIterator = rideRecord.getRideIteratorByReportPeriod(reportPeriod);
    int i = 0;
    if (rideIterator != null) {
      if (rideIterator.hasNext()) {
        while (rideIterator.hasNext() && i <= 10) {
          double fare = 0;
          List<Ride> rides = rideIterator.next();
          for (Ride ride : rides) {
            fare += ride.getFare();
          }
          String category;
          if (rides.get(0).getTapOutTime() != null) {
            category = rides.get(0).timeToString(rides.get(0).getTapOutTime()).split(" ")[1] + "]";
          } else {
            category = rides.get(0).timeToString(rides.get(0).getTapInTime()).split(" ")[1] + "]";
          }
          series.getData().add(new XYChart.Data<>(category, fare));
          i++;
        }
        fareLineChart.getData().add(series);
      }
    }
  }

  /**
   * Get the title for fare line chart by report period.
   *
   * @param reportPeriod report period
   * @return title for fare line chart by report period
   */
  private String getLineChartTitle(String reportPeriod) {
    switch (reportPeriod) {
      case "RECENT 10":
        return "Fare of recent 10 rides";
      case "DAY":
        return "Daily Fare Report";
      case "WEEK":
        return "Weekly Fare Report";
      case "MONTH":
        return "Monthly Fare Report";
      default:
        break;
    }
    return null;
  }

  /** Get data and display this cost composition pie chart. */
  private void displayCostCompositionPieChart() {
    String reportPeriod = reportPeriodComboBox.getValue();
    costCompositionPieChart.getData().clear();
    double bus = 0;
    double subway = 0;
    if (cardIdComboBox.getItems().size() != 0) {
      String cardId = cardIdComboBox.getValue().split("$")[0].split("\\s+")[2];
      Card card = getLoggedInCardholder().getMyCards().get(cardId);
      if (reportPeriod.equals("RECENT 10")) {
        Pair<Double, Double> costComposition = getCostCompositionDataForRecentTenRides(cardId);
        bus = costComposition.getKey();
        subway = costComposition.getValue();
      } else {
        for (Ride ride : card.getRideRecords().getRideIteratorByReportPeriod(reportPeriod).next()) {
          if (ride.getTransitType().equals(Ride.TransitType.BUS)) {
            bus += ride.getFare();
          }
          if (ride.getTransitType().equals(Ride.TransitType.SUBWAY)) {
            subway += ride.getFare();
          }
        }
      }
    }
    String busName = Double.toString(bus);
    String subwayName = Double.toString(subway);
    ObservableList<PieChart.Data> pieChartData =
        FXCollections.observableArrayList(
            new PieChart.Data("bus" + busName, bus),
            new PieChart.Data("subway" + subwayName, subway));
    costCompositionPieChart.setData(pieChartData);
    costCompositionPieChart.setStartAngle(90);
  }

  /**
   * Get the cost composition for recent 10 rides.
   *
   * @param cardId cardId of selected card.
   * @return the cost composition for recent 10 rides.
   */
  private Pair<Double, Double> getCostCompositionDataForRecentTenRides(String cardId) {
    double bus = 0;
    double subway = 0;
    List<Ride> rides =
        getTransitSystem()
            .getCardManager()
            .getCardPool()
            .get(cardId)
            .getRideRecords()
            .getAllRides();
    int counter = 0;
    for (int i = rides.size() - 1; i >= 0 && counter <= 10; i--) {
      Ride ride = rides.get(i);
      if (ride.getTransitType().equals(Ride.TransitType.BUS)) {
        bus += ride.getFare();
      }
      if (ride.getTransitType().equals(Ride.TransitType.SUBWAY)) {
        subway += ride.getFare();
      }
      counter++;
    }
    return new Pair<>(bus, subway);
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
