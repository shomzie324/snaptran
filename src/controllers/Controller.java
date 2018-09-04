package controllers;

import java.util.Observable;
import system.TransitSystem;

public abstract class Controller extends Observable {

  private TransitSystem transitSystem;

  private String userId;

  /**
   * Get the transit system used in view and control of this stage.
   *
   * @return the transit system used in view and control of this stage.
   */
  protected TransitSystem getTransitSystem() {
    return this.transitSystem;
  }

  /**
   * Set the transit system used in view and control of this stage.
   *
   * @param transitSystem the transit system used in view and control of this stage.
   */
  public void setTransitSystem(TransitSystem transitSystem) {
    this.transitSystem = transitSystem;
  }

  /**
   * Get the ID of the user who logged in this transit system.
   *
   * @return ID of user.
   */
  protected String getUserId() {
    return this.userId;
  }

  /**
   * Set the ID of the user who logged in this transit system.
   *
   * @param userId ID of user.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }
}
