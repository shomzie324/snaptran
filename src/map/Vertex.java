package map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import javafx.util.Pair;
import log.LogManager;
import serialize.SerializeManager;

/**
 * A vertex is a station/stop in this transit system. A vertex store its name as value, its adjacent
 * vertex and edge distance between them. A vertex collects statistics including the total times
 * been tapped in at, the total times been tapped out at, the total times all cardholders arrived
 * at. A vertex record information of previous vertex, distance when the shortest path calculator is
 * doing a shortest path calculation.
 */
public class Vertex extends Observable implements Serializable {

  private final String value;
  private final HashMap<Vertex, Double> adjacentVertices;
  /* store tap in times, tap out times, arrived times by day */
  private final Map<Long, Map<String, Integer>> passengerFlow;
  private Vertex prev;
  private double distance;
  private Pair<Integer, Integer> coordinate;

  /**
   * Construct a new Vertex.
   *
   * @param value the value/name for this Vertex.
   */
  Vertex(String value) {
    this.distance = Double.POSITIVE_INFINITY;
    this.value = value;
    this.adjacentVertices = new HashMap<>();
    this.passengerFlow = new HashMap<>();
    this.addObserver(LogManager.getInstance());
    this.setChanged();
    this.notifyObservers("New Station/Stop Added: " + value + "!");
    /* serialize system map if a new vertex is initialized */
    SerializeManager.getInstance().writeObject();
  }

  public Pair<Integer, Integer> getCoordinate() {
    return this.coordinate;
  }

