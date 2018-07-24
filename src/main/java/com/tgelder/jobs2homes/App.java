package com.tgelder.jobs2homes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tgelder.jobs2homes.loaders.CSVLoader;
import com.tgelder.jobs2homes.loaders.PointLoader;
import com.tgelder.jobs2homes.model.OutputArea;
import com.tgelder.jobs2homes.model.Point;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tgelder.jobs2homes.model.OutputArea.safe;

@Slf4j
public class App {

  public static void main(String... args) throws IOException {
    BasicConfigurator.configure();

    log.info("Loading workplace population");
    ImmutableMap<String, Integer> workplacePopulation = CSVLoader.load("src/main/resources/workplace_population.csv");
    log.info("Loading bedrooms");
    ImmutableMap<String, Integer> bedrooms = CSVLoader.load("src/main/resources/bedrooms.csv");
    log.info("Loading household spaces");
    ImmutableList<ImmutableMap<String, Integer>> householdSpaces = CSVLoader.load("src/main/resources/household_spaces.csv", 2);
    log.info("Loading children");
    ImmutableMap<String, Integer> children = CSVLoader.load("src/main/resources/children.csv");
    log.info("Loading couples");
    ImmutableMap<String, Integer> couples = CSVLoader.load("src/main/resources/couples.csv");
    log.info("Loading out of work");
    ImmutableList<ImmutableMap<String, Integer>> oow = CSVLoader.load("src/main/resources/oow.csv", 4);
    Point startPoint = new Point(Double.parseDouble(args[8]), Double.parseDouble(args[7]));
    log.info("Loading boundaries");
    ImmutableMap<String, ImmutableList<Point>> boundaries = PointLoader.load("src/main/resources/england_oa_2011_gen.kml");

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
                                              bedrooms.get(code),
                                              householdSpaces.get(0).get(code),
                                              householdSpaces.get(1).get(code),
                                              children.get(code),
                                              couples.get(code),
                                              oow.get(0).get(code),
                                              oow.get(1).get(code),
                                              oow.get(2).get(code),
                                              oow.get(3).get(code));
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



    log.info("Writing output");
    List<RunningTotal> runningTotals = Arrays.asList(
            new RunningTotal("jobs", area -> safe(area.getWorkingPopulation())),
            new RunningTotal("bedrooms_in_occupied", area -> safe(area.getBedrooms())),
            //new RunningTotal("bedrooms_in_unoccupied", OutputArea::estimateBedroomsInUnoccupiedHouseholds),
            new RunningTotal("total_bedrooms", OutputArea::estimateTotalBedrooms),
            //new RunningTotal("children", area -> -safe(area.getChildren())),
            //new RunningTotal("retired", area -> -safe(area.getRetired())),
            //new RunningTotal("unemployed", area -> -safe(area.getUnemployed())),
            //new RunningTotal("carers", area -> -safe(area.getCarer())),
            //new RunningTotal("disabled", area -> -safe(area.getDisabled())),
            new RunningTotal("allocated_bedrooms", area -> -area.estimateAllocatedBedrooms()),
            new RunningTotal("couples", area -> safe(area.getCouples())),
            new RunningTotal("free_bedrooms", OutputArea::estimateFreeBedrooms),
            new RunningTotal("free_bedrooms_with_coupling", OutputArea::estimateFreeBedroomsWithCoupling)
    );


    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[9]))))) {
      String header = Stream.concat(Stream.of("area", "distance"),
                                    runningTotals.stream()
                                                 .map(RunningTotal::getName))
                            .collect(Collectors.joining(","));
      writer.write(header + "\n");
      log.info(header);
      for (OutputArea outputArea : sortedByDistance) {
        runningTotals.forEach(runningTotal -> runningTotal.accumulate(outputArea));

        String line = Stream.concat(Stream.of(outputArea.getCode(), distances.get(outputArea.getCode()).toString()),
                                    runningTotals.stream()
                                                 .map(runningTotal -> Integer.toString(runningTotal.getValue())))
                            .collect(Collectors.joining(","));
        log.info(line);
        writer.write(line + "\n");
      }
    }


  }

  @Data
  private static class RunningTotal {

    private final String name;
    private final Function<OutputArea, Double> statistic;
    private int value = 0;

    public void accumulate(OutputArea outputArea) {
      value += statistic.apply(outputArea);
    }

  }
}
