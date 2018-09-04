package fare;

import java.io.Serializable;
import map.SystemMap;
import ride.Ride;

/**
 * A bus strategy is a subclass fo cap strategy. Normally, a bus strategy charge only when the card
 * taps in. However, if the cardholder forgot to tap in, a bus strategy will charge when the
 * cardholder tap out. As it is in cap strategy, fare for a ride is capped by max fare per trip if
 * there is a continuous ride within cap hour. A bus strategy charge a ride by constant amount, that
 * is one time fare.
 */
public class BusStrategy extends CapStrategy implements Serializable {

  private double oneTimeFare;

  /**
   * Construct a new bus strategy.
   *
   * @param systemMap the system map used to calculate fare in this bus strategy.
   */
  public BusStrategy(SystemMap systemMap) {
    super(systemMap);
    /* set one time fare by $2 by default, one time fare can be changed by admin user
    in this transit system. */
    this.oneTimeFare = 2;
  }

  /**
   * Check whether or not this bus ride should be charged.
   *
   * @param thisRide ride to check.
   * @return true if this ride should be charged.
   */
  @Override
  public boolean shouldTakeCharge(Ride thisRide) {
    boolean normalTapIn =
        thisRide.getTapInLocation() != null
            && thisRide.getTapInTime() != null
            && thisRide.getTapOutLocation() == null
            && thisRide.getTapOutTime() == null
            && thisRide.getFare() == 0;
    boolean forgotTapIn =
        "(Missed Tap In)".equals(thisRide.getTapInLocation())
            && thisRide.getTapInTime() == null
            && thisRide.getTapOutLocation() != null
            && thisRide.getTapOutTime() != null
            && thisRide.getFare() == 0;
    return normalTapIn | forgotTapIn;
  }

  /**
   * Get the fare of this bus ride.
   *
   * @param thisRide ride to charge.
   * @return A double representing the fare of this ride.
   */
  @Override
  public double getOneTimeFare(Ride thisRide) {
    String tapOutLocation = thisRide.getTapOutLocation();
    /* If this ride has missed tap out information */
    if (tapOutLocation != null && tapOutLocation.equals("(Missed Tap Out)")) {
      thisRide.setFare(oneTimeFare); // record fare has been charged for this ride
      return 0; // does not charge a ride with missed tap out, since already charged when tap in
    }
    /* record fare should be charged for a normal tap in or missed tap in */
    thisRide.setFare(oneTimeFare);
    return this.oneTimeFare;
  }

  /**
   * Set the fare should be charged for a single bus ride.
   *
   * @param oneTimeFare fare should be charged for a single bus ride.
   */
  public void setOneTimeFare(double oneTimeFare) {
    this.oneTimeFare = oneTimeFare;
  }
}
