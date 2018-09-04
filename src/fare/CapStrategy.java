package fare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import map.SystemMap;
import map.Vertex;
import ride.Ride;
import ride.RideRecord;

/**
 * A cap strategy has a system map to calculate fare. A cap strategy has maximum fare to charge for
 * a continuous trip within cap time. A cap strategy has a reach cap fare, which is the fare should
 * be charged after a continuous trip within cap time reached max fare per trip. reach cap fare and
 * max fare per trip can be modified by admin user.
 */
public abstract class CapStrategy implements FareStrategy, Serializable {

  private final SystemMap systemMap;
  private double reachCapFare;
  private double maxFarePerTrip;

  /**
   * Construct a new cap strategy. By default, reachCapFare is set to $0, maxFarePerTrip is set to
   * $6 as specified in handout.
   *
   * @param systemMap the system map for this transit system.
   */
  CapStrategy(SystemMap systemMap) {
    this.systemMap = systemMap;
    /* set reach cap fare to 0 by default */
    this.reachCapFare = 0;
    /* set max fare per trip to 6 by default */
    this.maxFarePerTrip = 6;
  }

  /**
   * Create a new fare strategy according to transit type.
   *
   * @param systemMap system map used to calculate fare.
   * @param transitType bus or subway
   * @return an instance of fare strategy
   */
  public static CapStrategy getNewFareStrategyInstance(
      SystemMap systemMap, Ride.TransitType transitType) {
    if (transitType == Ride.TransitType.BUS) {
      return new BusStrategy(systemMap);
    } else {
      return new SubwayStrategy(systemMap);
    }
  }

  /**
   * Calculate the fare should be deducted for the latest ride. And check if there is the latest
   * previous ride has missed tap out. If previous ride has a missed tap out, calculate fare for the
   * ride with missed tap out, record path and fare for it and deduct fare together with this ride.
   *
   * @param rideRecord the ride record of this card.
   * @return A double that represents the fare for this ride, and fare of previous ride with missed
   *     tap out (if there is).
   */
  @Override
  public double calculateFare(RideRecord rideRecord) {
    double fareToDeduct = 0;
    /* Check whether the latest previous ride has missed tap out, */
    /* if there is, charge together with this ride. */
    fareToDeduct += handleMissedTapOut(rideRecord);
    /* calculate fare for this ride. */
    Ride thisRide = rideRecord.getLatestRide();
    if (shouldTakeCharge(thisRide)) {
      /* check whether this ride has reached cap and record fare should be deducted in this ride */
      handleReachCap(rideRecord);
      fareToDeduct += thisRide.getFare();
    }
    /* add to tap in times, tap out times and arrived times for a station/stop if ride has end */
    recordPathStatistics(thisRide);
    return fareToDeduct;
  }

  /**
   * Firstly, check whether the latest previous ride of this ride(the latest ride) has missed tap
   * out. Secondly, If there is, mark the tap out location with "(Missed Tap Out)". Then calculate
   * fare should be deducted for the ride with missed tap out. Lastly, return fare should be
   * deducted for it.
   *
   * @param rideRecord a container with all ride records of this card.
   * @return fare should be deducted for the ride with missed tap out if there is any, return 0 if
   *     no ride with missed tap out.
   */
  private double handleMissedTapOut(RideRecord rideRecord) {
    double fareToDeduct = 0;
    if (hasMissedTapOut(rideRecord)) {
      Ride rideWithMissedTapOut = rideRecord.getAllRides().get(rideRecord.getAllRides().size() - 2);
      //          rideRecord.getCurrentRides().get(rideRecord.getCurrentRides().size() - 2);
      /* mark the tap out location of this ride by "(Missed Tap Out)" */
      /* to indicate this ride has missed tap out information.*/
      rideWithMissedTapOut.setTapOutLocation("(Missed Tap Out)");
      /* calculate fare should be deducted for this ride with missed tap out */
      fareToDeduct += calculateMissedTapOut(rideWithMissedTapOut);
      /* check if this single ride with missed tap out has reached max fare per trip, */
      /* if it is, only charge max fare per trip.*/
      if (fareToDeduct
          >= maxFarePerTrip) { // fare of this ride with missed tap out reach max fare per trip
        fareToDeduct = maxFarePerTrip; // only charge max fare per trip
        rideWithMissedTapOut.setFare(
            maxFarePerTrip); // correct fare record in this ride by max fare per trip
      }
    }
    return fareToDeduct;
  }

