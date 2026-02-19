package com.pg.astar.pathfinder.entity;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

/** Node Model (intersection/point on the map). */
@Node("Node")
@Getter
@Setter
@NoArgsConstructor
public class NodeEntity {

  @Id private String name;

  private double latitude;
  private double longitude;
  private String datasetType; // "training" or "testing"

  @Setter
  @Relationship(type = "CONNECTS_TO", direction = Relationship.Direction.OUTGOING)
  private Set<EdgeEntity> routes = new HashSet<>();
}
