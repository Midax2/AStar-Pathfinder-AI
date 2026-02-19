package com.pg.astar.pathfinder.controller;

import com.pg.astar.pathfinder.model.PathResult;
import com.pg.astar.pathfinder.service.RouteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller handling route finding requests. */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class RouteController {

  private final RouteService routeService;

  /**
   * Endpoint for route search: GET /api/routes?from=A&to=B
   *
   * @param from Start node name
   * @param to Target node name
   * @return List of path nodes with their coordinates
   */
  @GetMapping
  public ResponseEntity<List<PathResult>> getRoute(
      @RequestParam(name = "from") String from, @RequestParam(name = "to") String to) {
    log.info("Received route request from '{}' to '{}'", from, to);
    List<PathResult> result = routeService.findRoute(from, to);
    return ResponseEntity.ok(result);
  }
}
