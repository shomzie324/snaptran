package controllers.admin;

import component.MessageBox;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import system.AdminUser;

public class AdminInfoPageController extends AdminNavigationController implements Observer {

  @FXML private TextField currentPassword;
  @FXML private TextField newPassword;
  @FXML private ImageView profilePicDetail;

  /**
   * Change the admin user profile picture when admin user clicks the change profile button.
   *
   * @param event a mouse click event on change profile button.
   */
  @FXML
  public void onChangeProfilePictureButtonClicked(ActionEvent event) {

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select a profile picture");
    fileChooser
        .getExtensionFilters()
        .addAll(
            new FileChooser.ExtensionFilter("jpg", "*.jpg"),
            new FileChooser.ExtensionFilter("png", "*.png"));

    fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    File file = fileChooser.showOpenDialog(stage);

    if (file != null) {
      getLoggedInAdminUser().setProfilePic(file);
      updateProfilePic();
    }
  }

  /** Change the password of admin user. */
  public void changePassword() {
    String passwordQuery = currentPassword.getText();
    if (getLoggedInAdminUser().verifyPassword(passwordQuery)) {
      getLoggedInAdminUser().setPassword(newPassword.getText());
    } else {
      MessageBox.display("error", "Wrong current password!");
    }
  }

  @Override
  protected void updateProfilePic() {
    super.updateProfilePic();
    profilePicDetail.setImage(new Image(getLoggedInAdminUser().getProfilePic().toURI().toString()));
  }

  @Override
  public void update(Observable observable, Object arg) {
    if (observable instanceof AdminUser && arg instanceof String) {
      List<String> message = new ArrayList<>(Arrays.asList(((String) arg).split("\\s+")));
      if (message.contains("Password")) {
        MessageBox.display("Message", (String) arg);
      }
    }
  }
}
