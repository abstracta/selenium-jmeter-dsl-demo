package us.abstracta.selenium.jmeterdsl.execution;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;

public class TelemetryStep implements AutoCloseable {

  private final Timer timer;
  private final Instant start;

  public TelemetryStep(String name, String sessionId, MeterRegistry registry) {
    start = Instant.now();
    timer = registry.timer(name, "session", sessionId);
  }

  @Override
  public void close() {
    timer.record(Duration.between(start, Instant.now()));
  }

}
