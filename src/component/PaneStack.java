package component;

import java.util.Stack;
import javafx.scene.layout.Pane;

/**
 * A pane stack stores displayed panes in first in last out order, for the go back button to trace
 * previous pane displayed.
 */
public class PaneStack {

  private static final Stack<Pane> panes = new Stack<>();

  /**
   * Push a new pane to the front of the stack.
   *
   * @param pane a new pane to store.
   */
  public static void push(Pane pane) {
    panes.push(pane);
  }

  /**
   * Pop the previous pane displayed from the front of the stack if there is one, return null
   * otherwise.
   *
   * @return the previous pane displayed from the front of the stack if there is one.
   */
  public static Pane pop() {
    if (panes.isEmpty()) {
      return null;
    }
    return panes.pop();
  }

  /** Clear all panes stored in the pane stack. */
  public static void clear() {
    panes.clear();
  }
}
