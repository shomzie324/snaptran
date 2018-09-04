package map;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javafx.util.Pair;
import ride.Ride;

/**
 * A farthest path calculator can calculate the farthest path and farthest distance from one vertex
 * to the farthest end on one route.
 */
class FarthestPathCalculator implements Serializable {

  private final SystemMap systemMap; // the system map used to calculate farthest path

  /**
   * Create a new farthest path calculator.
   *
   * @param systemMap system map used to calculate farthest path.
   */
  private FarthestPathCalculator(SystemMap systemMap) {
    this.systemMap = systemMap;
  }

  /**
   * Get an instance of Farthest Path Calculator.
   *
   * @param systemMap system map used to calculate farthest path.
   * @return an instance of FarthestPathCalculator.
   */
  public static FarthestPathCalculator getInstance(SystemMap systemMap) {
    return new FarthestPathCalculator(systemMap);
  }

  /**
   * Get the path from this vertex to the farthest end of the route that contains this vertex.
   *
   * @param vertexName name of this vertex(station/stop)
   * @param ride this ride
   * @return the path from this vertex to the farthest end of the route that contains this vertex.
   */
  LinkedList<Vertex> getPathToFarthestEnd(String vertexName, Ride ride) {
    /* get bus routes if this ride is a bus ride, get subway routes */
    /* if this ride is a subway ride. */
    Collection<Pair<String, ArrayList<String>>> routes =
        getRoutesByTransitType(ride.getTransitType());
    /* get a list of vertices on the path from this vertex to the farthest end. */
    List<Vertex> vertexToFarthestEnd = findVertexToFarthestEnd(routes, vertexName, ride);
    LinkedList<Vertex> result = new LinkedList<>();
    /* create a path according to this list of vertices. */
    for (Vertex vertex : vertexToFarthestEnd) {
      result.addLast(vertex); // add to end each time, keep order of stations/stops in this path.
    }
    return result;
  }

  /**
   * Get bus routes for bus transit and subway routes for subway transit.
   *
   * @param transitType bus or subway transit
   * @return routes for this transit type.
   */
  private Collection<Pair<String, ArrayList<String>>> getRoutesByTransitType(
      Ride.TransitType transitType) {
    Collection<Pair<String, ArrayList<String>>> routes;
    if (transitType == Ride.TransitType.BUS) {
      routes = systemMap.getBusRoutes().values();
    } else {
      routes = systemMap.getSubwayRoutes().values();
    }
    return routes;
  }

  /**
   * Get a list of vertices on the path from this vertex to the farthest end on this route.
   *
   * @param routes a string indicates whether this route has both way or only one way, and a list of
   *     string with information of all adjacent vertices on this route, including vertex name and
   *     edge distance.
   * @param vertexName name of this vertex
   * @param ride this ride
   * @return a list of vertices on the path from this vertex to the farthest end on this route.
   */
  private List<Vertex> findVertexToFarthestEnd(
      Collection<Pair<String, ArrayList<String>>> routes, String vertexName, Ride ride) {
    Vertex vertex = systemMap.getVertex(vertexName);
    List<Vertex> result = new ArrayList<>();
    double distance = 0;
    for (Pair<String, ArrayList<String>> route : routes) {
      ArrayList<Vertex> verticesList = getVerticesList(route.getValue());
      if (verticesList.contains(vertex)) { // if this route contains this vertex
        List<Vertex> temp = new ArrayList<>();
        double thisDistance;
        if (route.getKey().equals("ONE WAY")) {
          temp = getVerticesToFarthestEndInOneWay(ride, verticesList, vertex);
        }
        if (route.getKey().equals("BOTH WAY")) {
          temp = getVerticesToFarthestEndInBothWay(ride, verticesList, vertex);
        }
        /* record the distance from this vertex to farthest end in last vertex on this path */
        thisDistance = temp.get(temp.size() - 1).getDistance();
        /* replace result if distance to farthest end on this route is larger */
        if (thisDistance > distance) {
          result = temp;
          distance = thisDistance;
          BigDecimal bigDecimal = new BigDecimal(Double.valueOf(distance).toString());
          bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
          distance = bigDecimal.doubleValue();
          result.get(result.size() - 1).setDistance(distance);
        }
      }
    }
    return result;
  }

