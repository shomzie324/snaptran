package ride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A RideRecord is a container with all ride records of this card. Current records are ride records
 * within reach cap time.
 */
public class RideRecord implements Serializable {

  private final ArrayList<Ride> contents; // all ride records of this card
  private double reachCapTime;

  /** Construct a new RideRecord. */
  public RideRecord() {
    /* create a new list to store all ride records of this card in order */
    this.contents = new ArrayList<>();
    /* set reach cap time to 2 hour by default, can be changed by admin user */
    /* in this transit system */
    this.reachCapTime = 2 * 3600 * 1000;
  }

  /**
   * Add a ride in this ride record.
   *
   * @param item A new ride.
   */
  public void add(Ride item) {
    contents.add(item);
  }

  /**
   * Set the reach cap time.
   *
   * @param reachCapTime the reach cap time of this transit system.
   */
  public void setReachCapTime(double reachCapTime) {
    this.reachCapTime = reachCapTime;
  }

  /**
   * Get the rides within reach cap time.
   *
   * @return An ArrayList of Ride including all rides not exceed reach cap time.
   */
  public ArrayList<Ride> getCurrentRides() {
    ArrayList<Ride> currentContent = new ArrayList<>();
    Ride latestRide = getLatestRide();
    if (latestRide != null) { // there are rides within reach cap time
      Calendar latestTapInTime = latestRide.getTapInTime();
      /* if the latest ride has missed tap in, it will not be capped as a punishment. */
      if (latestTapInTime != null) {
        for (int i = contents.size() - 1; i >= 0; i--) { // check from latest to previous ride
          if (contents.get(i).getTapInTime() == null) { // this ride has missed tap in time
            if (rideMissedTapInTimeWithinCapTime(i, latestTapInTime)) {
              currentContent.add(0, contents.get(i));
            }
          } else if (withinCapTime(contents.get(i).getTapInTime(), latestTapInTime)) {
            currentContent.add(0, contents.get(i));
          }
        }
      }
    }
    return currentContent;
  }

  /**
   * Check whether previous ride of this ride with missed tap in time is within cap time or not. If
   * its previous ride is within reach cap time, then this ride with missed tap in time is also
   * within cap time. Consider this ride within missed tap in time without reach cap time if there
   * is no previous ride to compare.
   *
   * @param indexOfRideWithMissedTapInTime index in of this ride with missed tap in time in all
   *     rides
   * @param latestTapInTime the latest tap in time of this card
   * @return whether this ride with missed tap in time is within cap time.
   */
  private boolean rideMissedTapInTimeWithinCapTime(
      int indexOfRideWithMissedTapInTime, Calendar latestTapInTime) {
    int indexOfThisRide = indexOfRideWithMissedTapInTime;
    if (indexOfThisRide > 0) {
      while (contents.get(indexOfThisRide - 1).getTapInTime() == null && indexOfThisRide - 1 >= 0) {
        indexOfThisRide--;
      }
      /* if its previous ride is within cap time, */
      /*this ride with missed tap in time is also within cap time. */
      return indexOfThisRide - 1 >= 0 // find a previous ride that has tap in time
          && withinCapTime(
              contents.get(indexOfThisRide - 1).getTapInTime(),
              latestTapInTime); // previous ride is within cap time
    }
    /* consider this ride with missed tap in without cap time */
    /* if there is no previous ride to compare */
    return false;
  }

  /**
   * Check whether this ride is within cap time.
   *
   * @param thisTapInTime tap in time of this ride
   * @param latestTapInTime tap in time of the latest ride of this card
   * @return true if this ride is within cap time, false otherwise.
   */
  private boolean withinCapTime(Calendar thisTapInTime, Calendar latestTapInTime) {
    return latestTapInTime.getTimeInMillis() - thisTapInTime.getTimeInMillis() <= reachCapTime;
  }

