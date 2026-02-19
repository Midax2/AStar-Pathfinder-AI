package com.pg.astar.pathfinder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Real-time training progress information */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgress {
  private int currentEpisode;
  private int totalEpisodes;
  private double currentLoss;
  private double averageLoss;
  private long trainingTimeMillis;
  private int percentComplete;
  private String status;
}