  /**
   * Check whether or not the latest second ride has missed tap out.
   *
   * @param rideRecord the ride record of this card.
   * @return true if the latest previous ride of this ride(the latest ride) has missed tap out
   *     information.
   */
  private boolean hasMissedTapOut(RideRecord rideRecord) {
    ArrayList<Ride> allRecord = rideRecord.getAllRides();
    boolean noPreviousRide = allRecord.size() == 1;
    if (noPreviousRide) {
      return false; // no previous ride, thus no ride with missed tap out previously
    } else {
      Ride latestPreviousRide = allRecord.get(allRecord.size() - 2); // the latest previous ride
      return latestPreviousRide.getTapOutLocation() == null
          && latestPreviousRide.getTapOutTime() == null;
    }
  }

  /**
   * Calculate fare should be deducted for this ride with missed tap out. If the route that this
   * card tapped in only has one direction, assume the cardholder went to the terminal. If the route
   * that this card tapped in has two direction, assume the cardholder went to the farthest end.
   *
   * @param rideWithMissedTapOut the ride with missed tap out to calculate fare.
   * @return A double representing the fare should be charged for this ride with missed tap out.
   */
  private double calculateMissedTapOut(Ride rideWithMissedTapOut) {
    String tapInLocation = rideWithMissedTapOut.getTapInLocation();
    /* assume cardholder went to farthest end if has missed tap out */
    LinkedList<Vertex> path =
        getSystemMap().getPathToFarthestEnd(tapInLocation, rideWithMissedTapOut);
    /* record the path from tap in location to farthest end in this ride */
    rideWithMissedTapOut.setPath(path);
    /* record the distance travelled from tap in location to farthest end in this ride */
    rideWithMissedTapOut.setDistanceTravelled(path.getLast().getDistance());
    double fareToDeduct = 0;
    /* get the cap strategy should be used to calculate fare by transit type */
    CapStrategy capStrategy =
        CapStrategy.getNewFareStrategyInstance(systemMap, rideWithMissedTapOut.getTransitType());
    fareToDeduct += capStrategy.getOneTimeFare(rideWithMissedTapOut);
    /* add to tap in times, tap out times and arrived times */
    /* for a station/stop since this ride has end */
    recordPathStatistics(rideWithMissedTapOut);
    return fareToDeduct;
  }

  /**
   * If total fare collected of the continuous trip within cap hour has reached max fare per trip,
   * then this ride is free. If total fare collected of the continuous trip within cap hour plus
   * fare should be charged for this ride reached max fare per trip, only charge the difference
   * between max fare per trip. Otherwise, charge the normal amount of fare for this ride.
   *
   * @param rideRecord a container with all ride records of this card.
   */
  private void handleReachCap(RideRecord rideRecord) {
    Ride thisRide = rideRecord.getLatestRide();
    double oneTimeFare = getOneTimeFare(thisRide); // fare should be charged for this ride
    /* get total fare collected of the continuous trip within 2 hours */
    double fareDeductedThisTrip = getFareThisTrip(rideRecord);
    if (fareDeductedThisTrip >= maxFarePerTrip) {
      thisRide.setFare(reachCapFare);
    } else if (fareDeductedThisTrip + oneTimeFare >= maxFarePerTrip) {
      /* only charge the difference between max fare per trip */
      thisRide.setFare(maxFarePerTrip - fareDeductedThisTrip + reachCapFare);
    } else {
      thisRide.setFare(oneTimeFare);
    }
  }

