package map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import javafx.util.Pair;
import log.LogManager;
import ride.Ride;
import serialize.SerializeManager;

/**
 * A system map store bus routes and subway routes in this transit system separately. A system map
 * also has a graph with all stations and stops in this transit system.
 */
public class SystemMap extends Observable implements Serializable {

  /* busRoutes and subwayRoutes use route name as key, use pair of string indicates
   * whether the route is both way or one way and the route as value, use a list of
   * vertex names to represent a route.*/
  private final HashMap<String, Pair<String, ArrayList<String>>> busRoutes;
  private final HashMap<String, Pair<String, ArrayList<String>>> subwayRoutes;
  private final Set<map.Vertex> graph;
  private final FarthestPathCalculator farthestPathCalculator;
  private final ShortestPathCalculator shortestPathCalculator;

  /** Create a new system map. */
  public SystemMap() {
    /* create a new map to store bus route name and stops correspondingly*/
    this.busRoutes = new HashMap<>();
    /* create a new map to store subway route name and stations correspondingly*/
    this.subwayRoutes = new HashMap<>();
    /* create a new set to store all stations/stops*/
    this.graph = new HashSet<>();
    this.farthestPathCalculator = FarthestPathCalculator.getInstance(this);
    this.shortestPathCalculator = ShortestPathCalculator.getInstance(this);
    this.addObserver(LogManager.getInstance());
    setChanged();
    notifyObservers("System Map Initialized! ");
  }

  /**
   * Add a new route to this system map.
   *
   * @param transitType whether this route is bus route or subway route.
   * @param direction whether this route is both way or one way.
   * @param routeName the name for this route.
   * @param route A string representing the route.
   */
  public void addNewRoute(
      Ride.TransitType transitType,
      String direction,
      String routeName,
      List<Pair<Integer, Integer>> coordinates,
      String route) {
    ArrayList<String> allAdjacentVertices = new ArrayList<>(Arrays.asList(route.split(" \\| ")));
    storeAllAdjacentVertices(transitType, direction, routeName, allAdjacentVertices);
    for (int i = 0; i <= allAdjacentVertices.size() - 1; i++) {
      String[] vertices = allAdjacentVertices.get(i).split("->");
      Vertex vertex1 = getVertex(vertices[0]);
      vertex1.setCoordinate(coordinates.get(i));
      Vertex vertex2 = getVertex(vertices[2]);
      vertex2.setCoordinate(coordinates.get(i + 1));
      double edge = Double.valueOf(vertices[1]);
      /* add vertex2 as the neighbor of vertex1 and record their edge distance. */
      vertex1.addAdjacentVertex(vertex2, edge);
      if (direction.equals("BOTH WAY")) {
        /* add vertex1 as the neighbor of vertex2 as well if this route has 2 directions. */
        vertex2.addAdjacentVertex(vertex1, edge);
      }
    }
    notifyChange(transitType, direction, routeName, route);
    /* serialize system map if new route is added */
    SerializeManager.getInstance().writeObject();
  }

  private void notifyChange(
      Ride.TransitType transitType, String direction, String routeName, String route) {
    setChanged();
    notifyObservers(
        "New "
            + transitType
            + " Route: "
            + routeName
            + " is Added!"
            + System.lineSeparator()
            + route
            + System.lineSeparator()
            + " Direction:"
            + direction);
  }

  /**
   * Store information of all adjacent vertices of this route, including stations/stops names,
   * distance between adjacent stations/stops, route name and direction(i.e. this route has both
   * come and back or only one way).
   *
   * @param transitType whether this route is for bus transit or subway transit.
   * @param direction whether this route has both come and back or only one way.
   * @param routeName name of this route
   * @param allAdjacentVertices information of all adjacent stations/stops, including their names
   *     and edge distance.
   */
  private void storeAllAdjacentVertices(
      Ride.TransitType transitType,
      String direction,
      String routeName,
      ArrayList<String> allAdjacentVertices) {
    if (transitType == Ride.TransitType.BUS) {
      busRoutes.put(routeName, new Pair<>(direction, allAdjacentVertices));
    }
    if (transitType == Ride.TransitType.SUBWAY) {
      subwayRoutes.put(routeName, new Pair<>(direction, allAdjacentVertices));
    }
  }

