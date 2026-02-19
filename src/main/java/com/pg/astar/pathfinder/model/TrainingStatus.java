package com.pg.astar.pathfinder.model;

import lombok.Builder;
import lombok.Data;

/** Status information about AI model training. */
@Data
@Builder
public class TrainingStatus {
  private boolean isTrained;
  private int totalEpisodes;
  private double averageLoss;
  private long trainingTimeMillis;
  private String status;
}
