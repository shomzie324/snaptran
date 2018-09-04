package controllers.admin;

import card.Card;
import component.MessageBox;
import component.PaneStack;
import controllers.NavigationController;
import controllers.policy.PolicyManagementPageController;
import controllers.route.RouteManagementPageController;
import exception.NoSuchAdminUserException;
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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import map.Vertex;
import ride.Ride;
import system.AdminUser;

/** An admin page controller handles events that happened on admin user dash board page. */
public class AdminDashBoardController extends NavigationController implements Observer {

  @FXML private Label totalRevenueLabel;
  @FXML private Label totalStationsLabel;
  @FXML private Label totalDistanceLabel;
  @FXML private RadioButton dayOption;
  @FXML private RadioButton weekOption;
  @FXML private RadioButton monthOption;
  @FXML private Label statsTitleLabel;
  @FXML private ToggleGroup timePeriodOptions;
  private AdminUser loggedInAdminUser;
  @FXML private Label loggedInEmployeeId;
  @FXML private ImageView profilePic;
  @FXML private PieChart revenueCompositionPieChart;
  @FXML private BarChart<String, Integer> passengerFlowBarChart;

  /** Initialize the admin user dash board when admin user load it. */
  @FXML
  public void initialize() {
    dayOption.setUserData("DAY");
    weekOption.setUserData("WEEK");
    monthOption.setUserData("MONTH");
    Platform.runLater(
        () -> {
          loggedInAdminUser = getLoggedInAdminUser();
          updateLoggedInAdminUserEmployeeId();
          updateProfilePic();
          updateStatistics();
        });
  }

  /**
   * Display route management page when the admin user click on route management button.
   *
   * @param mouseEvent a mouse click on route management button.
   */
  @FXML
  public void onRouteManagementButtonClicked(MouseEvent mouseEvent) throws Exception {
    /* get the stage where this mouse click event happened */
    Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* add previous pane displayed to history*/
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* set up scene for route management page on this stage */
    FXMLLoader routeManagementPageControllerLoader =
        new FXMLLoader(getClass().getResource("/views/route/routeManagementPage.fxml"));
    ((BorderPane) scene.getRoot()).setCenter(routeManagementPageControllerLoader.load());
    primaryStage.setScene(scene);
    /* set up transit system model and current user for the controller of this scene */
    RouteManagementPageController routeManagementPageController =
        routeManagementPageControllerLoader.getController();
    routeManagementPageController.setTransitSystem(getTransitSystem());
    routeManagementPageController.setUserId(getUserId());
    /* add this route management page controller to corresponding Observable class */
    getTransitSystem().getSystemMap().addObserver(routeManagementPageController);
    addObserverOfLoggedInAdminUser(routeManagementPageController);
    /* make this stage borderless movable */
    setWindowMovable(scene, primaryStage);
  }

  /** Add this controller as observer of the logged in observer. */
  private void addObserverOfLoggedInAdminUser(Observer observer) {
    try {
      getTransitSystem().getAdminUser(getUserId()).addObserver(observer);
    } catch (NoSuchAdminUserException e) {
      MessageBox.display("Error", "Admin User " + getUserId() + " has not been registered!");
    }
  }

  /**
   * Display policy management page when the admin user clicks on policy management button.
   *
   * @param mouseEvent a mouse click on policy management button.
   */
  @FXML
  public void onPolicyManagementButtonClicked(MouseEvent mouseEvent) throws Exception {
    /* get the stage where this mouse click event happened */
    Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* add previous pane displayed to history*/
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* set up scene for policy management page on this stage */
    FXMLLoader policyManagementPageControllerLoader =
        new FXMLLoader(getClass().getResource("/views/policy/policyManagementPage.fxml"));
    ((BorderPane) scene.getRoot()).setCenter(policyManagementPageControllerLoader.load());
    primaryStage.setScene(scene);
    /* set up transit system model and current user for the controller of this scene */
    PolicyManagementPageController policyManagementPageController =
        policyManagementPageControllerLoader.getController();
    policyManagementPageController.setTransitSystem(getTransitSystem());
    policyManagementPageController.setUserId(getUserId());
    /* add this policy management page controller to corresponding Observable class*/
    addObserverOfLoggedInAdminUser(policyManagementPageController);
    /* make this stage borderless movable */
    setWindowMovable(scene, primaryStage);
  }

  /**
   * Display admin account details page when the admin user clicks on account information button.
   *
   * @param mouseEvent a mouse click on account information button.
   */
  @FXML
  public void onAdminDetailsButtonClicked(MouseEvent mouseEvent) throws Exception {
    /* get the stage where this mouse click event happened */
    Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* add previous pane displayed to history*/
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* set up scene for policy management page on this stage */
    FXMLLoader adminInfoPageControllerLoader =
        new FXMLLoader(getClass().getResource("/views/admin/adminInfoPage.fxml"));
    ((BorderPane) scene.getRoot()).setCenter(adminInfoPageControllerLoader.load());
    primaryStage.setScene(scene);
    /* set up transit system model and current user for the controller of this scene */
    AdminInfoPageController adminInfoPageController = adminInfoPageControllerLoader.getController();
    adminInfoPageController.setTransitSystem(getTransitSystem());
    adminInfoPageController.setUserId(getUserId());
    /* add this policy management page controller to corresponding Observable class*/
    addObserverOfLoggedInAdminUser(adminInfoPageController);
    /* make this stage borderless movable */
    setWindowMovable(scene, primaryStage);
  }

