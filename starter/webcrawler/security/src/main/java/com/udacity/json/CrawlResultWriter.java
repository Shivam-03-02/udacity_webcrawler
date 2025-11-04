package com.udacity.webcrawler.json;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class CrawlResultWriter {
  private final CrawlResult result;

  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  public void write(Path path) {
    Objects.requireNonNull(path);
    try (Writer writer = Files.newBufferedWriter(
        path,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND)) {
      write(writer);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write crawl result to path", e);
    }
  }

  public void write(Writer writer) {
    Objects.requireNonNull(writer);
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
      mapper.writeValue(writer, result);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write crawl result", e);
    }
  }
}