  /**
   * Get all rides of this ride record.
   *
   * @return An ArrayList of Ride including all rides in this ride record.
   */
  public ArrayList<Ride> getAllRides() {
    return contents;
  }

  /**
   * Get the most recent ride of this ride record.
   *
   * @return A ride which is the latest ride.
   */
  public Ride getLatestRide() {
    ArrayList<Ride> allContents = this.getAllRides();
    if (!allContents.isEmpty()) {
      return allContents.get(allContents.size() - 1);
    } else {
      return null;
    }
  }

  /**
   * Create a new daily iterator for this ride record.
   *
   * @return A new DailyIterator.
   */
  public RideIterator dailyIterator() {
    return new DailyIterator();
  }

  /**
   * Create a new weekly iterator for this ride record.
   *
   * @return A new WeeklyIterator.
   */
  public RideIterator weeklyIterator() {
    return new WeeklyIterator();
  }

  /**
   * Create a new monthly iterator for this ride record.
   *
   * @return A new MonthlyIterator.
   */
  public RideIterator monthlyIterator() {
    return new MonthlyIterator();
  }

  /**
   * Get a ride iterator according to time period.
   *
   * @param reportPeriod A string representing time period
   * @return A ride iterator based on reportPeriod.
   */
  public RideRecord.RideIterator getRideIteratorByReportPeriod(String reportPeriod) {
    switch (reportPeriod) {
      case "DAY":
        return dailyIterator();
      case "WEEK":
        return weeklyIterator();
      case "MONTH":
        return monthlyIterator();
      default:
        break;
    }
    return null;
  }

  /**
   * Class RideIterator store the next batch of rides and can iterate over ride records of this
   * card. A subclass of RideIterator can have different ways to iterate.
   */
  public abstract class RideIterator {

    ArrayList<Ride> next; // next batch of ride records
    int indexToStartLoad;

    /** Create a new RideIterator. load a batch of ride records and store them as nex batch. */
    RideIterator() {
      indexToStartLoad = contents.size() - 1;
      load(); // load a batch of ride records
    }

    /**
     * Whether there is a next batch of ride records to iterate or not.
     *
     * @return true if there is a next batch of ride records, false otherwise.
     */
    public boolean hasNext() {
      return next.size() != 0;
    }

    /**
     * Get the next batch of ride records.
     *
     * @return next batch of ride records.
     */
    public ArrayList<Ride> next() {
      ArrayList<Ride> result = next; // store the next batch of ride records
      load(); // load a new batch to store as next batch of ride records
      return result;
    }

    /** Load the next batch of ride records. */
    abstract void load();
  }

  /**
   * A daily iterator can iterate over all ride records by day. i.e. a batch of ride records will
   * includes all ride records of a day.
   */
  class DailyIterator extends RideIterator {

    /**
     * Load ride records of the latest previous day and store them as next batch of daily ride
     * records.
     */
    @Override
    void load() {
      if (indexToStartLoad >= 0) {
        super.next = new ArrayList<>();
        int thisYear = contents.get(indexToStartLoad).getTapInTime().get(Calendar.YEAR);
        int thisMonth = contents.get(indexToStartLoad).getTapInTime().get(Calendar.MONTH);
        int thisDay = contents.get(indexToStartLoad).getTapInTime().get(Calendar.DATE);
        while (indexToStartLoad >= 0) {
          Ride thisRide = contents.get(indexToStartLoad);
          int yearOfThisRide;
          int monthOfThisRide;
          int dayOfThisRide;
          if (thisRide.getTapInTime() == null) {
            yearOfThisRide = thisRide.getTapOutTime().get(Calendar.YEAR);
            monthOfThisRide = thisRide.getTapOutTime().get(Calendar.MONTH);
            dayOfThisRide = thisRide.getTapOutTime().get(Calendar.DATE);
          } else {
            yearOfThisRide = thisRide.getTapInTime().get(Calendar.YEAR);
            monthOfThisRide = thisRide.getTapInTime().get(Calendar.MONTH);
            dayOfThisRide = thisRide.getTapInTime().get(Calendar.DATE);
          }
          if (thisYear == yearOfThisRide
              && thisMonth == monthOfThisRide
              && thisDay == dayOfThisRide) {
            super.next.add(0, contents.get(indexToStartLoad));
            indexToStartLoad--;
          } else {
            break;
          }
        }
      } else { // no ride record to load
        super.next = new ArrayList<>();
      }
    }
  }

