package controllers.policy;

import component.MessageBox;
import controllers.admin.AdminNavigationController;
import fare.BusStrategy;
import fare.SubwayStrategy;
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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import system.AdminUser;

public class PolicyEditPageController extends AdminNavigationController implements Observer {

  private String strategyName;
  @FXML private TextField oneTimeFare;
  @FXML private TextField perStationFare;
  @FXML private TextField reachCapFare;
  @FXML private TextField maxFarePerTrip;

  /**
   * Set the strategy(policy) name of this policy edit page.
   *
   * @param name the name of this strategy.
   */
  void setStrategyName(String name) {
    strategyName = name;
  }

  /**
   * Back to the policy management page when admin user clicks on dashboard button.
   *
   * @param mouseEvent a mouse click event on dashboard button.
   */
  private void loadPolicyManagementPage(MouseEvent mouseEvent) throws Exception {
    /* get the stage where this mouse click event happened */
    Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
    /* set up scene for user info page on this stage */
    FXMLLoader policyManagementPageControllerLoader =
        new FXMLLoader(getClass().getResource("/views/policy/policyManagementPage.fxml"));
    Pane root = policyManagementPageControllerLoader.load();
    Scene scene = new Scene(root, 800, 500);
    primaryStage.setScene(scene);
    /* set up transit system model and current user for the controller of this scene */
    PolicyManagementPageController policyManagementPageController =
        policyManagementPageControllerLoader.getController();
    policyManagementPageController.setTransitSystem(getTransitSystem());
    policyManagementPageController.setUserId(getUserId());
    /* add this policy management page to corresponding Observable class*/
    addObserverOfLoggedInAdminUser(policyManagementPageController);
    /* set up stage */
    setWindowMovable(scene, primaryStage);
  }

  /**
   * Set the one time fare/per station fare, reach cap fare or max fare per trip of this bus/subway
   * strategy.
   */
  public void onSaveButtonClicked(MouseEvent mouseEvent) throws Exception {
    if (isAllEmpty()) {
      MessageBox.display("Error", "Cannot Be Empty!");
    } else if (!oneTimeFare.getText().isEmpty()
        && getTransitSystem().getFareManager().getFarePolicy().get(strategyName)
            instanceof SubwayStrategy) {
      MessageBox.display("Error", "This Strategy Does Not Have One Time Fare!");
      oneTimeFare.setText("");
    } else if (!perStationFare.getText().isEmpty()
        && getTransitSystem().getFareManager().getFarePolicy().get(strategyName)
            instanceof BusStrategy) {
      MessageBox.display("Error", "This Strategy Does Not Have Per Station Fare!");
      perStationFare.setText("");
    } else if (!isDecimalNumber()) {
      MessageBox.display("Error", "Fare Should Be Number!");
    } else {
      changeFare();
      loadPolicyManagementPage(mouseEvent);
    }
  }

  private boolean isDecimalNumber() {
    boolean is = true;
    if ((!oneTimeFare.getText().isEmpty()
        && !oneTimeFare.getText().matches("[0-9]*\\.?[0-9]*"))
        || (!perStationFare.getText().isEmpty()
        && !perStationFare.getText().matches("[0-9]*\\.?[0-9]*"))
        || (!reachCapFare.getText().isEmpty()
        && !reachCapFare.getText().matches("[0-9]*\\.?[0-9]*"))
        || (!maxFarePerTrip.getText().isEmpty()
        && !maxFarePerTrip.getText().matches("[0-9]*\\.?[0-9]*"))) {
      is = false;
    }
    return is;
  }

  private boolean isAllEmpty() {
    return (oneTimeFare.getText().isEmpty()
        && perStationFare.getText().isEmpty()
        && reachCapFare.getText().isEmpty()
        && maxFarePerTrip.getText().isEmpty());
  }

  private void changeFare() {
    if (!oneTimeFare.getText().isEmpty()) {
      getLoggedInAdminUser().setOneTimeFare(strategyName, Double.valueOf(oneTimeFare.getText()));
    }
    if (!perStationFare.getText().isEmpty()) {
      getLoggedInAdminUser()
          .setPerStationFare(strategyName, Double.valueOf(perStationFare.getText()));
    }
    if (!reachCapFare.getText().isEmpty()) {
      getLoggedInAdminUser()
          .setReachCapFare(strategyName, Double.valueOf(reachCapFare.getText()));
    }
    if (!maxFarePerTrip.getText().isEmpty()) {
      getLoggedInAdminUser()
          .setMaxFarePerTrip(strategyName, Double.valueOf(maxFarePerTrip.getText()));
    }
  }

  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof AdminUser && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("is") && message.contains("set") && message.contains("to:")) {
        MessageBox.display("Message", (String) arg);
      }
    }
  }
}
