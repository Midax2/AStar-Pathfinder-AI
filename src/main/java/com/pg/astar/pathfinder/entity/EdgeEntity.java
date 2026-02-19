package com.pg.astar.pathfinder.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

/** Edge (route/connection) model. Represents a relationship with cost (weight). */
@RelationshipProperties
public record EdgeEntity(
    @Id @GeneratedValue String id,
    @TargetNode NodeEntity targetNode,
    double distance // Weight: distance in kilometers
    ) {}
