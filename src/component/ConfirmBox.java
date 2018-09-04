package component;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ConfirmBox {

  private static boolean answer;

  /**
   * Display a confirm box. User can choose yes or no.
   *
   * @param message A String representing the message which can seen by user.
   * @param title the title of the confirm box.
   * @return A boolean representing the user choice.
   */
  public static boolean display(String title, String message) {
    Stage window = new Stage();
    window.initModality(Modality.APPLICATION_MODAL);
    window.initStyle(StageStyle.UNDECORATED);
    window.setTitle(title);
    window.setWidth(300);
    window.setHeight(150);
    Label label = new Label();
    label.setText(message);
    // Create two buttons to give answer
    Button yesButton = new Button("Yes");
    Button noButton = new Button("No");
    yesButton.setOnAction(
        e -> {
          answer = true;
          window.close();
        });
    noButton.setOnAction(
        e -> {
          answer = false;
          window.close();
        });
    VBox layout = new VBox(10);
    layout.getChildren().addAll(label, yesButton, noButton);
    layout.setAlignment(Pos.CENTER);
    layout.setStyle("-fx-background-color: #b2ebf2");
    Scene scene = new Scene(layout);
    window.setScene(scene);
    window.showAndWait();
    return answer;
  }
}