  /**
   * Get a list of vertex from string information of all adjacent vertices on a route.
   *
   * @param allAdjacentVertices A string representation of allAdjacentVertices information of all
   *     adjacent vertices on a route, including name of vertices and edge distance between two
   *     adjacent vertices.
   * @return a list of vertex of all vertices on this route.
   */
  private ArrayList<Vertex> getVerticesList(ArrayList<String> allAdjacentVertices) {
    ArrayList<Vertex> result = new ArrayList<>();
    for (String neighbor : allAdjacentVertices) {
      String vertexName = neighbor.split("->")[0];
      result.add(systemMap.getVertex(vertexName));
    }
    result.add(
        systemMap.getVertex(
            allAdjacentVertices.get(allAdjacentVertices.size() - 1).split("->")[2]));
    return result;
  }

  /**
   * Get list of vertices to the end on a route with only one way.
   *
   * @param ride this ride
   * @param verticesList a list of all vertices on a route
   * @param vertex this vertex
   * @return get list of vertices to the end on a route.
   */
  private List<Vertex> getVerticesToFarthestEndInOneWay(
      Ride ride, List<Vertex> verticesList, Vertex vertex) {
    List<Vertex> temp;
    if (ride.getTapInLocation().equals("(Missed Tap In)")) { // this ride has missed tap in
      /* consider start of this route as tap in location */
      temp = verticesList.subList(0, verticesList.indexOf(vertex) + 1);
    } else { // this ride has missed tap out
      /* consider end of this route as tap out location */
      temp = verticesList.subList(verticesList.indexOf(vertex), verticesList.size());
    }
    temp.get(temp.size() - 1).setDistance(getDistanceBetweenTwoVertices(temp));
    return temp;
  }

  /**
   * Get a list of vertices to the farthest end on a route with both way.
   *
   * @param ride this ride
   * @param verticesList a list of vertices on a route
   * @param vertex this vertex
   * @return a list of vertices to the farthest end on a route with both way.
   */
  private List<Vertex> getVerticesToFarthestEndInBothWay(
      Ride ride, List<Vertex> verticesList, Vertex vertex) {
    double result;
    List<Vertex> temp;
    /* list of vertices from one end to this vertex */
    List<Vertex> result1 = verticesList.subList(0, verticesList.indexOf(vertex) + 1);
    /* distance from this end to this vertex*/
    double distance1 = getDistanceBetweenTwoVertices(result1);
    /* list of vertices from the other end to this vertex */
    List<Vertex> result2 = verticesList.subList(verticesList.indexOf(vertex), verticesList.size());
    /* distance from this end to this vertex*/
    double distance2 = getDistanceBetweenTwoVertices(result2);
    /*compare the distance from this vertex to both end, set result to the farthest distance */
    if (distance1 >= distance2) {
      temp = result1;
      result = distance1;
      if (ride.getTapOutLocation().equals("(Missed Tap Out)")) {
        temp = getReverseOrderList(temp);
      }
    } else {
      temp = result2;
      result = distance2;
      if (ride.getTapInLocation().equals("(Missed Tap In)")) {
        temp = getReverseOrderList(temp);
      }
    }
    temp.get(temp.size() - 1).setDistance(result);
    return temp;
  }

  /**
   * Get the total distance from the first vertex in this list to the last vertex in this list.
   *
   * @param vertices all vertices between the start vertex(included) to the end vertex(included).
   * @return the total distance from the first vertex in this list to the last vertex in this list.
   */
  private double getDistanceBetweenTwoVertices(List<Vertex> vertices) {
    double result = 0;
    for (int i = 0; i + 1 <= vertices.size() - 1; i++) {
      /* add edge distance between two adjacent vertices to total distance */
      result += vertices.get(i).getAdjacentVertices().get(vertices.get(i + 1));
    }
    return result;
  }

  /**
   * Get a list of vertices in reverse order of this list of vertices.
   *
   * @param listToReverseOrder list of vertices to reverse order
   * @return a list of vertices in reverse order of this list of vertices.
   */
  private List<Vertex> getReverseOrderList(List<Vertex> listToReverseOrder) {
    List<Vertex> result = new ArrayList<>();
    for (Vertex vertex : listToReverseOrder) { // loop from front to back
      result.add(0, vertex); // add from back to front
    }
    return result;
  }
}
