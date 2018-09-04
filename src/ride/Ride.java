package ride;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.LinkedList;
import map.Vertex;
import serialize.SerializeManager;

/**
 * a ride starts to record information when the cardholder enters the transit system, and end
 * recording information when the cardholder first time leaves the transit system. A ride will only
 * record information of a pair of tap in and tap out(i.e. no more taps between this pair of tap in
 * and tap out.). A ride records information including time, location, fare charged for this ride,
 * distance travelled in this ride, transit path of this ride and transit type of this ride.
 */
public class Ride implements Serializable {

  private final Calendar tapInTime;
  private final String tapInLocation;
  private final TransitType transitType;
  private Calendar tapOutTime;
  private String tapOutLocation;
  private double distanceTravelled;
  private double fare;
  private LinkedList<Vertex> path;

  /**
   * Create a new ride,start record by storing tap in time, tap in location and transit type.
   *
   * @param tapInTime tap in time of this ride
   * @param tapInLocation tap out time of this ride
   * @param transitType transit type of this ride
   */
  private Ride(Calendar tapInTime, String tapInLocation, TransitType transitType) {
    this.tapInTime = tapInTime;
    this.tapInLocation = tapInLocation;
    this.transitType = transitType;
  }

  /**
   * Create a new ride,start record by storing tap in time, tap in location and transit type.
   *
   * @param tapInTime tap in time of this ride
   * @param tapInLocation tap out time of this ride
   * @param transitType transit type of this ride
   * @return A new Ride.
   */
  public static Ride getInstance(
      Calendar tapInTime, String tapInLocation, TransitType transitType) {
    return new Ride(tapInTime, tapInLocation, transitType);
  }

  /**
   * Get the tap in time of this ride.
   *
   * @return A Calendar representing when this ride start.
   */
  public Calendar getTapInTime() {
    return this.tapInTime;
  }

  /**
   * Get the tap out time of this ride.
   *
   * @return A Calendar representing when this ride end.
   */
  public Calendar getTapOutTime() {
    return this.tapOutTime;
  }

  /**
   * Set the tap out time of this ride.
   *
   * @param tapOutTime A Calendar representing when this ride end.
   */
  public void setTapOutTime(Calendar tapOutTime) {
    this.tapOutTime = tapOutTime;
    /* serialize cards if tap out time of a ride of card is set up*/
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the tap in location of this ride.
   *
   * @return A String representing where this ride start.
   */
  public String getTapInLocation() {
    return this.tapInLocation;
  }

  /**
   * Get the tap out location of this ride.
   *
   * @return A String representing where this ride end.
   */
  public String getTapOutLocation() {
    return this.tapOutLocation;
  }

  /**
   * Set the tap out location of this ride.
   *
   * @param tapOutLocation A String representing where this ride end.
   */
  public void setTapOutLocation(String tapOutLocation) {
    this.tapOutLocation = tapOutLocation;
    /* serialize cards if tap out location of a ride of card is set up*/
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the number of stations this ride has go through.
   *
   * @return A double representing the total number of stations travelled of this ride.
   */
  public double getDistanceTravelled() {
    return this.distanceTravelled;
  }

  /**
   * Set the number of stations this ride has go through.
   *
   * @param distanceTravelled A double representing the total number of stations travelled.
   */
  public void setDistanceTravelled(double distanceTravelled) {
    BigDecimal bigDecimal = new BigDecimal(Double.valueOf(distanceTravelled).toString());
    bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    distanceTravelled = bigDecimal.doubleValue();
    this.distanceTravelled = distanceTravelled;
    /* serialize cards if distance travelled of a ride of card is set up*/
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the fare need to charge for this ride.
   *
   * @return A double representing the fare of this ride.
   */
  public double getFare() {
    return this.fare;
  }

  /**
   * Set the fare need to charge for this ride.
   *
   * @param fare A double representing the fare of this ride.
   */
  public void setFare(double fare) {
    /* round to 2 decimal point if there are more than 2 decimal point */
    BigDecimal bigDecimal = new BigDecimal(Double.valueOf(fare).toString());
    bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    this.fare = bigDecimal.doubleValue();
    /* serialize cards if fare of a ride of card is set up*/
    SerializeManager.getInstance().writeObject();
  }

  public LinkedList<Vertex> getPath() {
    return path;
  }

  /**
   * Set the path of this ride.
   *
   * @param path A LinkedList of Vertex representing the path.
   */
  public void setPath(LinkedList<Vertex> path) {
    this.path = path;
    /* serialize cards if path of a ride of card is set up*/
    SerializeManager.getInstance().writeObject();
  }

  public TransitType getTransitType() {
    return this.transitType;
  }

  /**
   * Get the information of this ride.
   *
   * @return A String representing all fields of this ride.
   */
  @Override
  public String toString() {
    String tapInInfo = "Transit Type: " + transitType + System.lineSeparator();
    tapInInfo += "Tap In At " + tapInLocation;
    tapInInfo += timeToString(tapInTime);
    String tapOutInfo = "Tap Out At " + tapOutLocation;
    tapOutInfo += timeToString(tapOutTime);
    tapOutInfo +=
        "Distance Travelled: "
            + distanceTravelled
            + " unit"
            + System.lineSeparator()
            + "Fare Charged: "
            + fare
            + System.lineSeparator()
            + "Path: "
            + path;

    return "--------------------------------------"
        + System.lineSeparator()
        + tapInInfo
        + tapOutInfo
        + System.lineSeparator()
        + "--------------------------------------";
  }

  /**
   * Get a string representation of this time.
   *
   * @param time time to translate to string
   * @return a sting representation of this time.
   */
  public String timeToString(Calendar time) {
    String result = "";
    if (time == null) {
      result += " [N/A] " + System.lineSeparator();
    } else {
      int year = time.get(Calendar.YEAR);
      int month = time.get(Calendar.MONTH) + 1;
      int date = time.get(Calendar.DATE);
      int hour = time.get(Calendar.HOUR_OF_DAY);
      int minute = time.get(Calendar.MINUTE);
      int second = time.get(Calendar.SECOND);
      result +=
          " ["
              + year
              + "-"
              + month
              + "-"
              + date
              + " "
              + hour
              + ":"
              + minute
              + ":"
              + second
              + "]"
              + System.lineSeparator();
    }
    return result;
  }

  /** a transit type in this transit system can be bus or subway. */
  public enum TransitType {
    BUS,
    SUBWAY
  }
}
