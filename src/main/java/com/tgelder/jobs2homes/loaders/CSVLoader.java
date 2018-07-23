package com.tgelder.jobs2homes.loaders;

import com.google.common.collect.ImmutableMap;

import java.io.*;

public class CSVLoader {

  public static ImmutableMap<String, Integer> load(String file) throws IOException {

    ImmutableMap.Builder<String, Integer> mapBuilder = ImmutableMap.builder();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
      reader.lines().forEach(line -> {
        String[] split = line.split(",");
        mapBuilder.put(split[0], Integer.parseInt(split[1]));
      });
    }

    return  mapBuilder.build();
  }

}
