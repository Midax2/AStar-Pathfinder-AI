package com.pg.astar.pathfinder.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Externalized configuration for the SSE training stream endpoint. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "training.sse")
public class TrainingSseProperties {

  /** Estimated milliseconds per training episode, used to scale the SSE connection timeout. */
  private long msPerEpisode = 100;

  /** Minimum SSE connection timeout in milliseconds regardless of episode count. */
  private long minTimeoutMs = 600_000;
}
