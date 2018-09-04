package component;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** A message box can display message in a new stage. */
public class MessageBox {

  /* record previous Message to avoid duplicate message */
  private static String previousMessage = "";

  /**
   * Display a message box.
   *
   * @param message A String representing the message which can seen by user.
   * @param title the title of the message box.
   */
  public static void display(String title, String message) {
    if (!previousMessage.equals(message)) { // message not duplicated
      /* add label to display this message and set up style of this label */
      Label label = new Label();
      label.setText(message);
      label.setStyle("-fx-text-fill: white");
      label.setPrefSize(400, 100);
      label.setAlignment(Pos.CENTER);
      /* set up style of this message box*/
      Stage window = new Stage();
      VBox layout = new VBox();
      setStyle(window, layout, title);
      /* add button to close this message box */
      Button closeButton = new Button("Close the window");
      closeButton.setOnAction(e -> window.close());
      /* add all content to this message box */
      layout.getChildren().addAll(label, closeButton);
      layout.setAlignment(Pos.CENTER);
      /* display this message box */
      Scene scene = new Scene(layout);
      window.setScene(scene);
      /* block any user interaction until this MessageBox is closed */
      window.showAndWait();
      /* record this message to avoid duplicate message in the future*/
      previousMessage = message;
    }
  }

  /**
   * Set up style of this message box.
   *
   * @param window window to display this message box
   * @param layout lay out of this message box
   * @param title title of this message box
   */
  private static void setStyle(Stage window, VBox layout, String title) {
    window.initModality(Modality.APPLICATION_MODAL);
    window.initStyle(StageStyle.UNDECORATED);
    window.setWidth(400);
    window.setHeight(150);
    if (title.equals("Message")) {
      layout.setStyle("-fx-background-color: green");
    } else {
      layout.setStyle("-fx-background-color: #e52325");
    }
  }
}
