package system;

import card.Card;
import card.CardManager;
import cardholder.Cardholder;
import cardholder.CardholderManager;
import exception.NoSuchAdminUserException;
import fare.CapStrategy;
import fare.FareManager;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Observable;
import log.LogManager;
import map.SystemMap;
import ride.Ride;
import serialize.SerializeManager;

/**
 * A transit system has a task center to dispatch tasks to managers to handle. A transit system has
 * several managers to handle tasks for their own part. A transit system has its admin users.
 */
public class TransitSystem extends Observable implements Serializable {

  private final SystemMap systemMap;
  private final CardholderManager cardholderManager;
  private final CardManager cardManager;
  private final FareManager fareManager;
  private final HashMap<String, AdminUser> adminUsers;

  /** Construct a new TransitSystem. */
  private TransitSystem() {
    this.addObserver(LogManager.getInstance());
    setChanged();
    notifyObservers("Transit System Initialized!");
    SerializeManager.getInstance().setTransitSystem(this);
    this.systemMap = new SystemMap();
    this.cardholderManager = new CardholderManager();
    this.cardManager = new CardManager();
    this.fareManager =
        new FareManager(
            CapStrategy.getNewFareStrategyInstance(systemMap, Ride.TransitType.BUS),
            CapStrategy.getNewFareStrategyInstance(systemMap, Ride.TransitType.SUBWAY));
    this.adminUsers = new HashMap<>();
  }

  /**
   * Get an instance of TransitSystem.
   *
   * @return an instance of TransitSystem.
   */
  public static TransitSystem getInstance() {
    TransitSystem transitSystem;
    /* try to de-serialize transit system by SerializeManager */
    try {
      transitSystem = (TransitSystem) SerializeManager.getInstance().readObject();
      /* set up LogManager Observer for this transit system */
      transitSystem.setObservers(transitSystem);
      /* set up transit system for the Serialize Manager */
      /* if successfully de-serialized transit system */
      SerializeManager.getInstance().setTransitSystem(transitSystem);
    } catch (Exception e) {
      /* create a new transit system if no serialization history */
      transitSystem = new TransitSystem();
    }
    return transitSystem;
  }

  private void setObservers(TransitSystem transitSystem) {
    LogManager logManager = LogManager.getInstance();
    transitSystem.addObserver(logManager);
    transitSystem.cardManager.addObserver(logManager);
    transitSystem.cardholderManager.addObserver(logManager);
    transitSystem.fareManager.addObserver(logManager);
    for (AdminUser adminUser : transitSystem.adminUsers.values()) {
      adminUser.addObserver(logManager);
    }
    for (Card card : transitSystem.cardManager.getCardPool().values()) {
      card.addObserver(logManager);
    }
    for (Cardholder cardholder : transitSystem.cardholderManager.getCardholderPool().values()) {
      cardholder.addObserver(logManager);
    }
  }

  /**
   * Get the CardholderManager of this transit system.
   *
   * @return A CardholderManager of this TransitSystem.
   */
  public CardholderManager getCardholderManager() {
    return this.cardholderManager;
  }

  /**
   * Get the CardManager of this transit system.
   *
   * @return A CardManager of this TransitSystem.
   */
  public CardManager getCardManager() {
    return this.cardManager;
  }

  /**
   * Get the FareManager of this transit system.
   *
   * @return A FareManager of this TransitSystem.
   */
  public FareManager getFareManager() {
    return this.fareManager;
  }

  /**
   * Add a new Admin user.
   *
   * @param adminUser A AdminUser we want to add.
   */
  public void addAdminUser(AdminUser adminUser) {
    if (this.adminUsers.containsKey(adminUser.getEmployeeId())) {
      setChanged();
      notifyObservers("The admin user ID " + adminUser.getEmployeeId() + " already exists!");
    } else {
      this.adminUsers.put(adminUser.getEmployeeId(), adminUser);
      setChanged();
      notifyObservers("The admin user " + adminUser.getEmployeeId() + " has been created!");
    }
    /* serialize system if new admin user is added */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the admin user instance according to employee id. Throw NoSuchAdminUserException if this
   * admin user employee ID has not been registered in this transit system.
   *
   * @param employeeId the id of this admin user.
   * @return A AdminUser according to employee id.
   */
  public AdminUser getAdminUser(String employeeId) throws NoSuchAdminUserException {
    if (!adminUsers.containsKey(employeeId)) {
      throw new NoSuchAdminUserException();
    }
    return adminUsers.get(employeeId);
  }

  /**
   * Get the system map of this transit system.
   *
   * @return the system map of this transit system.
   */
  public SystemMap getSystemMap() {
    return this.systemMap;
  }
}
