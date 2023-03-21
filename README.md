# Selenium JMeter DSL demo

This is a demo project that contains a Selenium test for [Retail Store demo app](https://github.com/aws-containers/retail-store-sample-app) and generates and run performance tests using [JMeter DSL](https://abstracta.github.io/jmeter-java-dsl/).

## Requirements

* Docker + Docker compose
* Maven 3.5+
* Java 19+

## Project Structure

| Path                                                                                                                            | Description                                                                                                    |
|---------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| [retailstore-test](./retailstore-test)                                                                                          | Selenium Test project                                                                                          |
| [retailstore-test/docker-compose.yml](./retailstore-test/docker-compose.yml)                                                    | Provisioning of Retail Store demo app                                                                          |
| [RetailStoreTest](./retailstore-test/src/test/java/us/abstracta/retailstore/RetailStoreTest.java)                               | Selenium Test for Retail Store app                                                                             |
| [.env](./retailstore-test/src/test/resources/.env)                                                                              | Configuration file for Selenium Test                                                                           |
| [selenium-jmeter-dsl](./selenium-jmeter-dsl)                                                                                    | Module contains main logic for recording and executing performance test. Including integration with Jmeter DSL |
| [JmeterDslSeleniumRecorder](./selenium-jmeter-dsl/src/main/java/us/abstracta/selenium/jmeterdsl/JmeterDslSeleniumRecorder.java) | JUnit extension which generates JMeter DSL test plan from a Selenium Test.                                     |
| [docker-compose.yml](./docker-compose.yml)                                                                                      | Provisioning for Grafana & InfluxDB                                                                            |

## Usage

### Selenium test execution

To start Retail Store demo app, go to [retailstore-test folder](./retailstore-test) and run `docker-compose up`.

Set local hosts file so `retailstore.test` points to localhost: `echo '127.0.0.1 retailstore.test' >> /private/etc/hosts`.

You can run [RetailStoreTest](./retailstore-test/src/test/java/us/abstracta/retailstore/RetailStoreTest.java) either using an IDE or using `mvn clean test`.

To see the actual flow running you can remove `"--headless=new"` chrome option and set `THINK_TIME_MILLIS` to a value like 1000 in [.env file](./retailstore-test/src/test/resources/.env).

Remember resetting `THINK_TIME_MILLIS` to 0, and re-adding `"--headless=new"` chrome option before running performance test.

### Performance test recording & tuning

1. Add `selenium-jmeter-dsl` dependency to [retailstore-test/pom.xml](./retailstore-test/pom.xml):

    ```xml
    <dependency>
      <groupId>us.abstracta</groupId>
      <artifactId>selenium-jmeter-dsl</artifactId>
      <version>${project.version}</version>
    </dependency>
    ```
   
2. Register [JmeterDslSeleniumRecorder](./selenium-jmeter-dsl/src/main/java/us/abstracta/selenium/jmeterdsl/JmeterDslSeleniumRecorder.java) JUnit extension in [RetailStoreTest](./retailstore-test/src/test/java/us/abstracta/retailstore/RetailStoreTest.java).
    
    ```java
    @RegisterExtension
    private final JmeterDslSeleniumRecorder recorder = new JmeterDslSeleniumRecorder()
        .basePageObject(BasePage.class);
    ```
   
3. Add `JmeterDslSeleniumRecorder` as proxy in WebDriver options.

    ```java
    options.setProxy(recorder.getProxy());
    ```
   
4. Run `RetailStoreTest.java` & review generated `PerformanceTest.java`.
5. Add InfluxDB listener config to `.env`:

    ```
    INFLUX_URI=http://localhost:8086
    INFLUX_ORG=abstracta
    INFLUX_BUCKET=selenium
    INFLUX_TOKEN=token
    ```

6. Add correlation rule to `JmeterDslSeleniumRecorder`. For fixed values in test plan, `productId` for instance, look into `target/recordings` generated XML files for responses containing the ID and define extraction and replacement regexes.

    ```java
    @RegisterExtension
    private final JmeterDslSeleniumRecorder recorder = new JmeterDslSeleniumRecorder()
        .correlationRule("productId",
            "name=\"productId\" value=\"([^\"]+)\"",
            "productId=(.*)")
        .basePageObject(BasePage.class);
    ```
   
7. Re-run `RetailStoreTest` and review `PerformanceTest.java`.
8. Run `PerformanceTest.java` test method and review calls.
9. Tune thread group configuration in `PerformanceTest.java`

    ```java
    threadGroup()
      .rampToAndHold(10, Duration.ZERO, Duration.ofMinutes(1))
      .rampToAndHold(20, Duration.ZERO, Duration.ofMinutes(1))
      .rampToAndHold(50, Duration.ZERO, Duration.ofMinutes(3))
      .children(...)
    ```
   
10. Remove JUnit extension & proxy from `RetailStoreTest.java`.
11. Start InfluxDB & Grafana running `docker-compose up` at root location of this project.
12. Start PerformanceTest main method.
13. Review results in Grafana at `http://localhost:3000` with `admin` & `1234`.



