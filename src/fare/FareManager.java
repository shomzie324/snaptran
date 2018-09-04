package fare;

import card.Card;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Observable;
import log.LogManager;
import ride.RideRecord;

public class FareManager extends Observable implements Serializable {

  private final HashMap<String, FareStrategy> farePolicy;

  /** Construct a new fare manager. */
  public FareManager(FareStrategy busStrategy, FareStrategy subwayStrategy) {
    farePolicy = new HashMap<>();
    this.farePolicy.put("BUS", busStrategy);
    this.farePolicy.put("SUBWAY", subwayStrategy);
    this.addObserver(LogManager.getInstance());
  }

  /**
   * Get the fare policy for this transit system.
   *
   * @return A HashMap that values are corresponding to its keys' strategy name.
   */
  public HashMap<String, FareStrategy> getFarePolicy() {
    return farePolicy;
  }

  /**
   * Calculates and processes a charge for the trip of a given card. Raises
   *
   * @param card the card that is being used for the ride.
   */
  public void takeCharge(Card card) {
    RideRecord cardRideRecord = card.getRideRecords();
    FareStrategy fareStrategy =
        farePolicy.get(cardRideRecord.getLatestRide().getTransitType().toString());
    double fareToDeduct = fareStrategy.calculateFare(cardRideRecord);
    card.deductBalance(fareToDeduct);
    if (fareToDeduct != 0) {
      /* round to 2 decimal point if there are more than 2 decimal point */
      BigDecimal bigDecimal = new BigDecimal(Double.valueOf(fareToDeduct).toString());
      bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
      fareToDeduct = bigDecimal.doubleValue();
      setChanged();
      notifyObservers(
          "Fare: $"
              + fareToDeduct
              + " has been successfully deducted from card #"
              + card.getCardId()
              + System.lineSeparator()
              + "The new balance is: $"
              + card.getBalance());
    }
  }
}
