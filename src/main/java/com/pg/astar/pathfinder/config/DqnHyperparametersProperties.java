package com.pg.astar.pathfinder.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Externalized hyperparameters for the DQN model. Tunable via application.properties or env vars. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "dqn.hyperparameters")
public class DqnHyperparametersProperties {

  private double learningRate = 0.00001;
  private double gamma = 0.9;
  private double epsilon = 0.1;
  private int inputSize = 10;
  private int hiddenSize = 64;
  private int hiddenSize2 = 32;
  private int outputSize = 64;
  private int batchSize = 16;
  private int trainingFrequency = 5;
  private double maxReward = 10.0;
  private double gradientClipNorm = 0.5;
}
