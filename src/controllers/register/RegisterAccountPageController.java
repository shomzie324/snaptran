package controllers.register;

import cardholder.Cardholder;
import component.MessageBox;
import controllers.NavigationController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import system.AdminUser;

public class RegisterAccountPageController extends NavigationController implements Observer {

  @FXML private TextField newUserIdInput;
  @FXML private TextField newPasswordInput;
  @FXML private TextField newUserNameInput;
  @FXML private RadioButton isNewAdminOption;
  @FXML private RadioButton isNewCardHolderOption;

  /**
   * Get the user input required for creating a user and call the appropriate helper method for
   * creating different user types.
   */
  @FXML
  public void onCreateAccountButtonClicked() {
    String newUserId = newUserIdInput.getText();
    String newUserPassword = newPasswordInput.getText();
    String newUserName = newUserNameInput.getText();
    // only create account if it is selected and contains the info for that account type
    if (isNewAdminOption.isSelected()) {
      addAdminUser(newUserId, newUserPassword);
    } else if (isNewCardHolderOption.isSelected()) {
      addNormalUser(newUserName, newUserId, newUserPassword);
    } else {
      MessageBox.display("Error", "Please select an account type!");
    }
  }

  /**
   * Helper method that creates an admin user if the input is valid.
   *
   * @param employeeId the unique identification number for the employee.
   * @param password the password associated with the new admin user account.
   */
  private void addAdminUser(String employeeId, String password) {
    if (employeeId.matches("[0-9]+") && !password.isEmpty()) {
      getTransitSystem()
          .addAdminUser(AdminUser.getInstance(employeeId, password, getTransitSystem()));
    } else {
      displayNoticeToAdminUser(employeeId, password);
    }
  }

  /**
   * Display a message box to this admin user with hint if any of employee ID, password information
   * is invalid.
   *
   * @param employeeId employee ID entered by this admin user
   * @param password password entered by this admin user
   */
  private void displayNoticeToAdminUser(String employeeId, String password) {
    if (!employeeId.matches("[0-9]+")) {
      MessageBox.display("Error", "Pleas enter valid employee ID!");
    } else {
      String message = "";
      if (employeeId.isEmpty()) {
        message += "employee ID ";
      }
      if (password.isEmpty()) {
        message += "password ";
      }
      MessageBox.display("Error", "Admin User " + message + "cannot be empty!");
    }
  }

  /**
   * Helper method that creates an admin user if the input is valid.
   *
   * @param name the card holder's name.
   * @param email the unique email identification for the cardholder.
   * @param password the password associated with the new admin user account.
   */
  private void addNormalUser(String name, String email, String password) {
    if (email.matches("^[a-z]+@[a-z]+\\.[a-z]+$") && !password.isEmpty() && !name.isEmpty()) {
      /* get system time to record this cardholder's register date */
      Calendar registerDate = Calendar.getInstance();
      registerDate.setTimeInMillis(System.currentTimeMillis());
      /* create new cardholder in this transit system model */
      getTransitSystem()
          .getCardholderManager()
          .addNewCardholder(Cardholder.getInstance(name, email, password, registerDate));
    } else {
      displayNoticeToCardholder(name, email, password);
    }
  }

  /**
   * Display a message box to this cardholder with hint if any of name, email, password information
   * is invalid.
   *
   * @param name name entered by this user
   * @param email email entered by this user
   * @param password password entered by this user
   */
  private void displayNoticeToCardholder(String name, String email, String password) {
    if (!email.matches("^[a-z]+@[a-z]+\\.[a-z]+$")) {
      MessageBox.display("Notice", "Pleas enter a valid email address!");
    } else {
      String message = "";
      if (name.isEmpty()) {
        message += "name ";
      }
      if (email.isEmpty()) {
        message += "email ";
      }
      if (password.isEmpty()) {
        message += "password ";
      }
      MessageBox.display("Error", "Cardholder " + message + "cannot be empty!");
    }
  }

  /**
   * This method overrides a method in the Observer interface. Display a message box whenever an
   * observable notifies this controller of a change.
   */
  @Override
  public void update(Observable o, Object arg) {
    ArrayList<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
    if (message.contains("already") && message.contains("exists!")) {
      MessageBox.display("Error", arg.toString());
    } else if (message.contains("has")
        && message.contains("been")
        && message.contains("created!")) {
      MessageBox.display("Message", arg.toString());
    }
  }
}
