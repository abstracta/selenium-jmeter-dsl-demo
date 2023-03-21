  public static void main(String[] args) throws InterruptedException{
    new FrontendPerformanceTest()
        .backendPerformanceTest({{backendTestClassName}}.class.getName(), "test")
        .frontendTest("{{frontendTestClass}}", "{{frontendTestMethod}}", Duration.ofMinutes(5))
        .basePageObject("{{basePageObject}}")
        .run();
  }
