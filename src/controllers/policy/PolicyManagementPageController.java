package controllers.policy;

import component.MessageBox;
import component.PaneStack;
import controllers.admin.AdminNavigationController;
import fare.CapStrategy;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ride.Ride;
import system.AdminUser;

public class PolicyManagementPageController extends AdminNavigationController implements Observer {

  @FXML private ListView<HBox> strategyList;
  @FXML private TextField strategyName;
  @FXML private TextField strategyType;
  @FXML private TextField reachCapTime;
  @FXML private TextField initialBalance;

  /** Initialize the policy management page data. */
  @FXML
  public void initialize() {
    Platform.runLater(
        () -> {
          super.initialize();
          for (String name : getTransitSystem().getFareManager().getFarePolicy().keySet()) {
            addLine(name);
          }
        });
  }

  /**
   * Add a line when user clicks the add strategy button. The line contains the information of
   * strategy name, and admin user can enter the edit page or remove this strategy by clicking the
   * button beside strategy name label.
   *
   * @param name the name of the strategy.
   */
  private void addLine(String name) {
    Label strategyLabel = new Label(name);
    strategyLabel.setStyle("-fx-text-fill: white;");
    // Set style for edit button
    Button editBtn = new Button("Edit");
    editBtn.setStyle("-fx-background-color: #D84727;-fx-text-fill: white;");
    editBtn.setOnMouseClicked(
        event -> {
          try {
            onEditButtonClicked(event, strategyLabel.getText());
          } catch (IOException e) {
            e.getStackTrace();
          }
        });

    Button deleteBtn = new Button("Remove");
    deleteBtn.setStyle("-fx-background-color: #D84727;-fx-text-fill: white;");
    deleteBtn.setOnMouseClicked(event -> onRemoveButtonClicked(strategyLabel.getText()));

    HBox strategy = new HBox(10);
    // Add all sub items to the HBox of each cell
    strategy.getChildren().addAll(strategyLabel, editBtn, deleteBtn);
    strategyList.getItems().add(strategy);
  }

  /** Set the new reach cap time when clicks on the save button. */
  public void onSetCapTimeButtonClicked() {
    if (reachCapTime.getText().isEmpty()) {
      MessageBox.display("Error", "Reach Cap Time Cannot Be Empty!");
    } else if (!reachCapTime.getText().isEmpty()
        && !reachCapTime.getText().matches("[0-9]*\\.?[0-9]*")) {
      MessageBox.display("Error", "Time Should Be Number!");
    } else {
      getLoggedInAdminUser().setReachCapTime(Double.valueOf(reachCapTime.getText()));
    }
  }

  /** Set the new initial balance when clicks on the save button. */
  public void onSetInitialBalanceButtonClicked() {
    if (initialBalance.getText().isEmpty()) {
      MessageBox.display("Error", "Initial Balance Cannot Be Empty!");
    } else if (!initialBalance.getText().isEmpty()
        && !initialBalance.getText().matches("[0-9]*\\.?[0-9]*")) {
      MessageBox.display("Error", "Initial Balance Should Be Number!");
    } else {
      getLoggedInAdminUser().setInitialBalance(Double.valueOf(initialBalance.getText()));
    }
  }

  /**
   * Add a new policy when admin user clicks on the add policy button. Admin user will see the
   * message box corresponding to the changes.
   */
  public void addNewPolicy() {
    if (strategyName.getText().isEmpty()) {
      MessageBox.display("Error", "Strategy Name Cannot Be Empty!");
    } else if (strategyType.getText().isEmpty()) {
      MessageBox.display("Error", "Strategy Type Cannot Be Empty!");
    } else if (!strategyType.getText().equals("BUS") && !strategyType.getText().equals("SUBWAY")) {
      MessageBox.display("Error", "Invalid Strategy Type!");
    } else {
      getLoggedInAdminUser()
          .addFareStrategy(
              strategyName.getText(),
              CapStrategy.getNewFareStrategyInstance(
                  getTransitSystem().getSystemMap(),
                  Ride.TransitType.valueOf(strategyType.getText())));
    }
  }

  /**
   * Display the policy edit page when admin user clicks on edit button.
   *
   * @param mouseEvent a mouse click event on edit button.
   */
  private void onEditButtonClicked(MouseEvent mouseEvent, String strategyName) throws IOException {
    Stage primaryStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
    Scene scene = ((Node) mouseEvent.getSource()).getScene();
    /* add root pane on current scene to history pane stack */
    PaneStack.push((Pane) ((BorderPane) scene.getRoot()).getCenter());
    /* set up scene for user info page on this stage */
    FXMLLoader policyEditViewLoader =
        new FXMLLoader(getClass().getResource("/views/policy/policyEditPane.fxml"));
    ((BorderPane) scene.getRoot()).setCenter(policyEditViewLoader.load());
    primaryStage.setScene(scene);
    /* set up transit system model and current user for the controller of this scene */
    PolicyEditPageController policyEditPageController = policyEditViewLoader.getController();
    policyEditPageController.setTransitSystem(getTransitSystem());
    policyEditPageController.setUserId(getUserId());
    policyEditPageController.setStrategyName(strategyName);
    /* add this policy edit page controller to corresponding Observable class */
    addObserverOfLoggedInAdminUser(policyEditPageController);
    /* set up stage */
    setWindowMovable(scene, primaryStage);
  }

  /**
   * Remove a strategy from the existing policy list. Admin User will see a message box if remove
   * the policy successfully.
   *
   * @param strategyName the name of the strategy.
   */
  private void onRemoveButtonClicked(String strategyName) {
    getLoggedInAdminUser().removeFareStrategy(strategyName);
  }

  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof AdminUser) {
      if (arg instanceof String) {
        List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
        if (message.contains("Profile") && message.contains("Updated")) {
          updateProfilePic();
        } else if (message.contains("New")
            && message.contains("Fare")
            && message.contains("Strategy")) {
          MessageBox.display("Message", (String) arg);
        } else if (message.contains("Fare")
            && message.contains("Strategy")
            && message.contains("Removed!")) {
          MessageBox.display("Message", (String) arg);
        } else if (message.contains("is") && message.contains("set") && message.contains("to:")) {
          MessageBox.display("Message", (String) arg);
        }
      }
      strategyList.getItems().clear();
      for (String name : getTransitSystem().getFareManager().getFarePolicy().keySet()) {
        addLine(name);
      }
    }
  }
}
