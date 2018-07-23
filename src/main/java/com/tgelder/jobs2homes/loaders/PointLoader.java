package com.tgelder.jobs2homes.loaders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tgelder.jobs2homes.model.Point;
import de.micromata.opengis.kml.v_2_2_0.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PointLoader {

  public static ImmutableMap<String, ImmutableList<Point>> load(String file) {
    Kml boundaries = Kml.unmarshal(new File(file));

    List<Placemark> placemarks = extractPlacemarks(boundaries);

    ImmutableMap.Builder<String, ImmutableList<Point>> mapBuilder = ImmutableMap.builder();

    for (Placemark placemark : placemarks) {
      mapBuilder.put(extractOutputAreaCode(placemark), extractCoordinates(placemark));
    }

    return mapBuilder.build();
  }

  private static ImmutableList<Point> extractCoordinates(Placemark placemark) {

    List<Geometry> polygons = extractPolygons(placemark);

    List<Coordinate> coordinates = new ArrayList<>();

    for (Geometry geometry : polygons) {
      Polygon polygon = (Polygon) geometry;
      coordinates.addAll(polygon.getOuterBoundaryIs().getLinearRing().getCoordinates());
    }

    return coordinates.stream()
                      .map(coordinate -> new Point(coordinate.getLongitude(), coordinate.getLatitude()))
                      .collect(ImmutableList.toImmutableList());
  }

  private static List<Geometry> extractPolygons(Placemark placemark) {
    MultiGeometry multiGeometry = (MultiGeometry) placemark.getGeometry();
    return multiGeometry.getGeometry();
  }

  private static String extractOutputAreaCode(Placemark placemark) {
    return placemark.getExtendedData().getSchemaData().get(0).getSimpleData().get(0).getValue();
  }

  private static List<Placemark> extractPlacemarks(Kml boundaries) {
    Document document = (Document) boundaries.getFeature();
    Folder folder = (Folder) document.getFeature().get(0);
    return (List<Placemark>) (List) folder.getFeature();
  }


}
