package com.example.demo.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageFileController {

      private static final Path SAMPLE_IMAGE_DIRECTORY = Path.of("src/main/resources/mia/images/");

      @GetMapping("/image-files")
      public @ResponseBody Set<String> index() throws IOException {

            try (Stream<Path> stream = Files.list(SAMPLE_IMAGE_DIRECTORY)) {
                  return stream
                              .filter(file -> !Files.isDirectory(file))
                              .map(Path::getFileName)
                              .map(Path::toString)
                              .collect(Collectors.toSet());
            }

      }
}
