package com.pg.astar.pathfinder.repository;

import com.pg.astar.pathfinder.entity.NodeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing NodeEntity objects in Neo4j database. Provides basic CRUD operations and
 * custom query methods.
 */
@Repository
public interface NodeRepository extends Neo4jRepository<NodeEntity, String> {

  /**
   * Find a node by its name (ID) with relationships loaded.
   *
   * @param name The unique name identifier of the node
   * @return Optional containing the node if found, empty otherwise
   */
  @Query(
      "MATCH (n:Node {name: $name}) WITH n MATCH (n)-[r:CONNECTS_TO]->(m:Node) RETURN n, collect(r), collect(m)")
  Optional<NodeEntity> findByNameWithRelationships(@Param("name") String name);

  /**
   * Find a node by its name (ID) - fallback method.
   *
   * @param name The unique name identifier of the node
   * @return Optional containing the node if found, empty otherwise
   */
  Optional<NodeEntity> findByName(String name);

  /**
   * Find all nodes belonging to a specific dataset type (training or testing) with relationships.
   *
   * @param datasetType The dataset type ("training" or "testing")
   * @return List of nodes in the specified dataset
   */
  @Query(
      "MATCH (n:Node) WHERE n.datasetType = $datasetType "
          + "WITH n MATCH (n)-[r:CONNECTS_TO]->(m:Node) "
          + "RETURN n, collect(r), collect(m)")
  List<NodeEntity> findByDatasetType(String datasetType);
}
