package com.pg.astar.pathfinder.service;

import com.pg.astar.pathfinder.entity.EdgeEntity;
import com.pg.astar.pathfinder.entity.NodeEntity;
import com.pg.astar.pathfinder.exception.NoPathFoundException;
import com.pg.astar.pathfinder.exception.NodeNotFoundException;
import com.pg.astar.pathfinder.model.ComparisonResult;
import com.pg.astar.pathfinder.model.PathResult;
import com.pg.astar.pathfinder.repository.NodeRepository;
import com.pg.astar.pathfinder.util.DistanceCalculationUtils;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for comparing A* (classical algorithm) with DQN (AI approach) pathfinding algorithms. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PathComparisonService {

  private final AStarPathfinder aStarPathfinder;
  private final DQNPathfinder dqnPathfinder;
  private final NodeRepository nodeRepository;

  /**
   * Compare A* and DQN pathfinding for the given start and goal nodes.
   *
   * @param from Start node name
   * @param to Goal node name
   * @return Comparison result with both paths and metrics
   */
  public ComparisonResult compareAlgorithms(String from, String to) {
    log.info("Starting comparison between A* and DQN for path from {} to {}", from, to);

    // Retrieve nodes
    NodeEntity startNode =
        nodeRepository
            .findByNameWithRelationships(from)
            .orElseThrow(() -> new NodeNotFoundException("Start node not found: " + from));

    log.info(
        "Start node '{}' loaded with {} outgoing edges",
        startNode.getName(),
        startNode.getRoutes().size());
    if (startNode.getRoutes().isEmpty()) {
      log.error(
          "Start node '{}' has NO edges! This indicates a data loading problem.",
          startNode.getName());
    } else {
      log.debug(
          "Start node edges: {}",
          startNode.getRoutes().stream().map(edge -> edge.targetNode().getName()).toList());
    }

    NodeEntity goalNode =
        nodeRepository
            .findByNameWithRelationships(to)
            .orElseThrow(() -> new NodeNotFoundException("Goal node not found: " + to));

    log.info(
        "Goal node '{}' loaded with {} outgoing edges",
        goalNode.getName(),
        goalNode.getRoutes().size());

    // Run A* algorithm
    log.info("Running A* algorithm...");
    aStarPathfinder.resetNodesVisited();
    long aStarStart = System.nanoTime();
    List<NodeEntity> aStarPath = aStarPathfinder.findPath(startNode, goalNode);
    long aStarTime = System.nanoTime() - aStarStart;
    int aStarNodesVisited = aStarPathfinder.getNodesVisited();

    if (aStarPath.isEmpty()) {
      throw new NoPathFoundException("A* could not find a path from " + from + " to " + to);
    }

    // Run DQN algorithm
    log.info("Running DQN algorithm...");
    dqnPathfinder.resetNodesVisited();
    long dqnStart = System.nanoTime();
    List<NodeEntity> dqnPath = dqnPathfinder.findPath(startNode, goalNode);
    long dqnTime = System.nanoTime() - dqnStart;
    int dqnNodesVisited = dqnPathfinder.getNodesVisited();

    // Convert to PathResult DTOs
    List<PathResult> aStarPathResults = convertToPathResults(aStarPath);
    List<PathResult> dqnPathResults =
        dqnPath.isEmpty() ? Collections.emptyList() : convertToPathResults(dqnPath);

    // Calculate distances
    double aStarDistance = calculateTotalDistance(aStarPath);
    double dqnDistance = dqnPath.isEmpty() ? Double.MAX_VALUE : calculateTotalDistance(dqnPath);

    log.info(
        "Distance comparison - A*: {} km ({} nodes), DQN: {} km ({} nodes)",
        String.format("%.2f", aStarDistance),
        aStarPath.size(),
        dqnPath.isEmpty() ? "N/A" : String.format("%.2f", dqnDistance),
        dqnPath.size());

    // Determine winner and analysis
    String winner = determineWinner(aStarTime, dqnTime, aStarDistance, dqnDistance, dqnPath);
    String analysis = generateAnalysis(aStarPath, dqnPath, aStarDistance, dqnDistance);

    log.info("Comparison completed. Winner: {}", winner);

    return ComparisonResult.builder()
        .aStarPath(aStarPathResults)
        .aiPath(dqnPathResults)
        .aStarExecutionTimeNanos(aStarTime)
        .aiExecutionTimeNanos(dqnTime)
        .aStarTotalDistance(aStarDistance)
        .aiTotalDistance(dqnDistance)
        .aStarNodesVisited(aStarNodesVisited)
        .aiNodesVisited(dqnNodesVisited)
        .winner(winner)
        .analysisNotes(analysis)
        .build();
  }

  /** Convert list of NodeEntity to PathResult DTOs */
  private List<PathResult> convertToPathResults(List<NodeEntity> nodes) {
    return nodes.stream()
        .map(node -> new PathResult(node.getName(), node.getLatitude(), node.getLongitude()))
        .toList();
  }

  /** Calculate total distance of a path */
  private double calculateTotalDistance(List<NodeEntity> path) {
    if (path.size() < 2) {
      return 0.0;
    }

    double totalDistance = 0.0;
    for (int i = 0; i < path.size() - 1; i++) {
      NodeEntity current = path.get(i);
      NodeEntity next = path.get(i + 1);

      // Reload current node with edges if needed
      if (current.getRoutes().isEmpty()) {
        log.debug("Reloading node {} with edges for distance calculation", current.getName());
        current = nodeRepository.findByNameWithRelationships(current.getName()).orElse(current);
      }

      // Make final reference for lambda
      final NodeEntity finalCurrent = current;

      // Find the edge connecting current to next
      double edgeDistance =
          finalCurrent.getRoutes().stream()
              .filter(edge -> edge.targetNode().getName().equals(next.getName()))
              .findFirst()
              .map(EdgeEntity::distance)
              .orElseGet(
                  () -> {
                    double haversine =
                        DistanceCalculationUtils.calculateHaversineDistance(
                            finalCurrent.getLatitude(),
                            finalCurrent.getLongitude(),
                            next.getLatitude(),
                            next.getLongitude());
                    log.warn(
                        "Edge not found from {} to {}, using Haversine distance: {} km",
                        finalCurrent.getName(),
                        next.getName(),
                        String.format("%.2f", haversine));
                    return haversine;
                  });

      totalDistance += edgeDistance;
      log.debug(
          "Path segment {}->{}: {} km (total so far: {} km)",
          finalCurrent.getName(),
          next.getName(),
          String.format("%.2f", edgeDistance),
          String.format("%.2f", totalDistance));
    }

    return totalDistance;
  }

  /** Determine which algorithm performed better */
  private String determineWinner(
      long aStarTime,
      long dqnTime,
      double aStarDistance,
      double dqnDistance,
      List<NodeEntity> dqnPath) {
    if (dqnPath.isEmpty()) {
      return "A* (DQN failed to find path)";
    }

    // A* is guaranteed to find optimal path, so compare optimality first
    double distanceDiff = Math.abs(aStarDistance - dqnDistance);
    if (distanceDiff < 0.01) {
      // Paths are essentially the same distance, compare time
      return aStarTime < dqnTime ? "A* (faster)" : "DQN (faster)";
    } else if (aStarDistance < dqnDistance) {
      return "A* (shorter path)";
    } else {
      return "DQN (shorter path - unusual!)";
    }
  }

  /** Generate detailed analysis of the comparison */
  private String generateAnalysis(
      List<NodeEntity> aStarPath,
      List<NodeEntity> dqnPath,
      double aStarDistance,
      double dqnDistance) {
    StringBuilder analysis = new StringBuilder();

    analysis.append("Path Comparison Analysis:\n");
    analysis.append(
        String.format("- A* path length: %d nodes, %.2f km\n", aStarPath.size(), aStarDistance));

    if (dqnPath.isEmpty()) {
      analysis.append("- DQN failed to find a path\n");
      analysis.append(
          "- Note: DQN is a learning-based approach and may need more training or better exploration\n");
    } else {
      analysis.append(
          String.format("- DQN path length: %d nodes, %.2f km\n", dqnPath.size(), dqnDistance));

      double distanceDiff = ((dqnDistance - aStarDistance) / aStarDistance) * 100;
      if (Math.abs(distanceDiff) < 1.0) {
        analysis.append("- Both algorithms found nearly optimal paths\n");
      } else if (distanceDiff > 0) {
        analysis.append(
            String.format("- DQN path is %.1f%% longer than A* optimal path\n", distanceDiff));
      } else {
        analysis.append(
            String.format(
                "- DQN path is %.1f%% shorter (unexpected - may indicate A* heuristic issue)\n",
                Math.abs(distanceDiff)));
      }
    }

    analysis.append("\nKey Differences:\n");
    analysis.append("- A*: Guaranteed optimal, uses heuristic (Haversine distance)\n");
    analysis.append(
        "- DQN: Learning-based, adapts through training, may explore sub-optimal paths\n");

    return analysis.toString();
  }
}
