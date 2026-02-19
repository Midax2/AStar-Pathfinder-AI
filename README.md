# AStar-Pathfinder-AI

A Spring Boot application that implements and compares the classical **A\* pathfinding algorithm** against a **Deep Q-Network (DQN) AI** agent on a graph of US cities stored in Neo4j.

## Features

- **A\* Pathfinding** — classic heuristic-based shortest path (Haversine distance)
- **DQN AI Pathfinding** — reinforcement-learning agent trained via DeepLearning4J
- **Algorithm Comparison** — side-by-side metrics (path length, nodes explored, execution time)
- **Real-time Training** — stream training progress via Server-Sent Events (SSE)
- **Web UI** — built-in dashboard at `http://localhost:8080`
- **Neo4j Graph DB** — 50+ US cities with weighted edges; split into training / testing datasets

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | Neo4j 5.16 |
| AI / ML | DeepLearning4J 1.0.0-M2.1 |
| Build | Gradle 8 |
| Code style | Spotless / Google Java Format |
| Container | Docker Compose |

## Prerequisites

- Java 21+
- Docker & Docker Compose

## Getting Started

### 1. Configure Environment

```bash
cp .env.example .env
# Edit .env and set your credentials
```

### 2. Start Neo4j

```bash
docker compose up -d
```

This starts Neo4j (ports `7474` / `7687`) and runs the initialisation script that seeds all city nodes and edges.

### 2. Run the Application

```bash
./gradlew bootRun
```

Open `http://localhost:8080` in your browser.

## REST API

### Nodes

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/nodes` | List all node names |
| GET | `/api/nodes/with-dataset` | List nodes with coordinates and dataset type |

### Routes (A\*)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/routes?from=New York&to=Atlanta` | Find shortest path via A\* |

### AI Comparison

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/comparison/train?episodes=3000` | Train the DQN model |
| GET | `/api/comparison/training-status` | Check training status |
| GET | `/api/comparison/compare?from=X&to=Y` | Compare A\* vs DQN |
| GET | `/api/comparison/compare-auto?from=X&to=Y` | Compare (auto-trains if needed) |
| GET | `/api/comparison/train-stream?episodes=3000` | Stream training progress (SSE) |

## Code Formatting

```bash
./gradlew format
```

## Project Structure

```
src/main/java/com/pg/astar/pathfinder/
├── controller/      # REST controllers (Route, Node, Comparison)
├── service/         # A* and DQN pathfinding logic
├── entity/          # Neo4j node/edge entities
├── model/           # DTOs and result models
├── repository/      # Spring Data Neo4j repositories
├── config/          # Neo4j configuration
├── exception/       # Global exception handling
└── util/            # Haversine distance utilities
```

## License

[MIT](LICENSE)