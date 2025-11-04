package com.udacity.webcrawler.profiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import java.util.Objects;

import javax.inject.Inject;

final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    if (!klass.isInterface()) {
      throw new IllegalArgumentException("Class to be proxied must be an interface");
    }

    boolean hasProfiled = false;
    for (Method m : klass.getMethods()) {
      if (m.isAnnotationPresent(Profiled.class)) {
        hasProfiled = true;
        break;
      }
    }
    if (!hasProfiled) {
      throw new IllegalArgumentException("Interface does not contain any @Profiled methods");
    }

    @SuppressWarnings("unchecked")
    T proxy = (T)
        Proxy.newProxyInstance(
            klass.getClassLoader(),
            new Class<?>[] {klass},
            new ProfilingMethodInterceptor(clock, delegate, state));
    return proxy;
  }

  @Override
  public void writeData(Path path) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(
        path,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND)) {
      writeData(writer);
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
