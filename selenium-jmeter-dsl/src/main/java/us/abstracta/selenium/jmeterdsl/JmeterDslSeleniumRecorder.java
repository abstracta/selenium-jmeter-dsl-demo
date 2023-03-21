package us.abstracta.selenium.jmeterdsl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
import org.apache.jorphan.exec.KeyToolUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.Proxy;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;
import us.abstracta.jmeter.javadsl.recorder.JmeterDslRecorder;
import us.abstracta.jmeter.javadsl.recorder.correlations.CorrelationExtractorBuilder;
import us.abstracta.jmeter.javadsl.recorder.correlations.CorrelationReplacementBuilder;
import us.abstracta.jmeter.javadsl.recorder.correlations.CorrelationRuleBuilder;
import us.abstracta.jmeter.javadsl.util.TestResource;
import us.abstracta.selenium.jmeterdsl.execution.FrontendPerformanceTest;
import us.abstracta.selenium.jmeterdsl.execution.TelemetryConfig;

public class JmeterDslSeleniumRecorder implements BeforeEachCallback,
    AfterEachCallback {

  private static final String DEFAULT_CLASS_NAME = "PerformanceTest";

  private final JmeterDslRecorder recorder = new JmeterDslRecorder()
      .logsDirectory(new File("target/recordings"));
  private String testPackage = "";
  private String className = DEFAULT_CLASS_NAME;
  private String frontendTestClass;
  private String frontendTestMethod;
  private Class<?> basePageObjectClass;

  public JmeterDslSeleniumRecorder testPackage(String testPackage) {
    this.testPackage = testPackage;
    return this;
  }

  public JmeterDslSeleniumRecorder className(String className) {
    this.className = className;
    return this;
  }

  public JmeterDslSeleniumRecorder clearUrlFilter() {
    recorder.clearUrlFilter();
    return this;
  }

  public JmeterDslSeleniumRecorder urlExcludes(String... regexes) {
    recorder.urlExcludes(strings2Patterns(regexes));
    return this;
  }

  private static List<Pattern> strings2Patterns(String[] regexes) {
    return Stream.of(regexes).map(Pattern::compile).collect(Collectors.toList());
  }

  public JmeterDslSeleniumRecorder clearHeaderFilter() {
    recorder.clearHeaderFilter();
    return this;
  }

  public JmeterDslSeleniumRecorder excludingHeadersMatching(String... regexes) {
    recorder.headerExcludes(strings2Patterns(regexes));
    return this;
  }

  public JmeterDslSeleniumRecorder correlationRule(String variableName, String extractorRegex,
      String replacementRegex) {
    return correlationRule(variableName, extractor(extractorRegex), replacement(replacementRegex));
  }

  public JmeterDslSeleniumRecorder correlationRule(String variableName,
      CorrelationExtractorBuilder extractor, CorrelationReplacementBuilder replacement) {
    recorder.correlationRule(new CorrelationRuleBuilder(variableName, extractor, replacement));
    return this;
  }

  public static CorrelationExtractorBuilder extractor(String regex) {
    return new CorrelationExtractorBuilder(Pattern.compile(regex));
  }

  public static CorrelationReplacementBuilder replacement(String regex) {
    return new CorrelationReplacementBuilder(Pattern.compile(regex));
  }

  public JmeterDslSeleniumRecorder basePageObject(Class<?> basePageObjectClass) {
    this.basePageObjectClass = basePageObjectClass;
    return this;
  }

  public Proxy getProxy() {
    var ret = new Proxy();
    ret.setHttpProxy(recorder.getProxy());
    ret.setSslProxy(recorder.getProxy());
    return ret;
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    tuneLoggingConfiguration();
    var testClass = context.getTestClass().get();
    frontendTestClass = testClass.getName();
    frontendTestMethod = context.getTestMethod().get().getName();
    testPackage = !testPackage.isBlank() ? testPackage : testClass.getPackageName();
    start();
  }

  private static void tuneLoggingConfiguration() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    setLogLevel(org.apache.jmeter.protocol.http.proxy.Proxy.class, Level.ERROR, config);
    setLogLevel(ProxyControl.class, Level.ERROR, config);
    setLogLevel(KeyToolUtils.class, Level.ERROR, config);
    setLogLevel(CompoundVariable.class, Level.ERROR, config);
    ctx.updateLoggers();
  }

  private static void setLogLevel(Class<?> loggerClass, Level level, Configuration config) {
    LoggerConfig recorderLogger = config.getLoggerConfig(loggerClass.getName());
    recorderLogger.setLevel(level);
  }

  public void start() throws IOException {
    recorder.start();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    stop();
  }

  public void stop() throws IOException, InterruptedException, TimeoutException {
    recorder.stop();
    Path recordingPath = Path.of(
        "src/test/java/" + testPackage.replace(".", "/") + "/" + className + ".java");
    Files.writeString(recordingPath, fixCode(recorder.getRecording()));
  }

  private String fixCode(String code) {
    code = replaceHeaderCommentsWithPackage(code);
    code = removeDependenciesComments(code);
    code = fixImports(code);
    code = replaceClassName(code);
    code = addJmeterTelemetry(code);
    code = replaceAssertJWithJunitAssertions(code);
    return replaceMain(code);
  }

  private String replaceHeaderCommentsWithPackage(String code) {
    return code.replaceAll("(?s)^.*\n\\*/", "package " + testPackage + ";");
  }

  private String removeDependenciesComments(String code) {
    return code.replaceAll("//DEPS .*\n", "");
  }

  private String fixImports(String code) {
    code = removeUnnecessaryImports(code);
    code = addDurationImport(code);
    return addTelemetryImports(code);
  }

  private String removeUnnecessaryImports(String code) {
    return code.replaceAll(
            "import (java\\.io\\.PrintWriter|org\\.junit\\.platform\\.(engine|launcher)).*\n", "")
        .replaceAll("import .*\\.TestPlanStats;\n", "");
  }

  private String addDurationImport(String code) {
    return code.replaceAll("(?s)(import java.io.IOException;\n)",
        String.format("$1import %s;\n", Duration.class.getName()));
  }

  private String addTelemetryImports(String code) {
    return code.replaceAll("(?s)(\n\npublic class)", String.format("\nimport %s;\n"
            + "import %s;$1", FrontendPerformanceTest.class.getName(),
        TelemetryConfig.class.getName()));
  }

  private String replaceClassName(String code) {
    return code.replaceAll("class " + DEFAULT_CLASS_NAME + "\\{", "class " + className + " {");
  }

  private String addJmeterTelemetry(String code) {
    try {
      String influxDbListenerCode = new TestResource("influxDbListener.java").rawContents();
      return code.replaceAll("(?s)TestPlanStats stats = testPlan\\((.*)\\)\n(\\s+\\)\\.run\\(\\))",
          "var envConfig = new TelemetryConfig();\n    var stats = testPlan($1),\n"
              + influxDbListenerCode
              + "\n$2");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String replaceAssertJWithJunitAssertions(String code) {
    return code.replaceAll("import static org\\.assertj\\.core\\.api\\.Assertions\\.assertThat",
            "import static org.junit.jupiter.api.Assertions.assertEquals")
        .replaceAll("assertThat\\((.*)\\).isEqualTo\\((.+)\\)", "assertEquals($2, $1)");
  }

  private String replaceMain(String code) {
    try {
      String main = new StringTemplate(
          new TestResource("performanceTestMain.template.java").contents())
          .bind("backendTestClassName", className)
          .bind("frontendTestClass", frontendTestClass)
          .bind("frontendTestMethod", frontendTestMethod)
          .bind("basePageObject", basePageObjectClass.getName())
          .solve();
      return code.replaceAll("(?s)\n  /\\*[^}]+}\n", "\n" + main + "\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
