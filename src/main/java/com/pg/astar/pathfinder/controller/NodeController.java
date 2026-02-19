package com.pg.astar.pathfinder.controller;

import com.pg.astar.pathfinder.model.NodeDTO;
import com.pg.astar.pathfinder.service.NodeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for node-related operations. */
@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NodeController {

  private final NodeService nodeService;

  /**
   * Get all available nodes.
   *
   * @return List of node names
   */
  @GetMapping
  public ResponseEntity<List<String>> getAllNodes() {
    List<String> nodeNames = nodeService.getAllNodeNames();
    return ResponseEntity.ok(nodeNames);
  }

  /**
   * Get all nodes with their dataset type information.
   *
   * @return List of NodeDTOs containing name, dataset type, and coordinates
   */
  @GetMapping("/with-dataset")
  public ResponseEntity<List<NodeDTO>> getAllNodesWithDataset() {
    List<NodeDTO> nodes = nodeService.getAllNodesWithDataset();
    return ResponseEntity.ok(nodes);
  }
}
