package system;

import card.Card;
import fare.BusStrategy;
import fare.CapStrategy;
import fare.FareStrategy;
import fare.SubwayStrategy;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Observable;
import log.LogManager;
import ride.Ride;
import ride.RideRecord;
import ride.RideRecord.RideIterator;
import serialize.SerializeManager;

/**
 * An admin user has a employee ID. An admin user has access to information stored in this transit
 * system. An admin user can add or remove fare strategy, set one time fare in bus strategy, set
 * fare per station in subway strategy, set max fare per trip in cap strategy, set reach cap time in
 * ride record. An admin user can also collect statistics including fare collected, total distance
 * travelled, total stations covered in all rides by day, week or month.
 */
public class AdminUser extends Observable implements Serializable {

  private final String employeeId;
  private final TransitSystem transitSystem;
  private String password;
  private File profilePic;

  /**
   * Construct a new AdminUser.
   *
   * @param transitSystem this transit system.
   * @param employeeId the employee id for this admin user.
   */
  private AdminUser(String employeeId, String password, TransitSystem transitSystem) {
    this.employeeId = employeeId;
    this.password = password;
    this.transitSystem = transitSystem;
    profilePic = new File("phase2/src/views/resources/avatar.png");
    this.addObserver(LogManager.getInstance());
    setChanged();
    notifyObservers("New Admin User added! Employee ID: " + employeeId + ".");
  }

  /**
   * Factory method to get a new instance of AdminUser.
   *
   * @param employeeId employeeId of this new admin user.
   * @param transitSystem transit system of this new admin user.
   * @return a new instance of AdminUser
   */
  public static AdminUser getInstance(
      String employeeId, String password, TransitSystem transitSystem) {
    return new AdminUser(employeeId, password, transitSystem);
  }

  /**
   * Verify whether the input is the same as this admin user's password.
   *
   * @return whether the input is the same as this admin user's password.
   */
  public boolean verifyPassword(String input) {
    return this.password.equals(input);
  }

  /**
   * Get the employee id of this admin user.
   *
   * @return A string representing this employee id.
   */
  String getEmployeeId() {
    return employeeId;
  }

