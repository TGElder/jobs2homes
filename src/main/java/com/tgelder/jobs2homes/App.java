package com.tgelder.jobs2homes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tgelder.jobs2homes.loaders.CSVLoader;
import com.tgelder.jobs2homes.loaders.PointLoader;
import com.tgelder.jobs2homes.model.OutputArea;
import com.tgelder.jobs2homes.model.Point;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;

import java.io.*;
import java.util.Comparator;
import java.util.Optional;

@Slf4j
public class App {

  public static void main(String... args) throws IOException {
    BasicConfigurator.configure();

    log.info("Loading boundaries");
    ImmutableMap<String, ImmutableList<Point>> boundaries = PointLoader.load(args[0]);
    log.info("Loading workplace population");
    ImmutableMap<String, Integer> workplacePopulation = CSVLoader.load(args[1]);
    log.info("Loading household spaces");
    ImmutableMap<String, Integer> householdSpaces = CSVLoader.load(args[2]);
    Point startPoint = new Point(Double.parseDouble(args[4]), Double.parseDouble(args[3]));


    log.info("Creating output areas");
    ImmutableList<OutputArea> outputAreas =
            boundaries.entrySet()
                      .stream()
                      .map(entry -> {
                        String code = entry.getKey();
                        ImmutableList<Point> boundary = entry.getValue();
                        return new OutputArea(code,
                                              boundary,
                                              workplacePopulation.get(code),
                                              householdSpaces.get(code));
                      })
                      .collect(ImmutableList.toImmutableList());

    log.info("Computing distances");
    ImmutableMap<String, Double> distances = outputAreas
            .stream()
            .collect(ImmutableMap.toImmutableMap(outputArea -> outputArea.getCode(),
                                                 outputArea -> outputArea.getDistanceToFurthestPoint(startPoint)));


    log.info("Sorting by distance");
    ImmutableList<OutputArea> sortedByDistance
            = outputAreas.stream()
                         .sorted(Comparator.comparingDouble(outputArea -> distances.get(outputArea.getCode())))
                         .collect(ImmutableList.toImmutableList());

    int jobs = 0;
    int residences = 0;

    log.info("Writing output");
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[5]))))) {
      for (OutputArea outputArea : sortedByDistance) {
        double distance = distances.get(outputArea.getCode());
        jobs += Optional.ofNullable(outputArea.getWorkingPopulation()).orElse(0);
        residences += Optional.ofNullable(outputArea.getHouseholdSpaces()).orElse(0);
        String line = outputArea.getCode() + "," + distance + "," + jobs + "," + residences;
        log.info(line);
        writer.write(line + "\n");
      }
    }




  }
}
