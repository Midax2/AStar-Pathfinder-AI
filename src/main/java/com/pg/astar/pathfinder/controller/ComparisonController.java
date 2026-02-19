package com.pg.astar.pathfinder.controller;

import com.pg.astar.pathfinder.config.TrainingSseProperties;
import com.pg.astar.pathfinder.model.ComparisonResult;
import com.pg.astar.pathfinder.model.TrainingProgress;
import com.pg.astar.pathfinder.model.TrainingStatus;
import com.pg.astar.pathfinder.service.DQNPathfinder;
import com.pg.astar.pathfinder.service.PathComparisonService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** REST Controller for comparing A* and AI pathfinding algorithms. */
@Slf4j
@RestController
@RequestMapping("/api/comparison")
@RequiredArgsConstructor
public class ComparisonController {

  private final PathComparisonService comparisonService;
  private final DQNPathfinder dqnPathfinder;
  private final TrainingSseProperties sseProperties;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  /**
   * Train the DQN model.
   *
   * @param episodes Number of training episodes (default: 3000)
   * @return Training status
   */
  @PostMapping("/train")
  public ResponseEntity<TrainingStatus> trainAI(@RequestParam(defaultValue = "3000") int episodes) {
    log.info("Received request to train AI model with {} episodes", episodes);

    if (episodes < 1 || episodes > 10000) {
      return ResponseEntity.badRequest()
          .body(
              TrainingStatus.builder()
                  .status("Error: Episodes must be between 1 and 10000")
                  .isTrained(false)
                  .build());
    }

    long startTime = System.currentTimeMillis();
    double avgLoss = dqnPathfinder.train(episodes);
    long trainingTime = System.currentTimeMillis() - startTime;

    TrainingStatus status =
        TrainingStatus.builder()
            .isTrained(true)
            .totalEpisodes(episodes)
            .averageLoss(avgLoss)
            .trainingTimeMillis(trainingTime)
            .status("Training completed successfully")
            .build();

    log.info("Training completed in {} ms", trainingTime);
    return ResponseEntity.ok(status);
  }

  /**
   * Get training status of the DQN model.
   *
   * @return Current training status
   */
  @GetMapping("/training-status")
  public ResponseEntity<TrainingStatus> getTrainingStatus() {
    TrainingStatus status =
        TrainingStatus.builder()
            .isTrained(dqnPathfinder.isTrained())
            .status(
                dqnPathfinder.isTrained()
                    ? "Model is trained and ready"
                    : "Model not trained yet. Use POST /api/comparison/train to train.")
            .build();

    return ResponseEntity.ok(status);
  }

  /**
   * Compare A* and DQN pathfinding algorithms.
   *
   * @param from Start node name
   * @param to Goal node name
   * @return Comparison result with both paths and metrics
   */
  @GetMapping("/compare")
  public ResponseEntity<ComparisonResult> comparePaths(
      @RequestParam String from, @RequestParam String to) {
    log.info("Received comparison request from {} to {}", from, to);

    if (!dqnPathfinder.isTrained()) {
      log.warn("DQN model not trained. Training with 1000 episodes first...");
      dqnPathfinder.train(1000);
    }

    ComparisonResult result = comparisonService.compareAlgorithms(from, to);
    return ResponseEntity.ok(result);
  }

  /**
   * Quick comparison endpoint that auto-trains if needed.
   *
   * @param from Start node name
   * @param to Goal node name
   * @param autoTrain Whether to auto-train if model is not trained (default: true)
   * @return Comparison result
   */
  @GetMapping("/compare-auto")
  public ResponseEntity<?> comparePathsAuto(
      @RequestParam String from,
      @RequestParam String to,
      @RequestParam(defaultValue = "true") boolean autoTrain) {

    if (!dqnPathfinder.isTrained()) {
      if (autoTrain) {
        log.info("Model not trained. Auto-training with 500 episodes...");
        dqnPathfinder.train(500);
      } else {
        return ResponseEntity.badRequest()
            .body("Model not trained. Please train first using POST /api/comparison/train");
      }
    }

    ComparisonResult result = comparisonService.compareAlgorithms(from, to);
    return ResponseEntity.ok(result);
  }

  /**
   * Stream training progress updates via Server-Sent Events.
   *
   * @param episodes Number of training episodes
   * @return SSE emitter for real-time progress
   */
  @GetMapping(value = "/train-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter trainWithProgress(@RequestParam(defaultValue = "3000") int episodes) {
    SseEmitter emitter = new SseEmitter(Math.max(sseProperties.getMinTimeoutMs(), (long) episodes * sseProperties.getMsPerEpisode()));

    executorService.execute(
        () -> {
          try {
            log.info("Starting streaming training for {} episodes", episodes);

            // Set up progress callback
            dqnPathfinder.setProgressCallback(
                (currentEpisode, totalEpisodes, loss) -> {
                  try {
                    TrainingProgress progress =
                        TrainingProgress.builder()
                            .currentEpisode(currentEpisode)
                            .totalEpisodes(totalEpisodes)
                            .currentLoss(loss)
                            .averageLoss(loss)
                            .percentComplete((int) ((currentEpisode * 100.0) / totalEpisodes))
                            .status("Training in progress...")
                            .build();

                    emitter.send(SseEmitter.event().name("progress").data(progress));
                  } catch (Exception e) {
                    log.error("Error sending progress update", e);
                  }
                });

            // Train the model and track time
            long startTime = System.currentTimeMillis();
            double avgLoss = dqnPathfinder.train(episodes);
            long trainingTime = System.currentTimeMillis() - startTime;

            // Send completion event
            TrainingProgress completion =
                TrainingProgress.builder()
                    .currentEpisode(episodes)
                    .totalEpisodes(episodes)
                    .averageLoss(avgLoss)
                    .trainingTimeMillis(trainingTime)
                    .percentComplete(100)
                    .status("Training completed successfully")
                    .build();

            emitter.send(SseEmitter.event().name("complete").data(completion));
            emitter.complete();

          } catch (Exception e) {
            log.error("Error during training", e);
            emitter.completeWithError(e);
          } finally {
            dqnPathfinder.setProgressCallback(null);
          }
        });

    return emitter;
  }

  @PreDestroy
  public void shutdown() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