  /**
   * A weekly iterator can iterate all ride records in this card by week. i.e. a batch of ride
   * record will includes all ride records of a week.
   */
  class WeeklyIterator extends RideIterator {

    /**
     * Load ride records of the latest previous week and store them as next batch of weekly ride
     * records.
     */
    @Override
    void load() {
      if (indexToStartLoad >= 0) {
        super.next = new ArrayList<>();
        int thisYear = contents.get(indexToStartLoad).getTapInTime().get(Calendar.YEAR);
        int thisMonth = contents.get(indexToStartLoad).getTapInTime().get(Calendar.MONTH);
        int thisWeek = contents.get(indexToStartLoad).getTapInTime().get(Calendar.WEEK_OF_MONTH);
        while (indexToStartLoad >= 0) {
          Ride thisRide = contents.get(indexToStartLoad);
          int yearOfThisRide;
          int monthOfThisRide;
          int weekOfThisRide;
          if (thisRide.getTapInTime() == null) {
            yearOfThisRide = thisRide.getTapOutTime().get(Calendar.YEAR);
            monthOfThisRide = thisRide.getTapOutTime().get(Calendar.MONTH);
            weekOfThisRide = thisRide.getTapOutTime().get(Calendar.WEEK_OF_MONTH);
          } else {
            yearOfThisRide = thisRide.getTapInTime().get(Calendar.YEAR);
            monthOfThisRide = thisRide.getTapInTime().get(Calendar.MONTH);
            weekOfThisRide = thisRide.getTapInTime().get(Calendar.WEEK_OF_MONTH);
          }
          if (thisYear == yearOfThisRide
              && thisMonth == monthOfThisRide
              && thisWeek == weekOfThisRide) {
            super.next.add(0, contents.get(indexToStartLoad));
          } else {
            break;
          }
          indexToStartLoad--;
        }
      } else { // no ride record to load
        super.next = new ArrayList<>();
      }
    }
  }

  /**
   * A monthly iterator can iterate all ride records in this card by month. i.e. a batch of ride
   * record will includes all ride records of a month.
   */
  class MonthlyIterator extends RideIterator {

    /**
     * Load ride records of the latest previous month and store them as next batch of monthly ride
     * records.
     */
    @Override
    void load() {
      if (indexToStartLoad >= 0) {
        super.next = new ArrayList<>();
        int thisYear = contents.get(indexToStartLoad).getTapInTime().get(Calendar.YEAR);
        int thisMonth = contents.get(indexToStartLoad).getTapInTime().get(Calendar.MONTH);
        while (indexToStartLoad >= 0) {
          Ride thisRide = contents.get(indexToStartLoad);
          int yearOfThisRide;
          int monthOfThisRide;
          if (thisRide.getTapInTime() == null) {
            yearOfThisRide = thisRide.getTapOutTime().get(Calendar.YEAR);
            monthOfThisRide = thisRide.getTapOutTime().get(Calendar.MONTH);
          } else {
            yearOfThisRide = thisRide.getTapInTime().get(Calendar.YEAR);
            monthOfThisRide = thisRide.getTapInTime().get(Calendar.MONTH);
          }
          if (thisYear == yearOfThisRide && thisMonth == monthOfThisRide) {
            super.next.add(0, contents.get(indexToStartLoad));
          } else {
            break;
          }
          indexToStartLoad--;
        }
      } else { // no ride record to load
        super.next = new ArrayList<>();
      }
    }
  }
}
