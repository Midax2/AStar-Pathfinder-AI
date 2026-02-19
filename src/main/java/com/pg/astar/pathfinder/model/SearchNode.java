package com.pg.astar.pathfinder.model;

import com.pg.astar.pathfinder.entity.NodeEntity;

/**
 * Helper class for A* algorithm that wraps a NodeEntity with its pathfinding scores. Contains
 * g-score (cost from start), f-score (estimated total cost), and predecessor node.
 */
public record SearchNode(NodeEntity node, double gScore, double fScore, NodeEntity predecessor) {}