  /**
   * Get the Vertex corresponding to its name.
   *
   * @param vertexName the name of this vertex.
   */
  public map.Vertex getVertex(String vertexName) {
    /* check if this station/stop name already exists in this system map */
    for (map.Vertex vertex : graph) {
      if (vertex.getValue().equals(vertexName)) { // this station/stop name already exists
        return vertex;
      }
    }
    /* this station/stop name is new */
    map.Vertex newVertex = new map.Vertex(vertexName);
    graph.add(newVertex); // store this new station/stop in graph
    return newVertex;
  }

  /**
   * Clear information stored in vertices from last calculation for all vertices in graph of this
   * system map.
   */
  void clear() {
    for (map.Vertex vertex : graph) {
      vertex.clear();
    }
    setChanged();
    notifyObservers("Expired Data Cleared!");
    /* serialize system map if previous statistics is cleared */
    SerializeManager.getInstance().writeObject();
  }

  /**
   * Get the shortest path of this ride.
   *
   * @param startVertexName the name of start point Vertex.
   * @param endVertexName the name of end point Vertex.
   * @return A LinkedList of Vertex representing the shortest path.
   */
  public LinkedList<map.Vertex> getShortestPath(String startVertexName, String endVertexName) {
    /* clear information stored in stations/stops from last calculation to start new calculation */
    clear();
    return shortestPathCalculator.getShortestPath(
        getVertex(startVertexName), getVertex(endVertexName));
  }

  /**
   * Get the path from this vertex to the farthest end of the route that contains this vertex.
   *
   * @param vertexName name of this vertex(station/stop)
   * @param ride this ride
   * @return the path from this vertex to the farthest end of the route that contains this vertex.
   */
  public LinkedList<Vertex> getPathToFarthestEnd(String vertexName, Ride ride) {
    /* clear information stored in stations/stops from last calculation to start new calculation */
    clear();
    return farthestPathCalculator.getPathToFarthestEnd(vertexName, ride);
  }

  /**
   * Get all bus routes in this system map.
   *
   * @return all bus routes in this system map.
   */
  public HashMap<String, Pair<String, ArrayList<String>>> getBusRoutes() {
    return this.busRoutes;
  }

  /**
   * Get all subway routes in this system map.
   *
   * @return all subway routes in this system map.
   */
  public HashMap<String, Pair<String, ArrayList<String>>> getSubwayRoutes() {
    return this.subwayRoutes;
  }

  /** Get a set of all vertices in this system map. */
  public Set<Vertex> getGraph() {
    return this.graph;
  }

  /**
   * Remove a route from this system map.
   *
   * @param routeName - the name of the route to remove.
   */
  public void removeRoute(String routeName) {
    if (subwayRoutes.containsKey(routeName)) {
      ArrayList<Vertex> vertices = getVerticesList(subwayRoutes.get(routeName).getValue());
      graph.removeAll(vertices);
      subwayRoutes.remove(routeName);
      setChanged();
      notifyObservers("Route " + routeName + " deleted!");
    } else if (busRoutes.containsKey(routeName)) {
      ArrayList<Vertex> vertices = getVerticesList(busRoutes.get(routeName).getValue());
      graph.removeAll(vertices);
      busRoutes.remove(routeName);
      setChanged();
      notifyObservers("Route " + routeName + " deleted!");
    }
    /* serialize system map if route is removed */
    SerializeManager.getInstance().writeObject();
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
      result.add(getVertex(vertexName));
    }
    result.add(getVertex(allAdjacentVertices.get(allAdjacentVertices.size() - 1).split("->")[2]));
    return result;
  }

  /**
   * Get a list of vertex by specific transit type.
   *
   * @param transitType the transit type of vertices.
   * @return A list of vertex of a specific transit type.
   */
  public List<Vertex> getVerticesByTransitType(Ride.TransitType transitType) {
    List<Vertex> result = new ArrayList<>();
    Collection<Pair<String, ArrayList<String>>> routes;
    if (transitType.equals(Ride.TransitType.BUS)) {
      routes = busRoutes.values();
    } else {
      routes = subwayRoutes.values();
    }
    for (Pair<String, ArrayList<String>> busRoute : routes) {
      result.addAll(getVerticesList(busRoute.getValue()));
    }
    return result;
  }
}
