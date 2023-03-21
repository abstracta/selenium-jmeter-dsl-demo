package us.abstracta.selenium.jmeterdsl.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.Executors;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontendPerformanceTest {

  private static final Logger LOG = LoggerFactory.getLogger(FrontendPerformanceTest.class);

  private String backendTestClassName;
  private String backendTestMethod;
  private String frontendTestClassName;
  private String frontendTestMethod;
  private Duration frontendLoopDuration;
  private String basePageObject;

  public FrontendPerformanceTest backendPerformanceTest(String className, String testMethod) {
    this.backendTestClassName = className;
    this.backendTestMethod = testMethod;
    return this;
  }

  public FrontendPerformanceTest frontendTest(String className, String testMethod,
      Duration loopDuration) {
    this.frontendTestClassName = className;
    this.frontendTestMethod = testMethod;
    frontendLoopDuration = loopDuration;
    return this;
  }

  public FrontendPerformanceTest basePageObject(String className) {
    this.basePageObject = className;
    return this;
  }

  public void run() throws InterruptedException {
    try (var executor = Executors.newFixedThreadPool(2)) {
      executor.invokeAll(Arrays.asList(
          this::runBackendTest,
          this::runFrontendTestLoop
      ));
    }
  }

  private Object runBackendTest() {
    try {
      runJunitTest(backendTestClassName, backendTestMethod);
    } catch (Exception e) {
      /*
       Assertions are not captured in this catch since they are subclass of error which is a
       separate branch of Throwable hierarchy
       */
      LOG.error("Problem running backend test. You should cancel execution of the entire test", e);
    }
    return null;
  }

  private static void runJunitTest(String testClass, String testMethod) {
    Launcher launcher = LauncherFactory.create();
    launcher.registerTestExecutionListeners(new TestExecutionListener() {
      @Override
      public void executionFinished(TestIdentifier testIdentifier,
          TestExecutionResult testExecutionResult) {
        if (testExecutionResult.getStatus().equals(Status.FAILED)) {
          LOG.warn("{} failed" , testIdentifier.getDisplayName(), testExecutionResult.getThrowable().orElse(null));
        }
      }
    });
    launcher.execute(LauncherDiscoveryRequestBuilder.request()
        .selectors(DiscoverySelectors.selectMethod(testClass, testMethod))
        .build());
  }

  private Object runFrontendTestLoop() {
    try (var collector = new InstrumentedTelemetryCollector()
        .testMethod(frontendTestClassName, frontendTestMethod)
        .basePageObject(basePageObject)
        .start()) {
      var start = Instant.now();
      while (Duration.between(start, Instant.now()).compareTo(frontendLoopDuration) < 0) {
        try {
          runJunitTest(frontendTestClassName, frontendTestMethod);
        } catch (Exception e) {
          LOG.warn("Problem running frontend test. Execution will ignore it and retry running it.",
              e);
        }
        collector.reset();
      }
    } catch (Exception e) {
      LOG.error("Problem running frontend test. You should cancel execution of the entire test", e);
    }
    return null;
  }

}
