package com.pg.astar.pathfinder.service;

import com.pg.astar.pathfinder.entity.NodeEntity;
import com.pg.astar.pathfinder.exception.NoPathFoundException;
import com.pg.astar.pathfinder.exception.NodeNotFoundException;
import com.pg.astar.pathfinder.model.PathResult;
import com.pg.astar.pathfinder.repository.NodeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

  private final AStarPathfinder pathfinder;
  private final NodeRepository nodeRepository;

  public List<PathResult> findRoute(String from, String to) {
    // 1. Retrieve start/goal nodes from database
    log.debug("Looking up start node: {}", from);
    NodeEntity startNode =
        nodeRepository
            .findByNameWithRelationships(from)
            .orElseThrow(
                () -> {
                  log.warn("Start node not found: {}", from);
                  return new NodeNotFoundException("Start node not found: " + from);
                });

    log.debug("Looking up goal node: {}", to);
    NodeEntity goalNode =
        nodeRepository
            .findByNameWithRelationships(to)
            .orElseThrow(
                () -> {
                  log.warn("Goal node not found: {}", to);
                  return new NodeNotFoundException("Goal node not found: " + to);
                });

    // 2. Execute A* algorithm
    log.debug("Executing A* pathfinding algorithm");
    List<NodeEntity> path = pathfinder.findPath(startNode, goalNode);

    if (path.isEmpty()) {
      log.warn("No path found between '{}' and '{}'", from, to);
      throw new NoPathFoundException("No path found between " + from + " and " + to);
    }

    // 3. Map result to DTOs (PathResult)
    List<PathResult> result =
        path.stream()
            .map(node -> new PathResult(node.getName(), node.getLatitude(), node.getLongitude()))
            .toList();

    log.info("Found path from '{}' to '{}' with {} nodes", from, to, result.size());
    return result;
  }
}
