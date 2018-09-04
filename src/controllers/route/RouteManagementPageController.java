package controllers.route;

import component.PaneStack;
import controllers.admin.AdminNavigationController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;
import map.SystemMap;
import system.AdminUser;

/**
 * A route management page controller handle events happened on route management page, including
 * adding new route and editing existing routes.
 */
public class RouteManagementPageController extends AdminNavigationController implements Observer {

  @FXML private ListView<Object> routesListView;
  private SystemMap systemMap;

  /** Initialize the route management page when admin user load it. */
  @FXML
  public void initialize() {
    Platform.runLater(
        () -> {
          super.initialize();
          systemMap = getTransitSystem().getSystemMap();
          displayRoutes();
        });
  }

  /** Display routes in existing routes area. */
  private void displayRoutes() {
    ObservableList<Object> observableList = FXCollections.observableArrayList();
    for (String routeName : systemMap.getBusRoutes().keySet()) {
      observableList.add(
          getRouteDisplayedInHBox(routeName, systemMap.getBusRoutes().get(routeName).getValue()));
    }
    for (String routeName : systemMap.getSubwayRoutes().keySet()) {
      observableList.add(
          getRouteDisplayedInHBox(
              routeName, systemMap.getSubwayRoutes().get(routeName).getValue()));
    }
    routesListView.setItems(observableList);
  }

  /**
   * Display a route in a HBox.
   *
   * @param routeName name of route
   * @param stationsList a list of station names
   * @return a HBox with route info
   */
  private HBox getRouteDisplayedInHBox(String routeName, ArrayList<String> stationsList) {

    Label routeNameLabel = new Label();
    routeNameLabel.setPrefHeight(30);
    routeNameLabel.setStyle("-fx-text-fill: black;");
    routeNameLabel.setText(routeName);

    Button deleteButton = new Button("Delete");
    deleteButton.setStyle("-fx-background-color: #B10F2E; -fx-text-fill: white;");
    deleteButton.setOnAction(event -> this.deleteRoute(routeNameLabel.getText()));

    HBox route = new HBox(10);
    HBox stations = getStationsDisplayedInHBox(stationsList);
    route.getChildren().addAll(routeNameLabel, stations, deleteButton);
    return route;
  }

  /**
   * Display stations on a route in a HBox
   * @param stationsList a list of station names
   * @return a HBox with all stations
   */
  private HBox getStationsDisplayedInHBox(ArrayList<String> stationsList) {
    HBox stations = new HBox(10);
    for (String neighbor : stationsList) {
      String stationName = neighbor.split("->")[0];
      Label stationLabel = new Label();
      stationLabel.setPrefHeight(30);
      stationLabel.setStyle("-fx-text-fill: black;");
      stationLabel.setText(stationName);
      stations.getChildren().add(stationLabel);
    }
    String stationName = stationsList.get(stationsList.size() - 1).split("->")[2];
    Label stationLabel = new Label();
    stationLabel.setPrefHeight(30);
    stationLabel.setStyle("-fx-text-fill: black;");
    stationLabel.setText(stationName);
    stations.getChildren().add(stationLabel);
    return stations;
  }

  /**
   * Delete an existing route from the transit system model.
   *
   * @param routeName name of route to delete.
   */
  private void deleteRoute(String routeName) {
    getTransitSystem().getSystemMap().removeRoute(routeName);
  }

  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof SystemMap) {
      displayRoutes();
    }
    if (observable instanceof AdminUser && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("Profile") && message.contains("Update")) {
        updateProfilePic();
      }
    }
  }

  /**
   * Display the add route page when user clicks on add route button.
   *
   * @param mouseEvent a mouse click event on add new route button.
   */
  @FXML
  public void onAddNewRouteButtonClicked(MouseEvent mouseEvent) throws IOException {
    /* get the stage where this mouse click event happened */
    Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* add previous pane displayed to history*/
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* set up scene for route management page on this stage */
    FXMLLoader editRoutePageLoader =
        new FXMLLoader(getClass().getResource("/views/route/addRoutePage.fxml"));
    ((BorderPane) scene.getRoot()).setCenter(editRoutePageLoader.load());
    primaryStage.setScene(scene);
    /* set up transit system model and current user for the controller of this scene */
    AddRoutePageController addRoutePageController = editRoutePageLoader.getController();
    addRoutePageController.setTransitSystem(getTransitSystem());
    addRoutePageController.setUserId(getUserId());
    /* add this route management page controller to corresponding Observable class */
    getTransitSystem().getSystemMap().addObserver(addRoutePageController);
    /* make this stage borderless movable */
    setWindowMovable(scene, primaryStage);
  }
}