  /**
   * Add a new fare strategy according to strategy name and strategy type.
   *
   * @param strategyName name of this new strategy.
   */
  public void addFareStrategy(String strategyName, FareStrategy fareStrategy) {
    transitSystem.getFareManager().getFarePolicy().put(strategyName, fareStrategy);
    setChanged();
    notifyObservers("New Fare Strategy added! Strategy Name: " + strategyName + ".");
    /* serialize fare manager if a fare strategy is added */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Remove a fare strategy from fare manager's fare policy.
   *
   * @param strategyName name of this strategy to move.
   */
  public void removeFareStrategy(String strategyName) {
    HashMap<String, FareStrategy> farePolicy = transitSystem.getFareManager().getFarePolicy();
    if (farePolicy.keySet().contains(strategyName)) {
      farePolicy.remove(strategyName);
      setChanged();
      notifyObservers(strategyName + " Fare Strategy Has Been Removed!");
    } else {
      setChanged();
      notifyObservers("No Such Fare Strategy: " + strategyName + " Fare Strategy!");
    }
    /* serialize fare manager if a fare strategy is removed */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Modify one time fare of a BusStrategy.
   *
   * @param newOneTimeFare the new fare for this bus trip.
   * @param strategyName the name of the strategy.
   */
  public void setOneTimeFare(String strategyName, double newOneTimeFare) {
    BusStrategy strategy;
    try {
      strategy = (BusStrategy) (transitSystem.getFareManager().getFarePolicy().get(strategyName));
      strategy.setOneTimeFare(newOneTimeFare);
      setChanged();
      notifyObservers(
          strategyName
              + " Fare Strategy is Changed!"
              + System.lineSeparator()
              + "One Time Fare is set to: "
              + newOneTimeFare
              + " .");
      /* serialize fare manager if one time fare is changed */
      SerializeManager.getInstance().writeObject();
    } catch (Exception e) {
      setChanged();
      notifyObservers(strategyName + " Fare Strategy DOES NOT have One Time Fare!");
    }
  }

  /**
   * Modify fare per station of a SubwayStrategy.
   *
   * @param newPerStationFare the new fare for each station of the subway.
   * @param strategyName the name of the strategy.
   */
  public void setPerStationFare(String strategyName, double newPerStationFare) {
    SubwayStrategy strategy;
    try {
      strategy =
          (SubwayStrategy) (transitSystem.getFareManager().getFarePolicy().get(strategyName));
      strategy.setFarePerUnit(newPerStationFare);
      setChanged();
      notifyObservers(
          strategyName
              + " Fare Strategy is Changed!"
              + System.lineSeparator()
              + "One Time Fare is set to: "
              + newPerStationFare
              + " .");
      /* serialize fare manager if per station fare is changed */
      SerializeManager.getInstance().writeObject();
    } catch (Exception e) {
      setChanged();
      notifyObservers(strategyName + " Fare Strategy DOES NOT have Per Station Fare!");
    }
  }

  /**
   * Modify reach cap fare of a CapStrategy.
   *
   * @param newReachCapFare the new reach cap fare.
   * @param strategyName the name of the strategy.
   */
  public void setReachCapFare(String strategyName, double newReachCapFare) {
    CapStrategy strategy;
    try {
      strategy = (CapStrategy) transitSystem.getFareManager().getFarePolicy().get(strategyName);
      strategy.setReachCapFare(newReachCapFare);
      setChanged();
      notifyObservers(
          strategyName
              + " Fare Strategy is Changed!"
              + System.lineSeparator()
              + "Reach Cap Fare is set to: "
              + newReachCapFare
              + " .");
      /* serialize fare manager if reach cap fare is changed */
      SerializeManager.getInstance().writeObject();
    } catch (Exception e) {
      setChanged();
      notifyObservers(strategyName + " Fare Strategy DOES NOT have Reach Cap Fare!");
    }
  }

  /**
   * Modify max fare per trip of a CapStrategy.
   *
   * @param newMaxFarePerTrip the new max fare per trip.
   * @param strategyName the name of this strategy.
   */
  public void setMaxFarePerTrip(String strategyName, double newMaxFarePerTrip) {
    CapStrategy strategy;
    try {
      strategy = (CapStrategy) transitSystem.getFareManager().getFarePolicy().get(strategyName);
      strategy.setMaxFarePerTrip(newMaxFarePerTrip);
      setChanged();
      notifyObservers(
          strategyName
              + " Fare Strategy is Changed!"
              + System.lineSeparator()
              + "Maximal Fare Per Trip is set to: "
              + newMaxFarePerTrip
              + " .");
      /* serialize fare manager if max fare per trip is changed */
      SerializeManager.getInstance().writeObject();
    } catch (Exception e) {
      setChanged();
      notifyObservers(strategyName + " Fare Strategy DOES NOT have Maximal Fare Per Trip!");
    }
  }

  /**
   * Modify the reach cap time in RideRecord. the input new cap time should be in minutes.
   *
   * @param newCapTime the new cap time for this transit system.
   */
  public void setReachCapTime(double newCapTime) {
    HashMap<String, Card> cardPool = transitSystem.getCardManager().getCardPool();
    for (Card card : cardPool.values()) {
      card.getRideRecords().setReachCapTime(newCapTime * 60 * 1000);
    }
    setChanged();
    notifyObservers("Reach Cap Time is set to: " + newCapTime + " .");
    /* serialize cards if reach cap time is changed */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Modify the initial balance of new cards that will be created later.
   *
   * @param newInitialBalance new initial balance
   */
  public void setInitialBalance(double newInitialBalance) {
    Card.setInitialBalance(newInitialBalance);
    setChanged();
    notifyObservers("Initial Balance Of New Cards is set to: $" + newInitialBalance + ".");
    /* serialize cards if initial balance is changed */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Calculate the total number of stations/total fare of transit system by a time
   * period(day/week/month).
   *
   * @param requestContent the information that admin user want to report.
   * @param reportTimePeriod the time period that admin user want to report.
   */
  public double getStatistics(String requestContent, String reportTimePeriod) {
    double statistics = 0;
    HashMap<String, Card> cardPool = transitSystem.getCardManager().getCardPool();
    for (Card card : cardPool.values()) {
      RideIterator iterator = getIterator(card.getRideRecords(), reportTimePeriod);
      if (iterator == null) {
        System.out.println("Invalid Report Period!");
        setChanged();
        notifyObservers("Failed To Report: Invalid Report Period!");
      } else {
        if (iterator.hasNext()) {
          for (Ride ride : iterator.next()) {
            switch (requestContent) {
              case "TOTAL FARE COLLECTED":
                statistics += ride.getFare();
                break;
              case "TOTAL DISTANCE TRAVELLED":
                statistics += ride.getDistanceTravelled();
                break;
              case "TOTAL STATIONS COVERED":
                if (ride.getPath() == null) {
                  statistics += 0;
                } else {
                  statistics += ride.getPath().size();
                }
                break;
              default:
                break;
            }
          }
        }
      }
    }
    return statistics;
  }

  /**
   * Get a ride iterator to get the right ride records in this report time period.
   *
   * @param rideRecord the ride record of this card.
   * @param reportTimePeriod the time period that admin user want to report.
   * @return A RideIterator to get the right ride records in this report time period.
   */
  private RideIterator getIterator(RideRecord rideRecord, String reportTimePeriod) {
    RideIterator iterator = null;
    switch (reportTimePeriod) {
      case "DAY":
        iterator = rideRecord.dailyIterator();
        break;
      case "WEEK":
        iterator = rideRecord.weeklyIterator();
        break;
      case "MONTH":
        iterator = rideRecord.monthlyIterator();
        break;
      default:
        break;
    }
    return iterator;
  }

  /**
   * get the admin user's profile picture file.
   *
   * @return the file containing the admin user's profile picture.
   */
  public File getProfilePic() {
    return profilePic;
  }

  /**
   * Set the admin user's profile picture file.
   *
   * @param profilePic the file containing the admin user's profile picture.
   */
  public void setProfilePic(File profilePic) {
    this.profilePic = profilePic;
    setChanged();
    notifyObservers("Admin User " + employeeId + " Profile Updated!");
    /* serialize system if admin user changed profile picture */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Set the admin user's password.
   *
   * @param newPassword the admin user's new password
   */
  public void setPassword(String newPassword) {
    String previousPassword = this.password;
    this.password = newPassword;
    setChanged();
    notifyObservers(
        "Admin User "
            + employeeId
            + " Password change from "
            + previousPassword
            + " to "
            + this.password
            + " !");
    /* serialize system if admin user changed password */
    SerializeManager.getInstance().writeObject();
  }
}