  /**
   * Display statistics for the time period selected when a time period radio button is selected.
   */
  public void updateStatistics() {
    if (timePeriodOptions.getSelectedToggle() == null) {
      MessageBox.display("error", "Please select a time period");
    } else {
      String selectedTimePeriod = timePeriodOptions.getSelectedToggle().getUserData().toString();
      /* set title of stats */
      statsTitleLabel.setText("Statistics for the " + selectedTimePeriod.toLowerCase());
      totalRevenueLabel.setText(
          "$" + loggedInAdminUser.getStatistics("TOTAL FARE " + "COLLECTED", selectedTimePeriod));
      totalStationsLabel.setText(
          "" + loggedInAdminUser.getStatistics("TOTAL STATIONS " + "COVERED", selectedTimePeriod));
      totalDistanceLabel.setText(
          loggedInAdminUser.getStatistics("TOTAL " + "DISTANCE " + "TRAVELLED", selectedTimePeriod)
              + " KM");
      String reportPeriod = (String) timePeriodOptions.getSelectedToggle().getUserData();
      displayPieChart(reportPeriod);
      displayPassengerFlowBarChart(reportPeriod);
    }
  }

  /**
   * Get data for revenue composition pie chart and display the pie chart.
   *
   * @param reportPeriod selected report period
   */
  private void displayPieChart(String reportPeriod) {
    revenueCompositionPieChart.getData().clear();
    double bus = 0;
    double subway = 0;
    for (Card card : getTransitSystem().getCardManager().getCardPool().values()) {
      for (Ride ride : card.getRideRecords().getRideIteratorByReportPeriod(reportPeriod).next()) {
        if (ride.getTransitType().equals(Ride.TransitType.BUS)) {
          bus += ride.getFare();
        }
        if (ride.getTransitType().equals(Ride.TransitType.SUBWAY)) {
          subway += ride.getFare();
        }
      }
    }
    String busName = Double.toString(bus);
    String subwayName = Double.toString(subway);
    ObservableList<PieChart.Data> pieChartData =
        FXCollections.observableArrayList(
            new PieChart.Data("bus" + busName, bus),
            new PieChart.Data("subway" + subwayName, subway));
    revenueCompositionPieChart.setData(pieChartData);
    revenueCompositionPieChart.setStartAngle(90);
  }

  /**
   * Get data for passenger flow bar chart and display the bar chart.
   *
   * @param reportPeriod selected report period
   */
  private void displayPassengerFlowBarChart(String reportPeriod) {
    passengerFlowBarChart.getData().clear();
    XYChart.Series<String, Integer> series1 = new XYChart.Series<>();
    XYChart.Series<String, Integer> series2 = new XYChart.Series<>();
    XYChart.Series<String, Integer> series3 = new XYChart.Series<>();
    for (Vertex vertex : getTransitSystem().getSystemMap().getGraph()) {
      series1
          .getData()
          .add(
              new XYChart.Data<>(
                  vertex.getValue(), vertex.getPassengerFlow(reportPeriod, "TAP IN TIMES")));
      series2
          .getData()
          .add(
              new XYChart.Data<>(
                  vertex.getValue(), vertex.getPassengerFlow(reportPeriod, "TAP OUT TIMES")));
      series3
          .getData()
          .add(
              new XYChart.Data<>(
                  vertex.getValue(), vertex.getPassengerFlow(reportPeriod, "ARRIVED TIMES")));
    }
    passengerFlowBarChart.getData().add(series1);
    passengerFlowBarChart.getData().add(series2);
    passengerFlowBarChart.getData().add(series3);
  }

  /**
   * Update user profile picture when user profile picture is changed in the model.
   *
   * @param observable Observable class that sent this notification
   * @param arg argument
   */
  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof AdminUser && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("Profile") && message.contains("Updated")) {
        updateProfilePic();
      }
    }
  }

  /** Update the UI display with the admin user's ID. */
  private void updateLoggedInAdminUserEmployeeId() {
    loggedInEmployeeId.setText("Employee ID: " + getUserId());
  }

  /** update the UI with the admin user's profile picture. */
  private void updateProfilePic() {
    profilePic.setImage(new Image(loggedInAdminUser.getProfilePic().toURI().toString()));
  }

  /**
   * get the current logged in admin user from the transit system.
   *
   * @return the AdminUser that was used to login.
   */
  private AdminUser getLoggedInAdminUser() {
    AdminUser loggedInAdminUser = null;
    try {
      loggedInAdminUser = getTransitSystem().getAdminUser(getUserId());
    } catch (NoSuchAdminUserException e) {
      MessageBox.display("Error", "Employee ID " + getUserId() + " has not been registered!");
    }
    return loggedInAdminUser;
  }
}
