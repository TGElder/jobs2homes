package com.tgelder.jobs2homes.model;

import lombok.Data;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
public class OutputArea {

  private final String code;
  private final List<Point> boundary;
  private final Integer workingPopulation;
  private final Integer bedrooms;
  private final Integer occupiedHouseholds;
  private final Integer unoccupiedHouseholds;
  private final Integer children;
  private final Integer couples;
  private final Integer unemployed;
  private final Integer retired;
  private final Integer carer;
  private final Integer disabled;

  public double getDistanceToFurthestPoint(Point from) {
    return boundary.stream()
                   .map(point -> point.distanceTo(from))
                   .max(Comparator.comparingDouble(distance -> distance))
                   .get();
  }

  public Double estimateBedroomsInUnoccupiedHouseholds() {
    return (safe(bedrooms )/ safe(occupiedHouseholds)) * safe(unoccupiedHouseholds);
  }

  public Double estimateTotalBedrooms() {
    return safe(bedrooms) + estimateBedroomsInUnoccupiedHouseholds();
  }

  public Double estimateAllocatedBedrooms() {
    return safe(children) + safe(retired) + safe(carer) + safe(disabled);
  }

  public Double estimateFreeBedrooms() {
    return estimateTotalBedrooms() - estimateAllocatedBedrooms();
  }

  public Double estimateFreeBedroomsWithCoupling() {
    return estimateFreeBedrooms() + safe(couples);
  }

  public static Double safe(Integer value) {
    return Optional.ofNullable(value).map(Integer::doubleValue).orElse(0.0);
  }

}
