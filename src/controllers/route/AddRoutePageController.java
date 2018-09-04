package controllers.route;

import component.MessageBox;
import controllers.admin.AdminNavigationController;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import map.SystemMap;
import map.Vertex;
import ride.Ride;

public class AddRoutePageController extends AdminNavigationController implements Observer {

  @FXML private ComboBox<Ride.TransitType> transitTypeComboBox;
  @FXML private ComboBox<String> directionComboBox;
  @FXML private TextField stationName;
  @FXML private TextField routeName;
  @FXML private GridPane gridPane;
  private Stack<Pair<String, Pair<Integer, Integer>>> coordinates;

  /** Initialize the add route page when admin user load it. */
  @FXML
  public void initialize() {
    List<Ride.TransitType> routeTypes = new ArrayList<>();
    routeTypes.add(Ride.TransitType.BUS);
    routeTypes.add(Ride.TransitType.SUBWAY);
    List<String> samePathTypes = new ArrayList<>();
    samePathTypes.add("BOTH WAY");
    samePathTypes.add("ONE WAY");
    transitTypeComboBox.setItems(FXCollections.observableList(routeTypes));
    directionComboBox.setItems(FXCollections.observableArrayList(samePathTypes));
    initializeGridPane();
    coordinates = new Stack<>();
    Platform.runLater(
        () -> {
          super.initialize();
          displayExistingRoute();
        });
  }

  /** Initialize the map. */
  private void initializeGridPane() {
    int i = 0;
    while (i <= 17) {
      int j = 0;
      while (j <= 15) {
        Rectangle rectangle = new Rectangle();
        rectangle.setFill(Color.DARKSLATEGREY);
        rectangle.setHeight(23);
        rectangle.setWidth(23);
        gridPane.add(rectangle, i, j);
        GridPane.setColumnIndex(rectangle, i);
        GridPane.setRowIndex(rectangle, j);
        j++;
      }
      i++;
    }
  }

  /** Add a station on the map(grid pane) when admin user clicks on add station button. */
  @FXML
  public void onAddStationButtonClicked() {
    String stationName = this.stationName.getText();
    if (stationName.isEmpty()) {
      MessageBox.display("Notice", "Station name cannot be empty!");
    } else {
      Circle station = addNewStation(stationName);
      setGridListeners(station, stationName);
    }
  }

  /**
   * Add a new station on the map when admin user clicks on add station button.
   *
   * @param stationName the name of the station.
   * @return A Circle representing a new station.
   */
  private Circle addNewStation(String stationName) {
    Circle station = new Circle();
    Tooltip stationNameTip = new Tooltip(stationName);
    Tooltip.install(station, stationNameTip);
    station.setFill(Color.YELLOW);
    station.setRadius(10);
    GridPane.setColumnIndex(station, 0);
    GridPane.setRowIndex(station, 14);
    coordinates.push(new Pair<>(stationName, new Pair<>(0, 14)));
    gridPane.getChildren().add(station);
    return station;
  }

  /**
   * Set the station position.
   *
   * @param stationName the name of the station.
   * @param station A Circle representing a station.
   */
  private void setGridListeners(Circle station, String stationName) {
    for (Node node : gridPane.getChildren()) {
      node.setOnMouseClicked(
          event -> {
            if (!coordinates.isEmpty()) {
              Rectangle rectangle = new Rectangle();
              rectangle.setFill(Color.DARKSLATEGREY);
              rectangle.setHeight(23);
              rectangle.setWidth(23);
              GridPane.setColumnIndex(rectangle, GridPane.getColumnIndex(station));
              GridPane.setRowIndex(rectangle, GridPane.getRowIndex(station));
              gridPane.getChildren().add(rectangle);
              gridPane.getChildren().remove(station);
              GridPane.setColumnIndex(station, GridPane.getColumnIndex(node));
              GridPane.setRowIndex(station, GridPane.getRowIndex(node));
              coordinates.pop();
              coordinates.push(
                  new Pair<>(
                      stationName,
                      new Pair<>(GridPane.getColumnIndex(node), GridPane.getRowIndex(node))));
              gridPane.getChildren().add(station);
              gridPane.getChildren().remove(node);
            }
          });
    }
  }

