package us.abstracta.selenium.jmeterdsl.execution;

import io.micrometer.influx.InfluxConfig;
import java.io.IOException;
import java.util.Properties;

public class TelemetryConfig implements InfluxConfig {

  private final Properties props;

  public TelemetryConfig() {
    props = new Properties();
    try {
      props.load(getClass().getResourceAsStream("/.env"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String get(String key) {
    return props.getProperty(key.toUpperCase().replace(".", "_"));
  }

}