  void setCoordinate(Pair<Integer, Integer> coordinate) {
    this.coordinate = coordinate;
    setChanged();
    notifyObservers("Coordinate Of " + value + " is set to:" + coordinate + "!");
    /* serialize system map if coordinate of this vertex is set up */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the previous Vertex.
   *
   * @return A Vertex.
   */
  Vertex getPrev() {
    return this.prev;
  }

  /**
   * Set the previous Vertex.
   *
   * @param prev A Vertex.
   */
  void setPrev(Vertex prev) {
    this.prev = prev;
    setChanged();
    notifyObservers("Previous Vertex Of " + value + " is set to:" + prev + "!");
    /* serialize system map if previous vertex of this vertex is set up */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the distance of this Vertex.
   *
   * @return A double representing the distance.
   */
  public double getDistance() {
    return this.distance;
  }

  /**
   * Set the distance of this Vertex.
   *
   * @param distance A double representing the shortest distance of this Vertex to its neighbor.
   */
  void setDistance(double distance) {
    this.distance = distance;
    setChanged();
    notifyObservers("Distance of " + value + " is set to:" + distance + "!");
    /* serialize system map if distance of a vertex is set up */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the name/value of this Vertex.
   *
   * @return A String representing the name/value.
   */
  public String getValue() {
    return value;
  }

  /**
   * Get the adjacent vertices of this vertex.
   *
   * @return the adjacent vertices of this vertex.
   */
  HashMap<Vertex, Double> getAdjacentVertices() {
    return adjacentVertices;
  }

  /**
   * Add a new neighbor Vertex to adjacent vertices.
   *
   * @param edge A double representing the distance.
   * @param neighbor A Vertex which is next to this Vertex.
   */
  void addAdjacentVertex(Vertex neighbor, Double edge) {
    adjacentVertices.put(neighbor, edge);
  }

  /**
   * Get the passenger flow according to report period and report content.
   *
   * @param reportPeriod report period, DAY or WEEK or MONTH
   * @param reportContent report content, TAP IN TIMES or TAP OUT TIMES or ARRIVED TIMES
   * @return passenger flow according to report period and report content.
   */
  public int getPassengerFlow(String reportPeriod, String reportContent) {
    int result = 0;
    List<Map<String, Integer>> flowForPeriod = getPassengerFlowForPeriod(reportPeriod);
    for (Map<String, Integer> flow : flowForPeriod) {
      result += flow.get(reportContent);
    }
    return result;
  }

  /**
   * Get passenger flow statistics according to report period.
   *
   * @param reportPeriod report period, , DAY or WEEK or MONTH
   * @return list of passenger flow statistics according to report period.
   */
  private List<Map<String, Integer>> getPassengerFlowForPeriod(String reportPeriod) {
    List<Map<String, Integer>> result = new ArrayList<>();
    long keyOfToday = getKey(System.currentTimeMillis());
    Calendar today = Calendar.getInstance();
    today.setTimeInMillis(keyOfToday);
    switch (reportPeriod) {
      case "DAY":
        result.add(passengerFlow.get(keyOfToday));
        break;
      case "WEEK":
        result = getWeeklyPassengerFlow(today);
        break;
      case "MONTH":
        result = getMonthlyPassengerFlow(today);
        break;
      default:
        break;
    }
    return result;
  }

  /**
   * Get passenger flow in this week.
   *
   * @param today a Calendar represents today
   * @return list of passenger flow statistics in this week.
   */
  private List<Map<String, Integer>> getWeeklyPassengerFlow(Calendar today) {
    List<Map<String, Integer>> result = new ArrayList<>();
    for (Long dayInMillis : passengerFlow.keySet()) {
      Calendar day = Calendar.getInstance();
      day.setTimeInMillis(dayInMillis);
      if (day.get(Calendar.YEAR) == today.get(Calendar.YEAR)
          && day.get(Calendar.MONTH) == today.get(Calendar.MONTH)
          && day.get(Calendar.WEEK_OF_MONTH) == today.get(Calendar.WEEK_OF_MONTH)) {
        result.add(passengerFlow.get(dayInMillis));
      }
    }
    return result;
  }

  /**
   * Get passenger flow in this month.
   *
   * @param today a Calendar represents today
   * @return list of passenger flow statics in this month
   */
  private List<Map<String, Integer>> getMonthlyPassengerFlow(Calendar today) {
    List<Map<String, Integer>> result = new ArrayList<>();
    for (Long dayInMillis : passengerFlow.keySet()) {
      Calendar day = Calendar.getInstance();
      day.setTimeInMillis(dayInMillis);
      if (day.get(Calendar.YEAR) == today.get(Calendar.YEAR)
          && day.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
        result.add(passengerFlow.get(dayInMillis));
      }
    }
    return result;
  }

  /**
   * Get the key value in passenger flow map that represent this day.
   *
   * @param dayInMillis this day represented in millis
   * @return the key in passenger flow map that represent this day.
   */
  private Long getKey(long dayInMillis) {
    Calendar day = Calendar.getInstance();
    day.setTimeInMillis(dayInMillis);
    for (long key : passengerFlow.keySet()) {
      Calendar keyDay = Calendar.getInstance();
      keyDay.setTimeInMillis(key);
      if (day.get(Calendar.YEAR) == keyDay.get(Calendar.YEAR)
          && day.get(Calendar.MONTH) == keyDay.get(Calendar.MONTH)
          && day.get(Calendar.DAY_OF_MONTH) == keyDay.get(Calendar.DAY_OF_MONTH)) {
        return key;
      }
    }
    return initializeFlowForNewDay();
  }

  /**
   * Initialize the passenger flow statistics for a new day.
   *
   * @return the key of passenger flow statistics for this new day.
   */
  private long initializeFlowForNewDay() {
    Map<String, Integer> flow = new HashMap<>();
    flow.put("TAP IN TIMES", 0);
    flow.put("TAP OUT TIMES", 0);
    flow.put("ARRIVED TIMES", 0);
    long day = System.currentTimeMillis();
    passengerFlow.put(day, flow);
    setChanged();
    notifyObservers("Passenger of " + value + " is initialized:" + "!");
    /* serialize system map if passenger flow of a vertex is initialized*/
    SerializeManager.getInstance().writeObject();
    return day;
  }

  /**
   * Add to passenger flow statistics of this vertex by report day and report content.
   *
   * @param dayInMillis a long representation of the day that the passenger flow is added
   * @param reportContent report content, "TAP IN TIMES" or "TAP OUT TIMES" or "ARRIVED TIMES"
   */
  public void addPassengerFlow(Long dayInMillis, String reportContent) {
    Long day = getKey(dayInMillis);
    passengerFlow.get(day).put(reportContent, passengerFlow.get(day).get(reportContent) + 1);
    this.setChanged();
    this.notifyObservers(value + " " + reportContent + " Has Been Added By One!");
    /* serialize system map if passenger flow of a vertex is added*/
    SerializeManager.getInstance().writeObject();
  }

  /** Clear the Vertex information of prev and (shortest) distance. */
  void clear() {
    prev = null;
    distance = Double.POSITIVE_INFINITY;
  }

  /**
   * Get the information of this Vertex.
   *
   * @return A String representing the name/value of this Vertex.
   */
  @Override
  public String toString() {
    return this.value;
  }
}
