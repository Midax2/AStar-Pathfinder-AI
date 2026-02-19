package com.pg.astar.pathfinder.service;

import com.pg.astar.pathfinder.entity.NodeEntity;
import com.pg.astar.pathfinder.model.NodeDTO;
import com.pg.astar.pathfinder.repository.NodeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for node-related operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeService {

  private final NodeRepository nodeRepository;

  /**
   * Get all available node names.
   *
   * @return List of node names sorted alphabetically
   */
  public List<String> getAllNodeNames() {
    log.debug("Retrieving all node names");
    List<String> nodeNames =
        nodeRepository.findAll().stream().map(NodeEntity::getName).sorted().toList();
    log.debug("Found {} nodes", nodeNames.size());
    return nodeNames;
  }

  /**
   * Get all nodes with their dataset type information.
   *
   * @return List of NodeDTOs with name, dataset type, and coordinates
   */
  public List<NodeDTO> getAllNodesWithDataset() {
    log.debug("Retrieving all nodes with dataset information");
    List<NodeDTO> nodes =
        nodeRepository.findAll().stream()
            .map(
                node ->
                    NodeDTO.builder()
                        .name(node.getName())
                        .datasetType(node.getDatasetType())
                        .latitude(node.getLatitude())
                        .longitude(node.getLongitude())
                        .build())
            .sorted((a, b) -> a.getName().compareTo(b.getName()))
            .toList();
    log.debug("Found {} nodes", nodes.size());
    return nodes;
  }
}
