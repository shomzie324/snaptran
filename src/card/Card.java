package card;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Observable;
import log.LogManager;
import ride.Ride;
import ride.RideRecord;
import serialize.SerializeManager;

/**
 * Represents a card owned by a cardholder in this transit system. A card has its ID, state (active
 * or suspended or removed), balance, and its ride records. A card can show recent rides of itself.
 */
public class Card extends Observable implements Serializable {

  /* The initial balance of a new card, may be modified by AdminUser. */
  private static double INITIAL_BALANCE = 19;
  private final String cardId;
  /* The ride records of this card, which can be traced back to the time when this card is first
   * created. */
  private final RideRecord rideRecords;
  /* The state of a card can be State.ACTIVE, State.SUSPENDED, State.REMOVED. An active card can be
   * used normally. A suspended card cannot be used to tap in or tap out, may be reactivated by the
   * cardholder who owns the card. A removed card will never be activated, but the cardholder may
   * transfer the balance (if positive) to another card.*/
  private State state;

  private double balance;

  /**
   * Create a new card.
   *
   * @param cardId The card id that will be associated with the new card.
   */
  private Card(String cardId) {
    /* set cardId of this card according to given cardId */
    this.cardId = cardId;
    /* set state of a new card to active by default */
    this.state = State.ACTIVE;
    /* set balance of a new card according to INITIAL_BALANCE */
    this.balance = INITIAL_BALANCE;
    /* create a new container to store ride records of this card */
    this.rideRecords = new RideRecord();
    this.addObserver(LogManager.getInstance());
    setChanged();
    notifyObservers("New Card Created! Card ID: " + cardId + ".");
  }

  /**
   * Create a new card.
   *
   * @param cardId the id of the card.
   * @return A new Card.
   */
  public static Card getInstance(String cardId) {
    return new Card(cardId);
  }

  /**
   * Modify the initial balance of new cards created in this transit system. All new cards generated
   * after this modification will have the new initial balance when they are created. Previously
   * generated cards will not be affected. This action should be authorized by a admin user in this
   * transit system.
   *
   * @param newInitialBalance the new balance of cards created after this modification.
   */
  public static void setInitialBalance(double newInitialBalance) {
    INITIAL_BALANCE = newInitialBalance;
  }

  /**
   * Get the cardId of this card.
   *
   * @return A String representation of the cardId of this card.
   */
  public String getCardId() {
    return this.cardId;
  }

  /**
   * Add a new ride to the ride records associated with this card.
   *
   * @param newRide The ride object to be added to the ride records.
   */
  void recordRide(Ride newRide) {
    this.rideRecords.add(newRide);
    /* serialize cards if tap in info of a ride of card is set up*/
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the ride records associated with this card.
   *
   * @return A RideRecord object representing the collection of rides associated with this card.
   */
  public RideRecord getRideRecords() {
    return this.rideRecords;
  }

  /**
   * Get the current balance of this card.
   *
   * @return The current balance for this card.
   */
  public double getBalance() {
    BigDecimal bigDecimal = new BigDecimal(Double.valueOf(this.balance).toString());
    bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    this.balance = bigDecimal.doubleValue();
    return this.balance;
  }

  /**
   * Deposit money into this card.
   *
   * @param amount the amount of money to be deposited into this card.
   */
  public void addBalance(double amount) {
    this.balance += amount;
    setChanged();
    notifyObservers("Balance Added To Card " + cardId + " : $" + amount + ".");
    /* serialize cards if balance is added */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Deduct money from this card.
   *
   * @param amount the amount of money to be deducted from this card.
   */
  public void deductBalance(double amount) {
    /* round to 2 decimal point if there are more than 2 decimal point */
    BigDecimal bigDecimal = new BigDecimal(Double.valueOf(amount).toString());
    bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    amount = bigDecimal.doubleValue();
    this.balance -= amount;
    if (amount != 0) {
      setChanged();
      notifyObservers(
          "Balance Deducted From Card "
              + cardId
              + " : $"
              + amount
              + "."
              + System.lineSeparator()
              + "New Balance: "
              + "$"
              + balance
              + ".");
    }
    /* serialize cards if balance is deducted */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the state enum of this card. The state represents the card's usability.
   *
   * @return current state of this card.
   */
  public State getState() {
    return state;
  }

  /**
   * Change the state enum of this card. The state represents the card's usability.
   *
   * @param newState new state for this card.
   */
  public void setState(State newState) {
    this.state = newState;
    setChanged();
    notifyObservers("State of Card " + cardId + " is set to " + state + ".");
    /* serialize cards if card state is modified */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Report location, time, path, fare of  recent trips according to period.
   *
   * @return a String representation of the recent trips associated with this card.
   */
  public String reportRecentTrips(String reportPeriod) {
    StringBuilder trips = new StringBuilder();
    switch (reportPeriod) {
      case "RECENT 3":
        int i = this.rideRecords.getAllRides().size();
        for (int j = i - 1; j > i - 4 && j > -1; j--) {
          trips.append(this.rideRecords.getAllRides().get(j));
          trips.append(System.lineSeparator());
          trips.append(System.lineSeparator());
        }
        break;
      case "ALL":
        for (Ride ride : rideRecords.getAllRides()) {
          trips.append(ride);
          trips.append(System.lineSeparator());
          trips.append(System.lineSeparator());
        }
        break;
      default:
        RideRecord.RideIterator rideIterator =
            rideRecords.getRideIteratorByReportPeriod(reportPeriod);
        if (rideIterator != null) {
          for (Ride ride : rideIterator.next()) {
            trips.append(ride);
            trips.append(System.lineSeparator());
            trips.append(System.lineSeparator());
          }
        }
        break;
    }
    return trips.toString();
  }

  /**
   * Enum State. A state represents the state of card. The state of a card can be modified by its
   * cardholder. Only a ACTIVE card state can be used for tapping features of the Transit System.
   */
  public enum State {
    SUSPENDED,
    ACTIVE,
    REMOVED
  }
}
