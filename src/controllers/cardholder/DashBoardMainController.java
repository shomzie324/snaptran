package controllers.cardholder;

import card.CardManager;
import cardholder.Cardholder;
import component.MessageBox;
import component.PaneStack;
import controllers.NavigationController;
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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import map.Vertex;

/** A cardholder dash board view class update view of items displayed on cardholder dash board. */
public class DashBoardMainController extends NavigationController implements Observer {

  @FXML private LineChart<String, Integer> passengerFlowLineChart;

  /** Initialize view of cardholder dash board. */
  @FXML
  public void initialize() {
    Platform.runLater(this::displayLineChart);
  }

  /**
   * Update view of the passenger flow line chart when cardholder tap out.
   *
   * @param observable Observable class that notified this change
   * @param arg argument that states this change
   */
  @Override
  public void update(Observable observable, Object arg) {
    Platform.runLater(
        () -> {
          if (observable instanceof CardManager && arg instanceof String) {
            List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
            if (message.contains("Tapped") && message.contains("At")) {
              displayLineChart();
            }
          }
        });
  }

  /** Display tap in times, tap out times and arrived times on passenger flow line chart. */
  private void displayLineChart() {
    passengerFlowLineChart.getData().clear();
    String reportPeriod = "DAY";
    /* get the data for tap in times */
    XYChart.Series<String, Integer> tapInSeries = new XYChart.Series<>();
    List<Vertex> stations = new ArrayList<>(getTransitSystem().getSystemMap().getGraph());
    for (Vertex station : stations) {
      tapInSeries
          .getData()
          .add(
              new XYChart.Data<>(
                  station.getValue(), station.getPassengerFlow(reportPeriod, "TAP IN TIMES")));
    }
    /* get the data for tap out times */
    XYChart.Series<String, Integer> tapOutSeries = new XYChart.Series<>();
    for (Vertex station : stations) {
      tapOutSeries
          .getData()
          .add(
              new XYChart.Data<>(
                  station.getValue(), station.getPassengerFlow(reportPeriod, "TAP OUT TIMES")));
    }
    /* get the data for arrived times */
    XYChart.Series<String, Integer> arrivedSeries = new XYChart.Series<>();
    for (Vertex station : stations) {
      arrivedSeries
          .getData()
          .add(
              new XYChart.Data<>(
                  station.getValue(), station.getPassengerFlow(reportPeriod, "ARRIVED TIMES")));
    }
    passengerFlowLineChart.getData().add(tapInSeries);
    passengerFlowLineChart.getData().add(tapOutSeries);
    passengerFlowLineChart.getData().add(arrivedSeries);
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
   * Display user info pane at center when the user click on user info button.
   *
   * @param mouseEvent a mouse click event on user info button.
   */
  @FXML
  public void onUserInfoButtonClicked(MouseEvent mouseEvent) throws IOException {
    /* load user info pane and get current scene displayed */
    FXMLLoader userInfoPaneLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/userInfoPaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* store previous pane displayed at centre of current scene in pane stack */
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* display user info pane on current scene */
    ((BorderPane) scene.getRoot()).setCenter(userInfoPaneLoader.load());
    /* set up model for this user info pane */
    UserInfoPaneController userInfoPaneController = userInfoPaneLoader.getController();
    userInfoPaneController.setTransitSystem(getTransitSystem());
    userInfoPaneController.setUserId(getUserId());
  }

  /**
   * Display dash board pane at center when the user click on home button.
   *
   * @param mouseEvent a mouse click event on home button.
   */
  @FXML
  public void onHomeButtonClicked(MouseEvent mouseEvent) throws IOException {
    /* load dash board pane and set it to the center of current scene */
    FXMLLoader dashBoardPaneLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/dashBoardPaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* store previous pane displayed at centre of current scene in pane stack */
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* display dash board pane on current scene */
    BorderPane center = dashBoardPaneLoader.load();
    ((BorderPane) scene.getRoot()).setCenter(center);
    /* set up model for this dash board pane */
    DashBoardPaneController dashBoardPaneController = dashBoardPaneLoader.getController();
    dashBoardPaneController.setTransitSystem(getTransitSystem());
    dashBoardPaneController.setUserId(getUserId());
    dashBoardPaneController.setLayout(center);
  }

  /**
   * Display cards pane at center when the user click on cards button.
   *
   * @param mouseEvent a mouse click event on cards button.
   */
  @FXML
  public void onCardsButtonClicked(MouseEvent mouseEvent) throws IOException {
    FXMLLoader cardsPaneLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/cardsPaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* store previous pane displayed at centre of current scene in pane stack */
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* display user info pane on current scene */
    ((BorderPane) scene.getRoot()).setCenter(cardsPaneLoader.load());
    /* set up model for this user info pane */
    CardsPaneController cardsPaneController = cardsPaneLoader.getController();
    cardsPaneController.setTransitSystem(getTransitSystem());
    cardsPaneController.setUserId(getUserId());
    getLoggedInCardholder().addObserver(cardsPaneController);
  }

  /**
   * Display go transit pane at center when the user click on go transit button.
   *
   * @param mouseEvent a mouse click event on go transit button.
   */
  @FXML
  public void onGoTransitButtonClicked(MouseEvent mouseEvent) throws IOException {
    FXMLLoader goTransitPaneLoader =
        new FXMLLoader(getClass().getResource("/views/cardholder/goTransitPaneView.fxml"));
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* store previous pane displayed at centre of current scene in pane stack */
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* display dash board pane on current scene */
    ((BorderPane) scene.getRoot()).setCenter(goTransitPaneLoader.load());
    /* set up model for this dash board pane */
    GoTransitPaneController goTransitPaneController = goTransitPaneLoader.getController();
    goTransitPaneController.setTransitSystem(getTransitSystem());
    goTransitPaneController.setUserId(getUserId());
    /* add this go transit pane controller to its corresponding Observable class */
    getTransitSystem().getCardManager().addObserver(goTransitPaneController);
    getTransitSystem().getFareManager().addObserver(goTransitPaneController);
  }
}
