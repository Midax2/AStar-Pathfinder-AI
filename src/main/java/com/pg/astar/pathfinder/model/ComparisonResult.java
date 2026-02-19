package com.pg.astar.pathfinder.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** DTO for comparing A* and AI pathfinding results. */
@Data
@Builder
public class ComparisonResult {
  private List<PathResult> aStarPath;
  private List<PathResult> aiPath;
  private long aStarExecutionTimeNanos;
  private long aiExecutionTimeNanos;
  private double aStarTotalDistance;
  private double aiTotalDistance;
  private int aStarNodesVisited;
  private int aiNodesVisited;
  private String winner;
  private String analysisNotes;
}
