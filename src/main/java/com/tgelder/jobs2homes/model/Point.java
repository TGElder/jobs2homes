package com.tgelder.jobs2homes.model;

import com.tgelder.jobs2homes.geometry.GreatCircleCalculator;
import lombok.Data;

@Data
public class Point {
  private final double longitude;
  private final double latitude;

  public double distanceTo(Point other) {
    return GreatCircleCalculator.distVincenty(this.latitude, this.longitude, other.latitude, other.longitude);
  }
}
