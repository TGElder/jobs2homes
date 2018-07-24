package com.tgelder.jobs2homes.loaders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.IntStream;

public class CSVLoader {

  public static ImmutableList<ImmutableMap<String, Integer>> load(String file, int columns) throws IOException {

    ImmutableList<ImmutableMap.Builder<String, Integer>> mapBuilders =
            IntStream.range(0, columns)
                     .mapToObj(i -> ImmutableMap.<String, Integer>builder())
                     .collect(ImmutableList.toImmutableList());

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
      reader.lines().forEach(line -> {
        String[] split = line.split(",");
        for (int c = 0; c < columns; c++) {
          mapBuilders.get(c).put(split[0], Integer.parseInt(split[c + 1]));
        }
      });
    }

    return mapBuilders.stream().map(ImmutableMap.Builder::build).collect(ImmutableList.toImmutableList());
  }

  public static ImmutableMap<String, Integer> load(String file) throws IOException {
    return load(file, 1).get(0);
  }

}
