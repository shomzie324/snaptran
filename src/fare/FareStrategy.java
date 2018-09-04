package fare;

import ride.RideRecord;

/** A fare strategy can calculate fare. */
public interface FareStrategy {

  /**
   * Calculate the fare of the latest ride in the ride record.
   *
   * @param rideRecord the ride record of this card.
   * @return A double that represents the fare of the latest ride.
   */
  double calculateFare(RideRecord rideRecord);
}
