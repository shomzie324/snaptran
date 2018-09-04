package controllers;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** A window controller can close or move the stage. */
public abstract class WindowController extends Controller {

  /* define offsets for moving borderless window */
  private double horizontalOffSet;
  private double verticalOffSet;

  /** Close the stage if user clicked close button. */
  public void onCloseButtonClicked() {
    Platform.exit();
  }

  /**
   * Set this stage borderless movable.
   *
   * @param scene current scene on this stage
   * @param stage this stage.
   */
  public void setWindowMovable(Scene scene, Stage stage) {
    scene.setOnMousePressed(
        windowEvent -> {
          horizontalOffSet = windowEvent.getSceneX();
          verticalOffSet = windowEvent.getSceneY();
        });

    scene.setOnMouseDragged(
        windowEvent -> {
          stage.setX(windowEvent.getScreenX() - horizontalOffSet);
          stage.setY(windowEvent.getScreenY() - verticalOffSet);
        });
  }
}
