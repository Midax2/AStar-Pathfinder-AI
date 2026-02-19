package com.pg.astar.pathfinder.util;

import lombok.NoArgsConstructor;

/** Service for calculating geographical distances. */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DistanceCalculationUtils {

  // Earth radius in kilometers
  private static final double EARTH_RADIUS_KM = 6371.0;

  /**
   * Calculates the Haversine distance between two points on Earth.
   *
   * @param lat1 Latitude of the first point in degrees
   * @param lon1 Longitude of the first point in degrees
   * @param lat2 Latitude of the second point in degrees
   * @param lon2 Longitude of the second point in degrees
   * @return Distance in kilometers
   */
  public static double calculateHaversineDistance(
      double lat1, double lon1, double lat2, double lon2) {
    // Convert degrees to radians
    double lat1Rad = Math.toRadians(lat1);
    double lon1Rad = Math.toRadians(lon1);
    double lat2Rad = Math.toRadians(lat2);
    double lon2Rad = Math.toRadians(lon2);

    // Haversine formula
    double dLat = lat2Rad - lat1Rad;
    double dLon = lon2Rad - lon1Rad;
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    // Distance in kilometers
    return EARTH_RADIUS_KM * c;
  }
}
