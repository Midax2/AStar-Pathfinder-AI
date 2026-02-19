package com.pg.astar.pathfinder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data Transfer Object for Node information with dataset type. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeDTO {
  private String name;
  private String datasetType; // "training" or "testing"
  private double latitude;
  private double longitude;
}
