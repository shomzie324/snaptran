package map;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A shortest path calculator can calculate the shortest path and shortest distance from one vertex
 * to another.
 */
class ShortestPathCalculator implements Serializable {

  private final SystemMap systemMap; // the system map used to calculate shortest path

  /**
   * Create a new shortest path calculator.
   *
   * @param systemMap system map used to calculate shortest path.
   */
  private ShortestPathCalculator(SystemMap systemMap) {
    this.systemMap = systemMap;
  }

  /**
   * Get an instance of Shortest Path Calculator.
   *
   * @param systemMap system map used to calculate shortest path.
   * @return an instance of shortest path calculator.
   */
  public static ShortestPathCalculator getInstance(SystemMap systemMap) {
    return new ShortestPathCalculator(systemMap);
  }

  /**
   * Get the shortest path from the start vertex to the end vertex.
   *
   * @param startVertex the start vertex.
   * @param endVertex the end vertex.
   * @return A LinkedList of Vertex representing the shortest path.
   */
  LinkedList<Vertex> getShortestPath(Vertex startVertex, Vertex endVertex) {
    /* clear information stored in stations/stops from last calculation to start new calculation */
    systemMap.clear();
    /* calculate shortest distance from each station/stop in graph to start station/stop */
    getShortestDistance(startVertex);
    map.Vertex currentVertex = endVertex;
    LinkedList<map.Vertex> path = new LinkedList<>();
    path.offerFirst(currentVertex);
    /* every vertex record its previous vertex, */
    /* prepend previous vertex of each vertex to this path */
    while (currentVertex.getPrev() != null) {
      path.offerFirst(currentVertex.getPrev());
      currentVertex = currentVertex.getPrev();
    }
    return path;
  }

  /**
   * Calculate the shortest distance from each vertex in graph to the source vertex. Store distance
   * from this vertex to the source vertex and its previous vertex for each vertex.
   *
   * @param source the source vertex to calculate distance from other vertices to.
   */
  private void getShortestDistance(Vertex source) {
    source.setDistance(0); // set distance from source to itself by 0
    Set<Vertex> discovered = new HashSet<>(); // vertices that has been discovered
    Set<map.Vertex> toDiscover = new HashSet<>(); // vertices to discover
    toDiscover.add(source);

    while (toDiscover.size() > 0) { // there is vertex has not been discovered
      map.Vertex currentVertex = extractMin(toDiscover);

      HashMap<Vertex, Double> adjacentVertices = currentVertex.getAdjacentVertices();
      for (map.Vertex neighbor :
          adjacentVertices.keySet()) { // discover all neighbors of current vertex
        double newDistance = currentVertex.getDistance() + adjacentVertices.get(neighbor);
        /* update shortest distance from this neighbor of current vertex to the source*/
        if (newDistance < neighbor.getDistance()) {
          /* record distance and previous vertex from */
          /* this neighbor of current vertex to the source*/
          neighbor.setDistance(newDistance);
          neighbor.setPrev(currentVertex);
          if (!discovered.contains(neighbor)) { // this neighbor has not been discovered
            toDiscover.add(neighbor);
          }
        }
      }
      discovered.add(currentVertex); // this current vertex has been discovered
      toDiscover.remove(
          currentVertex); // remove this current vertex from set of vertices to discover
    }
  }

  /**
   * Get the vertex with minimum distance to the source in set of vertices to discover.
   *
   * @param toDiscover A Set of Vertex to be discovered.
   * @return A Vertex has the minimum distance to the source.
   */
  private map.Vertex extractMin(Set<map.Vertex> toDiscover) {
    map.Vertex result = null;
    for (map.Vertex vertex : toDiscover) {
      if (result == null) {
        result = vertex;
      } else if (vertex.getDistance() < result.getDistance()) {
        result = vertex; // replace with vertex has smaller distance to source
      }
    }
    return result;
  }
}
