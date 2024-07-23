package io.github.mianalysis.miaserver.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageFileController {

      private static final Path SAMPLE_IMAGE_DIRECTORY = Paths.get("src/main/resources/mia/images/");

      private final Set<String> imageFiles = listFiles();

      private Set<String> listFiles() {
            try (Stream<Path> stream = Files.list(SAMPLE_IMAGE_DIRECTORY)) {
                  return stream
                              .filter(file -> !Files.isDirectory(file))
                              .map(Path::getFileName)
                              .map(Path::toString)
                              .collect(Collectors.toSet());
            } catch (IOException e) {
                  throw new RuntimeException(e);
            }
      }

      @GetMapping("/image-files")
      public @ResponseBody Set<String> index() throws IOException {
            return imageFiles;
      }

      @GetMapping("/image-files/{filename}")
      public @ResponseBody ResponseEntity<Resource> show(@PathVariable String filename) throws IOException {
            if (!imageFiles.contains(filename)) {
                  return ResponseEntity.notFound().build();
            }

            Resource imageResource = new ClassPathResource("mia/images/" + filename);

            return ResponseEntity.ok()
                        .body(imageResource);
      }
}
