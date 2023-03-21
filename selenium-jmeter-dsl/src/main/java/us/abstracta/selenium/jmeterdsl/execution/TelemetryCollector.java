package us.abstracta.selenium.jmeterdsl.execution;

import io.micrometer.core.instrument.Clock;
import io.micrometer.influx.InfluxMeterRegistry;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TelemetryCollector implements AutoCloseable, AfterTestExecutionCallback {

  private final InfluxMeterRegistry registry;
  private final String sessionId;

  public TelemetryCollector() {
    registry = new InfluxMeterRegistry(new TelemetryConfig(), Clock.SYSTEM);
    sessionId = UUID.randomUUID().toString();
  }

  public TelemetryStep startStep(String name) {
    return new TelemetryStep(name, sessionId, registry);
  }

  public <T> T step(String name, Supplier<T> method) {
    try (TelemetryStep ignored = new TelemetryStep(name, sessionId, registry)) {
      return method.get();
    }
  }

  @Override
  public void close() {
    registry.close();
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    close();
  }

}