  /** Add a new route on the map when admin user clicks on add route button. */
  @FXML
  public void onAddRouteButtonClicked() {
    String routeName = this.routeName.getText();
    if (coordinates.size() < 2) {
      MessageBox.display("Notice", "A route must have at least 2 stations/stops!");
    } else if (routeName.isEmpty()) {
      MessageBox.display("Notice", "Route name cannot be empty!");
    } else if (transitTypeComboBox.getValue() == null) {
      MessageBox.display("Notice", "Please select a transit type of this route!");
    } else if (directionComboBox.getValue() == null) {
      MessageBox.display(
          "Notice", "Please select to generate this route for both way or only one way!");
    } else {
      updateModel(
          transitTypeComboBox.getValue(),
          directionComboBox.getValue(),
          routeName,
          getCoordinates(),
          getRoute());
    }
  }

  /**
   * Update the grid pane when adding a new route.
   *
   * @param transitType the transit type of this route.
   * @param coordinates A List including all coordinates of stations.
   * @param direction A String representing whether this route is both way or one way.
   * @param route the name for this route.
   * @param routeName the name for this route.
   */
  private void updateModel(
      Ride.TransitType transitType,
      String direction,
      String routeName,
      List<Pair<Integer, Integer>> coordinates,
      String route) {
    getTransitSystem()
        .getSystemMap()
        .addNewRoute(transitType, direction, routeName, coordinates, route);
  }

  /**
   * Get the coordinates of stations of a route.
   *
   * @return A list representing coordinates of stations.
   */
  private List<Pair<Integer, Integer>> getCoordinates() {
    List<Pair<Integer, Integer>> result = new ArrayList<>();
    for (Pair<String, Pair<Integer, Integer>> coordinate : coordinates) {
      result.add(coordinate.getValue());
    }
    return result;
  }

  /**
   * Get the route on the map.
   *
   * @return A String representing the route.
   */
  private String getRoute() {
    StringBuilder route = new StringBuilder();
    Pair<String, Pair<Integer, Integer>> last = coordinates.pop();
    route.insert(0, last.getKey());
    Pair<Integer, Integer> prevCoordinate = last.getValue();
    while (!coordinates.isEmpty()) {
      Pair<String, Pair<Integer, Integer>> coordinate = coordinates.pop();
      route.insert(0, "->");
      route.insert(0, getDistanceBetweenTwoStation(coordinate.getValue(), prevCoordinate));
      route.insert(0, "->");
      route.insert(0, coordinate.getKey());

      if (coordinates.size() != 0) {
        route.insert(0, " | ");
        route.insert(0, coordinate.getKey());
      }
      prevCoordinate = coordinate.getValue();
    }
    return route.toString();
  }

  /**
   * Get the distance between two stations.
   *
   * @return A Double representing the distance.
   */
  private double getDistanceBetweenTwoStation(
      Pair<Integer, Integer> coordinate1, Pair<Integer, Integer> coordinate2) {
    double distance =
        Math.sqrt(
            (coordinate1.getKey() - coordinate2.getKey())
                    * (coordinate1.getKey() - coordinate2.getKey())
                + (coordinate1.getValue() - coordinate2.getValue())
                    * (coordinate1.getValue() - coordinate2.getValue()));
    BigDecimal bigDecimal = new BigDecimal(Double.valueOf(distance).toString());
    bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    return bigDecimal.doubleValue();
  }

  /**
   * Update the whole page when adding a new route or station.
   *
   * @param observable an observable class.
   * @param arg a message.
   */
  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof SystemMap && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("New") && message.contains("Route:")) {
        MessageBox.display("Message", (String) arg);
        displayExistingRoute();
        transitTypeComboBox.setValue(null);
        directionComboBox.setValue(null);
        stationName.setText(null);
        routeName.setText(null);
      }
    }
  }

  /** Display the existing route when admin user load the page. */
  private void displayExistingRoute() {
    for (Vertex station : getTransitSystem().getSystemMap().getGraph()) {
      Circle existingStation = new Circle();
      existingStation.setFill(Color.GREEN);
      existingStation.setRadius(10);
      GridPane.setConstraints(
          existingStation, station.getCoordinate().getKey(), station.getCoordinate().getValue());
      gridPane.getChildren().add(existingStation);
      Tooltip stationNameTip = new Tooltip(station.getValue());
      Tooltip.install(existingStation, stationNameTip);
    }
  }
}
