package us.abstracta.retailstore;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

public class TestConfig {

  private static final TestConfig INSTANCE = new TestConfig();

  private final Properties props;

  private TestConfig() {
    props = new Properties();
    try {
      props.load(getClass().getResourceAsStream("/.env"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Duration getImplicitTimeout() {
    return Duration.ofSeconds(getLongConfig("IMPLICIT_TIMEOUT_SECONDS", 10));
  }

  private static long getLongConfig(String configName, long defaultVal) {
    String ret = get(configName);
    return ret != null ? Long.parseLong(ret) : defaultVal;
  }

  public static Duration getThinkTime() {
    return Duration.ofMillis(getLongConfig("THINK_TIME_MILLIS", 0));
  }

  public static String get(String key) {
    return INSTANCE.props.getProperty(key);
  }

}
