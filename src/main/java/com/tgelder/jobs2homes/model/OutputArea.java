package com.tgelder.jobs2homes.model;

import lombok.Data;

import java.util.Comparator;
import java.util.List;

@Data
public class OutputArea {

  private final String code;
  private final List<Point> boundary;
  private final Integer workingPopulation;
  private final Integer householdSpaces;

  public double getDistanceToFurthestPoint(Point from) {
    return boundary.stream()
                   .map(point -> point.distanceTo(from))
                   .max(Comparator.comparingDouble(distance -> distance))
                   .get();
  }

}
