package fare;

import java.io.Serializable;
import java.util.LinkedList;
import map.SystemMap;
import map.Vertex;
import ride.Ride;

/**
 * A subway strategy is a subclass of cap strategy. A subway strategy only charge when the
 * cardholder tap out. A subway strategy calculate fare by the product of number of distance
 * travelled and fare per unit. As it is in cap strategy, fare for a ride is capped by max fare per
 * trip if there is a continuous ride within cap hour.
 */
public class SubwayStrategy extends CapStrategy implements Serializable {

  private double farePerUnit;

  /**
   * Construct a new subway strategy.
   *
   * @param systemMap the system map for this transit system.
   */
  public SubwayStrategy(SystemMap systemMap) {
    super(systemMap);
    /* set fare per unit by $0.5 by default*/
    this.farePerUnit = 0.5;
  }

  /**
   * Check whether or not this subway ride should take charge.
   *
   * @param ride ride to check.
   * @return true if this ride should be charged.
   */
  @Override
  public boolean shouldTakeCharge(Ride ride) {
    return (ride.getTapInLocation() != null
        && ride.getTapInTime() != null
        && ride.getTapOutLocation() != null
        && ride.getTapOutTime() != null
        && ride.getFare() == 0);
  }

  /**
   * Get the fare should be charged for this subway ride.
   *
   * @param ride ride to charge.
   * @return A double representing the fare of this ride.
   */
  @Override
  public double getOneTimeFare(Ride ride) {
    double fareToDeduct;
    String tapOutLocation = ride.getTapOutLocation();
    double distanceTravelled;
    if (tapOutLocation.equals("(Missed Tap Out)")) {
      distanceTravelled = ride.getDistanceTravelled(); // get distance travelled from ride record
    } else { // a normal tap out
      String tapInLocation = ride.getTapInLocation();
      /* assume the cardholder went through the shortest path */
      /* from tap in location and tap out location */
      LinkedList<Vertex> path = getSystemMap().getShortestPath(tapInLocation, tapOutLocation);
      ride.setPath(path); // record the transit path in this ride
      distanceTravelled = path.getLast().getDistance(); // get the distance travelled in this path
      ride.setDistanceTravelled(distanceTravelled); // record teh distance travelled in this ride
    }
    fareToDeduct = distanceTravelled * farePerUnit;
    ride.setFare(fareToDeduct);
    return fareToDeduct;
  }

  /**
   * Set the fare should be charged for a unit of distance in this subway strategy. Notice that
   * distance between two stations can have more that 1 unit.
   *
   * @param farePerUnit fare should be charged for a unit of distance.
   */
  public void setFarePerUnit(double farePerUnit) {
    this.farePerUnit = farePerUnit;
  }
}
