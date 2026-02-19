package com.pg.astar.pathfinder.service;

import com.pg.astar.pathfinder.entity.EdgeEntity;
import com.pg.astar.pathfinder.entity.NodeEntity;
import com.pg.astar.pathfinder.repository.NodeRepository;
import com.pg.astar.pathfinder.util.DistanceCalculationUtils;
import jakarta.annotation.PostConstruct;
import java.util.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;

/**
 * AI-based pathfinding using Deep Q-Network (DQN). This service uses a neural network to learn
 * optimal paths through reinforcement learning.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DQNPathfinder {

  private final NodeRepository nodeRepository;
  private final AStarPathfinder aStarPathfinder;
  private MultiLayerNetwork model;
  @Getter private boolean isTrained = false;
  @Getter private int nodesVisited = 0;

  // Progress callback for real-time updates
  @FunctionalInterface
  public interface ProgressCallback {
    void onProgress(int currentEpisode, int totalEpisodes, double loss);
  }

  @Setter private ProgressCallback progressCallback;

  // Hyperparameters
  private static final double LEARNING_RATE = 0.00001; // 10x lower - prevent any overshooting
  private static final double GAMMA = 0.9; // Lower discount - focus on immediate rewards
  private static final double EPSILON = 0.1; // Lower exploration
  private static final int INPUT_SIZE = 10;
  private static final int HIDDEN_SIZE = 64; // Reduced network size for stability
  private static final int HIDDEN_SIZE_2 = 32; // Smaller second layer
  private static final int OUTPUT_SIZE = 64;
  private static final int BATCH_SIZE = 16; // Smaller batches for more stable updates
  private static final int TRAINING_FREQUENCY = 5; // Less frequent training
  private static final double MAX_REWARD = 10.0; // Much smaller rewards (10x reduction)
  private static final double GRADIENT_CLIP_NORM = 0.5; // Much stricter clipping

  @PostConstruct
  public void initialize() {
    log.info("Initializing DQN Pathfinder...");
    initializeNetwork();
  }

  /** Initialize the neural network architecture */
  private void initializeNetwork() {
    MultiLayerConfiguration conf =
        new NeuralNetConfiguration.Builder()
            .seed(12345)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(LEARNING_RATE))
            .gradientNormalization(
                org.deeplearning4j.nn.conf.GradientNormalization
                    .ClipElementWiseAbsoluteValue) // Clip gradients
            .gradientNormalizationThreshold(GRADIENT_CLIP_NORM) // Threshold for clipping
            .list()
            .layer(
                0,
                new DenseLayer.Builder()
                    .nIn(INPUT_SIZE)
                    .nOut(HIDDEN_SIZE)
                    .activation(Activation.RELU)
                    .build())
            .layer(
                1,
                new DenseLayer.Builder()
                    .nIn(HIDDEN_SIZE)
                    .nOut(HIDDEN_SIZE_2)
                    .activation(Activation.RELU)
                    .build())
            .layer(
                2,
                new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                    .nIn(HIDDEN_SIZE_2)
                    .nOut(OUTPUT_SIZE)
                    .activation(Activation.IDENTITY)
                    .build())
            .build();

    model = new MultiLayerNetwork(conf);
    model.init();
    log.info(
        "Neural network initialized with {} parameters and gradient clipping at {}",
        model.numParams(),
        GRADIENT_CLIP_NORM);
  }

  /**
   * Train the DQN model on random paths
   *
   * @param episodes Number of training episodes
   * @return Average loss over all episodes
   */
  public double train(int episodes) {
    log.info("Starting DQN training for {} episodes", episodes);
    double totalLoss = 0.0;

    // Enable quiet mode for A* during training to reduce log noise
    aStarPathfinder.setQuietMode(true);

    // Use only training nodes to prevent data leakage
    List<NodeEntity> trainingNodes = nodeRepository.findByDatasetType("training");
    log.info("Training on {} nodes marked as 'training' dataset", trainingNodes.size());

    if (trainingNodes.size() < 2) {
      log.warn(
          "Not enough training nodes for training. Found {} training nodes. Please ensure nodes are marked with datasetType='training'",
          trainingNodes.size());
      return 0.0;
    }

    Random random = new Random();

    // Track loss trends
    double recentLossSum = 0.0;
    int recentLossCount = 0;
    double previousWindowAvg = 0.0;

    for (int episode = 0; episode < episodes; episode++) {
      // Pick random start and goal from training nodes only
      NodeEntity start = trainingNodes.get(random.nextInt(trainingNodes.size()));
      NodeEntity goal = trainingNodes.get(random.nextInt(trainingNodes.size()));

      if (start.getName().equals(goal.getName())) {
        continue; // Skip if same node
      }

      // Every 3 episodes, train on an optimal A* path for supervision
      if (episode % 3 == 0) {
        try {
          List<NodeEntity> optimalPath = aStarPathfinder.findPath(start, goal);
          if (!optimalPath.isEmpty()) {
            double supervisedLoss = trainFromOptimalPath(optimalPath, goal);
            totalLoss += supervisedLoss;
            recentLossSum += supervisedLoss;
            recentLossCount++;
            log.debug(
                "Episode {}: Learned from optimal path with {} nodes, loss: {}",
                episode,
                optimalPath.size(),
                String.format("%.4f", supervisedLoss));
          }
        } catch (Exception e) {
          log.debug("Could not get A* path for supervision: {}", e.getMessage());
        }
      }

      // Simulate an episode with reinforcement learning
      double episodeLoss = trainEpisode(start, goal);
      totalLoss += episodeLoss;
      recentLossSum += episodeLoss;
      recentLossCount++;

      if ((episode + 1) % 100 == 0) {
        double avgLossAtStep = totalLoss / (episode + 1);
        double windowAvg = recentLossSum / Math.max(1, recentLossCount);

        // Check if loss is rising
        if (episode > 100 && windowAvg > previousWindowAvg * 1.5) {
          log.warn(
              "Episode {}/{}, WARNING: Loss increasing! Window avg: {} (prev: {}), Overall avg: {}",
              episode + 1,
              episodes,
              String.format("%.4f", windowAvg),
              String.format("%.4f", previousWindowAvg),
              String.format("%.4f", avgLossAtStep));
        } else {
          log.info(
              "Episode {}/{}, Average Loss: {}, Window Loss: {}",
              episode + 1,
              episodes,
              String.format("%.4f", avgLossAtStep),
              String.format("%.4f", windowAvg));
        }

        previousWindowAvg = windowAvg;
        recentLossSum = 0.0;
        recentLossCount = 0;

        // Send progress update
        if (progressCallback != null) {
          progressCallback.onProgress(episode + 1, episodes, avgLossAtStep);
        }
      } else if ((episode + 1) % 10 == 0 && progressCallback != null) {
        // More frequent updates for UI (every 10 episodes)
        double avgLossAtStep = totalLoss / (episode + 1);
        progressCallback.onProgress(episode + 1, episodes, avgLossAtStep);
      }
    }

    // Disable quiet mode for A* after training
    aStarPathfinder.setQuietMode(false);

    isTrained = true;
    double avgLoss = totalLoss / episodes;
    log.info("Training completed. Average loss: {}", avgLoss);
    return avgLoss;
  }

  /**
   * Train from an optimal path found by A* algorithm (supervised learning) This helps the model
   * learn from good examples
   */
  private double trainFromOptimalPath(List<NodeEntity> optimalPath, NodeEntity goal) {
    if (optimalPath.size() < 2) {
      return 0.0;
    }

    List<INDArray> stateBatch = new ArrayList<>();
    List<INDArray> targetBatch = new ArrayList<>();

    // For each step in the optimal path, teach the model to prefer the next optimal node
    for (int i = 0; i < optimalPath.size() - 1; i++) {
      NodeEntity current = optimalPath.get(i);
      NodeEntity optimalNext = optimalPath.get(i + 1);

      // Get current state and Q-values
      INDArray state = encodeState(current, goal);
      INDArray qValues = model.output(state);

      // Calculate reward for following optimal path (tiny scale)
      double remainingSteps = optimalPath.size() - i - 1;
      double reward = MAX_REWARD * 0.8 + (MAX_REWARD * 0.2 / (remainingSteps + 1));
      // Ranges from 8.0 to 10.0 (10x smaller than before)

      // Set target Q-value for the optimal action
      INDArray target = qValues.dup();
      int actionIndex = getActionIndex(current, optimalNext);

      if (actionIndex >= 0 && actionIndex < OUTPUT_SIZE) {
        // For optimal actions, set target value (capped at MAX_REWARD)
        target.putScalar(0, actionIndex, Math.min(reward, MAX_REWARD));

        // Gently penalize other actions (less aggressive)
        for (EdgeEntity edge : current.getRoutes()) {
          if (!edge.targetNode().getName().equals(optimalNext.getName())) {
            int otherAction = getActionIndex(current, edge.targetNode());
            if (otherAction >= 0 && otherAction < OUTPUT_SIZE) {
              double currentQ = qValues.getDouble(0, otherAction);
              // Reduce by only 20% to avoid large negative values
              target.putScalar(0, otherAction, currentQ * 0.8);
            }
          }
        }

        stateBatch.add(state);
        targetBatch.add(target);
      }
    }

    // Train on the batch
    if (!stateBatch.isEmpty()) {
      INDArray stateBatchArray = Nd4j.vstack(stateBatch);
      INDArray targetBatchArray = Nd4j.vstack(targetBatch);
      model.fit(stateBatchArray, targetBatchArray);
      return model.score();
    }

    return 0.0;
  }

  /** Train on a single episode (start to goal) */
  private double trainEpisode(NodeEntity start, NodeEntity goal) {
    NodeEntity current = start;
    Set<String> visited = new HashSet<>();
    double totalLoss = 0.0;
    int steps = 0;
    int maxSteps = 30; // Further reduced to focus on shorter paths
    int trainingSteps = 0;
    int stuckCounter = 0; // Track if agent is stuck

    // Experience buffer for batch training
    List<INDArray> stateBatch = new ArrayList<>();
    List<INDArray> targetBatch = new ArrayList<>();

    while (!current.getName().equals(goal.getName()) && steps < maxSteps && stuckCounter < 3) {
      visited.add(current.getName());

      // Get current state
      INDArray state = encodeState(current, goal);

      // Get Q-values for all actions
      INDArray qValues = model.output(state);

      if (current.getRoutes().isEmpty()) {
        break; // Dead end
      }

      // Choose action (epsilon-greedy with distance heuristic bias)
      NodeEntity nextNode;
      double reward;

      if (Math.random() < EPSILON) {
        // Explore: but prefer closer neighbors (guided exploration)
        List<EdgeEntity> routes = new ArrayList<>(current.getRoutes());

        // 70% chance to pick a neighbor closer to goal during exploration
        if (Math.random() < 0.7) {
          EdgeEntity bestEdge = null;
          double bestDistance = Double.POSITIVE_INFINITY;

          for (EdgeEntity edge : routes) {
            if (!visited.contains(edge.targetNode().getName())) {
              double dist =
                  DistanceCalculationUtils.calculateHaversineDistance(
                      edge.targetNode().getLatitude(),
                      edge.targetNode().getLongitude(),
                      goal.getLatitude(),
                      goal.getLongitude());
              if (dist < bestDistance) {
                bestDistance = dist;
                bestEdge = edge;
              }
            }
          }

          if (bestEdge != null) {
            nextNode = bestEdge.targetNode();
          } else {
            // All visited, pick random
            EdgeEntity randomEdge = routes.get(new Random().nextInt(routes.size()));
            nextNode = randomEdge.targetNode();
            stuckCounter++;
          }
        } else {
          // 30% truly random exploration
          EdgeEntity randomEdge = routes.get(new Random().nextInt(routes.size()));
          nextNode = randomEdge.targetNode();
        }
      } else {
        // Exploit: choose best Q-value
        nextNode = selectBestAction(current, qValues);
      }

      // Calculate reward with normalized scale
      double distanceToGoal =
          DistanceCalculationUtils.calculateHaversineDistance(
              nextNode.getLatitude(),
              nextNode.getLongitude(),
              goal.getLatitude(),
              goal.getLongitude());

      double previousDistance =
          DistanceCalculationUtils.calculateHaversineDistance(
              current.getLatitude(),
              current.getLongitude(),
              goal.getLatitude(),
              goal.getLongitude());

      // Extremely conservative reward structure (all values 10x smaller)
      if (nextNode.getName().equals(goal.getName())) {
        reward = MAX_REWARD; // Maximum reward = 10.0
      } else if (distanceToGoal < previousDistance) {
        // Reward for moving closer (very small scale)
        double improvement = (previousDistance - distanceToGoal) / 1000.0;
        reward = 1.0 + (improvement * 2.0); // Range: 1.0 to ~3.0
      } else {
        // Small penalty for moving away
        double worsening = (distanceToGoal - previousDistance) / 1000.0;
        reward = -0.5 - (worsening); // Range: -0.5 to ~-1.5
      }

      // Small cycle penalty
      if (visited.contains(nextNode.getName())) {
        reward -= 2.0; // Reduced from 20
      }

      // Tiny step penalty
      reward -= 0.1; // Reduced from 1.0

      // Clip reward to tiny range
      reward = Math.max(-MAX_REWARD, Math.min(MAX_REWARD, reward));

      // CRITICAL: Check for NaN
      if (Double.isNaN(reward) || Double.isInfinite(reward)) {
        log.error(
            "Invalid reward detected: {} (distances: current={}, next={})",
            reward,
            previousDistance,
            distanceToGoal);
        reward = -10.0; // Safe fallback
      }

      // Get next state and Q-values
      INDArray nextState = encodeState(nextNode, goal);
      INDArray nextQValues = model.output(nextState);

      // Calculate target Q-value (Bellman equation)
      double maxNextQ = nextQValues.maxNumber().doubleValue();

      // Check for NaN in Q-values
      if (Double.isNaN(maxNextQ) || Double.isInfinite(maxNextQ)) {
        log.error("NaN/Inf detected in Q-values! Resetting to 0");
        maxNextQ = 0.0;
      }

      // Clip maxNextQ to reasonable range (smaller bounds)
      maxNextQ =
          Math.max(-MAX_REWARD * 5, Math.min(MAX_REWARD * 5, maxNextQ)); // ±50 instead of ±1000

      double targetQ = reward + GAMMA * maxNextQ;

      // Check for NaN in target Q
      if (Double.isNaN(targetQ) || Double.isInfinite(targetQ)) {
        log.error("NaN/Inf detected in target Q-value! Using reward only");
        targetQ = reward;
      }

      // Clip final target Q-value to smaller range
      targetQ =
          Math.max(-MAX_REWARD * 5, Math.min(MAX_REWARD * 5, targetQ)); // ±50 instead of ±1000

      // Update Q-value for the chosen action
      int actionIndex = getActionIndex(current, nextNode);
      if (actionIndex >= 0 && actionIndex < OUTPUT_SIZE) {
        INDArray target = qValues.dup();
        target.putScalar(0, actionIndex, targetQ);

        // Add to batch instead of training immediately
        stateBatch.add(state);
        targetBatch.add(target);

        // Train in batches for efficiency
        if (stateBatch.size() >= BATCH_SIZE || steps % TRAINING_FREQUENCY == 0) {
          if (!stateBatch.isEmpty()) {
            INDArray stateBatchArray = Nd4j.vstack(stateBatch);
            INDArray targetBatchArray = Nd4j.vstack(targetBatch);
            model.fit(stateBatchArray, targetBatchArray);
            totalLoss += model.score();
            trainingSteps++;

            stateBatch.clear();
            targetBatch.clear();
          }
        }
      }

      current = nextNode;
      steps++;
    }

    // Train on remaining batch
    if (!stateBatch.isEmpty()) {
      INDArray stateBatchArray = Nd4j.vstack(stateBatch);
      INDArray targetBatchArray = Nd4j.vstack(targetBatch);
      model.fit(stateBatchArray, targetBatchArray);
      totalLoss += model.score();
      trainingSteps++;
    }

    return totalLoss / Math.max(trainingSteps, 1);
  }

  /**
   * Find path using the trained DQN model
   *
   * @param startNode Starting node
   * @param goalNode Goal node
   * @return List of nodes forming the path
   */
  public List<NodeEntity> findPath(NodeEntity startNode, NodeEntity goalNode) {
    if (!isTrained) {
      log.warn("Model not trained yet. Using random walk.");
    }

    nodesVisited = 0;
    List<NodeEntity> path = new ArrayList<>();
    NodeEntity current = startNode;
    Set<String> visited = new HashSet<>();
    Map<String, Set<String>> failedTransitions = new HashMap<>(); // Track failed node transitions
    int maxSteps = 200; // Increased for larger network
    int backtrackCount = 0;
    int maxBacktracks = 20; // Limit backtracking attempts to prevent infinite loops

    path.add(current);
    log.info("Starting DQN pathfinding from {} to {}", startNode.getName(), goalNode.getName());

    while (!current.getName().equals(goalNode.getName()) && path.size() < maxSteps) {
      visited.add(current.getName());
      nodesVisited++;

      if (current.getRoutes().isEmpty()) {
        log.debug("Current node {} has no edges, reloading from database...", current.getName());
        current = nodeRepository.findByNameWithRelationships(current.getName()).orElse(current);

        if (current.getRoutes().isEmpty()) {
          log.warn("Dead end reached at node {} (even after reload)", current.getName());
          return Collections.emptyList();
        }
        log.debug(
            "Reloaded node {} now has {} edges", current.getName(), current.getRoutes().size());
      }

      // Get state and Q-values
      INDArray state = encodeState(current, goalNode);
      INDArray qValues = model.output(state);

      // Get failed transitions for current node
      Set<String> blockedNodes = failedTransitions.getOrDefault(current.getName(), new HashSet<>());

      // This guides the AI to prefer neighbors that are closer to the goal
      INDArray biasedQValues = qValues.dup();
      List<EdgeEntity> routes = new ArrayList<>(current.getRoutes());
      for (int i = 0; i < routes.size() && i < OUTPUT_SIZE; i++) {
        NodeEntity neighbor = routes.get(i).targetNode();
        double distanceToGoal =
            DistanceCalculationUtils.calculateHaversineDistance(
                neighbor.getLatitude(), neighbor.getLongitude(),
                goalNode.getLatitude(), goalNode.getLongitude());

        // Add bonus for neighbors closer to goal (scaled heuristic)
        double heuristicBonus = 50.0 / (1.0 + distanceToGoal / 1000.0);
        int actionIndex = getActionIndex(current, neighbor);
        if (actionIndex >= 0 && actionIndex < OUTPUT_SIZE) {
          double originalQ = qValues.getDouble(0, actionIndex);
          biasedQValues.putScalar(0, actionIndex, originalQ + heuristicBonus);
        }
      }

      // Select best action using biased Q-values, avoiding failed transitions
      NodeEntity nextNode =
          selectBestAction(current, biasedQValues, visited, goalNode, blockedNodes);

      // If no valid next node found, backtrack or fail
      if (nextNode == null || nextNode.equals(current)) {
        backtrackCount++;

        if (backtrackCount > maxBacktracks) {
          log.warn("Exceeded maximum backtrack attempts ({}), path finding failed", maxBacktracks);
          return Collections.emptyList();
        }

        log.debug(
            "No valid next node found at {}, attempting backtrack (attempt {}/{})",
            current.getName(),
            backtrackCount,
            maxBacktracks);

        // Try to backtrack by removing last node and marking this transition as failed
        if (path.size() > 1) {
          NodeEntity previousNode = path.get(path.size() - 2);

          // Mark this transition as failed
          failedTransitions
              .computeIfAbsent(previousNode.getName(), k -> new HashSet<>())
              .add(current.getName());

          // Remove current node from path and visited
          path.removeLast();
          visited.remove(current.getName());
          current = previousNode;

          log.debug("Backtracked to {}", current.getName());
          continue;
        } else {
          log.warn("Cannot backtrack further, path finding failed");
          return Collections.emptyList();
        }
      }

      // Reset backtrack counter on successful move
      backtrackCount = 0;

      path.add(nextNode);
      log.debug("DQN selected node: {}", nextNode.getName());
      current = nextNode;
    }

    if (current.getName().equals(goalNode.getName())) {
      log.info("DQN found path with {} nodes", path.size());
      return path;
    } else {
      log.warn("DQN failed to find path within step limit");
      return Collections.emptyList();
    }
  }

  /** Encode the current state as a feature vector */
  private INDArray encodeState(NodeEntity current, NodeEntity goal) {
    double[] features = new double[INPUT_SIZE];

    // Current position (normalized)
    features[0] = current.getLatitude() / 90.0;
    features[1] = current.getLongitude() / 180.0;

    // Goal position (normalized)
    features[2] = goal.getLatitude() / 90.0;
    features[3] = goal.getLongitude() / 180.0;

    // Distance to goal (normalized to 0-1 range, assuming max ~5000km)
    double distanceToGoal =
        DistanceCalculationUtils.calculateHaversineDistance(
            current.getLatitude(), current.getLongitude(), goal.getLatitude(), goal.getLongitude());
    features[4] = Math.min(distanceToGoal / 5000.0, 1.0);

    // Direction to goal (bearing as sin/cos for continuity)
    double bearing = calculateBearing(current, goal);
    features[5] = Math.sin(Math.toRadians(bearing));
    features[6] = Math.cos(Math.toRadians(bearing));

    // Number of available routes (normalized)
    features[7] = Math.min(current.getRoutes().size() / 15.0, 1.0);

    // Relative position (delta from current to goal, normalized)
    features[8] = (goal.getLatitude() - current.getLatitude()) / 180.0;
    features[9] = (goal.getLongitude() - current.getLongitude()) / 360.0;

    return Nd4j.create(features, new int[] {1, INPUT_SIZE});
  }

  /**
   * Select the best action based on Q-values, avoiding visited nodes, with distance heuristic
   * fallback
   */
  private NodeEntity selectBestAction(
      NodeEntity current,
      INDArray qValues,
      Set<String> visited,
      NodeEntity goal,
      Set<String> blockedNodes) {
    List<EdgeEntity> routes = new ArrayList<>(current.getRoutes());
    if (routes.isEmpty()) {
      return null;
    }

    // Find unvisited and unblocked candidates
    List<EdgeEntity> unvisitedRoutes =
        routes.stream()
            .filter(edge -> !visited.contains(edge.targetNode().getName()))
            .filter(edge -> !blockedNodes.contains(edge.targetNode().getName()))
            .toList();

    if (unvisitedRoutes.isEmpty()) {
      log.debug("All neighbors of {} have been visited or blocked", current.getName());
      return null;
    }

    // Try to find best unvisited node based on Q-values
    double bestQ = Double.NEGATIVE_INFINITY;
    NodeEntity bestNode = null;

    for (EdgeEntity edge : unvisitedRoutes) {
      NodeEntity candidate = edge.targetNode();
      int actionIndex = getActionIndex(current, candidate);

      if (actionIndex >= 0 && actionIndex < OUTPUT_SIZE) {
        double q = qValues.getDouble(0, actionIndex);
        if (q > bestQ) {
          bestQ = q;
          bestNode = candidate;
        }
      }
    }

    // If Q-values are all very low or similar, use distance heuristic as tiebreaker
    if (bestNode == null || bestQ < -50.0) {
      log.debug("Q-values are poor (bestQ={}), using distance heuristic for node selection", bestQ);
      double bestDistance = Double.POSITIVE_INFINITY;

      for (EdgeEntity edge : unvisitedRoutes) {
        NodeEntity candidate = edge.targetNode();
        double distance =
            DistanceCalculationUtils.calculateHaversineDistance(
                candidate.getLatitude(), candidate.getLongitude(),
                goal.getLatitude(), goal.getLongitude());

        if (distance < bestDistance) {
          bestDistance = distance;
          bestNode = candidate;
        }
      }
    }

    return bestNode;
  }

  /**
   * Select the best action based on Q-values, avoiding visited nodes, with distance heuristic
   * fallback (overload without blocked nodes for backward compatibility)
   */
  private NodeEntity selectBestAction(
      NodeEntity current, INDArray qValues, Set<String> visited, NodeEntity goal) {
    return selectBestAction(current, qValues, visited, goal, new HashSet<>());
  }

  /** Select the best action based on Q-values (overload without visited set for training) */
  private NodeEntity selectBestAction(NodeEntity current, INDArray qValues) {
    List<EdgeEntity> routes = new ArrayList<>(current.getRoutes());
    if (routes.isEmpty()) {
      return current;
    }

    double bestQ = Double.NEGATIVE_INFINITY;
    NodeEntity bestNode = routes.getFirst().targetNode();

    for (EdgeEntity edge : routes) {
      int actionIndex = getActionIndex(current, edge.targetNode());
      if (actionIndex >= 0 && actionIndex < OUTPUT_SIZE) {
        double q = qValues.getDouble(0, actionIndex);
        if (q > bestQ) {
          bestQ = q;
          bestNode = edge.targetNode();
        }
      }
    }

    return bestNode;
  }

  /** Get action index for a transition from current to next node */
  private int getActionIndex(NodeEntity current, NodeEntity next) {
    // Use hash-based approach for action indexing
    String transition = current.getName() + "->" + next.getName();
    return Math.abs(transition.hashCode() % OUTPUT_SIZE);
  }

  /** Calculate bearing from one node to another */
  private double calculateBearing(NodeEntity from, NodeEntity to) {
    double lat1 = Math.toRadians(from.getLatitude());
    double lat2 = Math.toRadians(to.getLatitude());
    double lon1 = Math.toRadians(from.getLongitude());
    double lon2 = Math.toRadians(to.getLongitude());

    double dLon = lon2 - lon1;
    double y = Math.sin(dLon) * Math.cos(lat2);
    double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

    double bearing = Math.toDegrees(Math.atan2(y, x));
    return (bearing + 360) % 360;
  }

  public void resetNodesVisited() {
    nodesVisited = 0;
  }
}