  /**
   * Calculate total fare collected in this continuous trip (if there is) within cap hour. A trip is
   * continuous if every ride's tap in location is same as previous ride's tap out location(if there
   * is). There are two ways to end a trip, one is that a ride is disjoint with its previous ride,
   * the other is that tap in time of previous ride is over cap hour.
   *
   * @param rideRecord a container with all ride records of this card.
   * @return total fare collected in this continuous trip(if there is) within cap hour.
   */
  private double getFareThisTrip(RideRecord rideRecord) {
    double fareDeductedThisTrip = 0;
    ArrayList<Ride> rideWithinCapHour = rideRecord.getCurrentRides();
    for (int i = rideRecord.getCurrentRides().size() - 1; i >= 1; i--) {
      Ride thisRide = rideWithinCapHour.get(i);
      Ride previousRide = rideWithinCapHour.get(i - 1);
      /* check whether previous ride and this ride is continuous */
      if (previousRide.getTapOutLocation().equals(thisRide.getTapInLocation())) {
        fareDeductedThisTrip += previousRide.getFare();
      } else { // disjoint trip
        break;
      }
    }
    return fareDeductedThisTrip;
  }

  /**
   * Set the fare should be charged after a card has reached cap in this cap strategy.
   *
   * @param reachCapFare the fare should be charged after a card has reached cap.
   */
  public void setReachCapFare(double reachCapFare) {
    this.reachCapFare = reachCapFare;
  }

  /**
   * Set the maximum fare can be charged for a continuous trip within cap hours in this cap
   * strategy.
   *
   * @param maxFarePerTrip the maximum fare can be charged for a continuous trip within cap hours.
   */
  public void setMaxFarePerTrip(double maxFarePerTrip) {
    this.maxFarePerTrip = maxFarePerTrip;
  }

  /**
   * Get the system map used to calculate fare in this cap strategy.
   *
   * @return the system map used to calculate fare in this cap strategy.
   */
  SystemMap getSystemMap() {
    return systemMap;
  }

  /**
   * Record transit path in this ride record if it has not been recorded. Record tap in times, tap
   * out times, arrived times of stations/stops in this path.
   *
   * @param ride ride to get statistics.
   */
  private void recordPathStatistics(Ride ride) {
    String tapInLocation = ride.getTapInLocation();
    String tapOutLocation = ride.getTapOutLocation();
    if (tapOutLocation != null) { // this ride has ended
      LinkedList<Vertex> path = ride.getPath(); // get the transit path from this ride record
      if (path == null) { // path has not been recorded in this ride
        if (tapInLocation.equals("(Missed Tap In)")) {
          /* assume the cardholder started from the farthest end if has missed tap in information */
          path = systemMap.getPathToFarthestEnd(tapOutLocation, ride);
        } else {
          /* assume the cardholder went through the shortest path from tap in location to tap out
          location for a ride with full information */
          path = systemMap.getShortestPath(tapInLocation, tapOutLocation);
        }
        ride.setPath(path);
        ride.setDistanceTravelled(path.getLast().getDistance());
      }
      recordVertexStatistics(ride);
    }
  }

  /**
   * Add to tap in times, tap out times and arrived times for stations/stop on the path of this
   * ride.
   *
   * @param ride ride to get statistics of stations/stops.
   */
  private void recordVertexStatistics(Ride ride) {
    LinkedList<Vertex> path = ride.getPath();
    /* add tap in time for tap in station/stop */
    path.get(0).addPassengerFlow(ride.getTapInTime().getTimeInMillis(), "TAP IN TIMES");
    /* add arrived time for all passed stations/stops and tap out station/stop */
    Calendar time = ride.getTapOutTime();
    if (time == null) {
      time = ride.getTapInTime();
    }
    for (Vertex vertex : path.subList(1, path.size())) {
      vertex.addPassengerFlow(time.getTimeInMillis(), "ARRIVED TIMES");
    }
    if (path.size() == 1) { // a path with only one station/stop is also arrived station/stop
      path.get(path.size() - 1).addPassengerFlow(time.getTimeInMillis(), "ARRIVED TIMES");
    }
    /* add tap out time for tap out vertex */
    path.get(path.size() - 1).addPassengerFlow(time.getTimeInMillis(), "TAP OUT TIMES");
  }

  /**
   * Check whether or not this ride should be charged.
   *
   * @param thisRide ride to check.
   * @return true if this ride should take charge.
   */
  protected abstract boolean shouldTakeCharge(Ride thisRide);

  /**
   * Get the fare should be charged for this ride.
   *
   * @param thisRide the ride to charge.
   * @return A double representing the fare should be charged for this ride.
   */
  protected abstract double getOneTimeFare(Ride thisRide);
}
