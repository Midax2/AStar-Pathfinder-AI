package com.pg.astar.pathfinder.service;

import com.pg.astar.pathfinder.entity.EdgeEntity;
import com.pg.astar.pathfinder.entity.NodeEntity;
import com.pg.astar.pathfinder.model.SearchNode;
import com.pg.astar.pathfinder.repository.NodeRepository;
import com.pg.astar.pathfinder.util.DistanceCalculationUtils;
import java.util.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Implementation of the A* (AStar) algorithm */
@Getter
@Slf4j
@Service
@RequiredArgsConstructor
public class AStarPathfinder {

  private final NodeRepository nodeRepository;
  private int nodesVisited = 0;

  /** -- SETTER -- Enable or disable quiet mode (suppress logs). */
  @Setter private boolean quietMode = false; // Suppress logs during training

  /**
   * Finds the shortest path using the A* algorithm.
   *
   * @param startNode Starting node.
   * @param goalNode Target node.
   * @return List of nodes forming the shortest path.
   */
  public List<NodeEntity> findPath(NodeEntity startNode, NodeEntity goalNode) {
    nodesVisited = 0;
    // 1. Open Set (PriorityQueue) - stores nodes to visit, sorted by fScore
    PriorityQueue<SearchNode> openSet =
        new PriorityQueue<>(Comparator.comparingDouble(SearchNode::fScore));
    openSet.add(new SearchNode(startNode, 0.0, heuristic(startNode, goalNode), null));

    if (!quietMode) {
      log.info("Starting A* search from {} to {}", startNode.getName(), goalNode.getName());
      log.info("Start node has {} outgoing edges", startNode.getRoutes().size());
    }
    log.debug("Initial heuristic estimate: {} km", heuristic(startNode, goalNode));

    // 2. Maps to track costs and path
    Map<String, Double> gScore = new HashMap<>();
    gScore.put(startNode.getName(), 0.0);

    Map<String, NodeEntity> cameFrom = new HashMap<>();

    while (!openSet.isEmpty()) {
      SearchNode current = openSet.poll();
      NodeEntity currentNode = current.node();
      nodesVisited++;

      if (!quietMode) {
        log.debug(
            "Visiting node: {} (f={}, g={}, h={})",
            currentNode.getName(),
            current.fScore(),
            current.gScore(),
            (current.fScore() - current.gScore()));
      }

      // Termination condition
      if (currentNode.getName().equals(goalNode.getName())) {
        List<NodeEntity> path = reconstructPath(cameFrom, currentNode);
        if (!quietMode) {
          log.info("Path found! Length: {} nodes", path.size());
        }
        return path;
      }

      // Process neighbors
      if (!quietMode) {
        log.debug("Exploring neighbors of {}:", currentNode.getName());
      }
      for (EdgeEntity edge : currentNode.getRoutes()) {
        NodeEntity neighbor = edge.targetNode();

        if (neighbor.getRoutes().isEmpty() && !neighbor.getName().equals(goalNode.getName())) {
          log.debug(
              "Neighbor {} has no edges loaded, reloading from database...", neighbor.getName());
          neighbor =
              nodeRepository.findByNameWithRelationships(neighbor.getName()).orElse(neighbor);
          log.debug(
              "Reloaded neighbor {} now has {} edges",
              neighbor.getName(),
              neighbor.getRoutes().size());
        }

        double tentativeGScore = current.gScore() + edge.distance();
        double oldScore = gScore.getOrDefault(neighbor.getName(), Double.MAX_VALUE);

        if (tentativeGScore < oldScore) {
          cameFrom.put(neighbor.getName(), currentNode);
          gScore.put(neighbor.getName(), tentativeGScore);
          double fScore = tentativeGScore + heuristic(neighbor, goalNode);
          openSet.add(new SearchNode(neighbor, tentativeGScore, fScore, currentNode));
          log.debug(
              "  Neighbor: {}, distance: {}, new g-score: {} (better path found, adding to open set)",
              neighbor.getName(),
              String.format("%.2f", edge.distance()),
              String.format("%.2f", tentativeGScore));
        } else {
          log.debug(
              "  Neighbor: {}, distance: {}, new g-score: {} (not better than existing path, ignoring)",
              neighbor.getName(),
              String.format("%.2f", edge.distance()),
              String.format("%.2f", tentativeGScore));
        }
      }
    }

    if (!quietMode) {
      log.info("No path found between {} and {}", startNode.getName(), goalNode.getName());
    }
    return Collections.emptyList();
  }

  private double heuristic(NodeEntity a, NodeEntity b) {
    return DistanceCalculationUtils.calculateHaversineDistance(
        a.getLatitude(), a.getLongitude(),
        b.getLatitude(), b.getLongitude());
  }

  private List<NodeEntity> reconstructPath(Map<String, NodeEntity> cameFrom, NodeEntity current) {
    LinkedList<NodeEntity> path = new LinkedList<>();
    path.add(current);
    NodeEntity temp = current;
    while (cameFrom.containsKey(temp.getName())) {
      temp = cameFrom.get(temp.getName());
      path.addFirst(temp);
    }
    return path;
  }

  public void resetNodesVisited() {
    nodesVisited = 0;
  }
}
