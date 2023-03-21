package us.abstracta.retailstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import us.abstracta.retailstore.pages.HomePage;

public class RetailStoreTest {

  private WebDriver driver;

  @BeforeEach
  public void setup() {
    System.setProperty("webdriver.http.factory", "jdk-http-client");
    var options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.setImplicitWaitTimeout(TestConfig.getImplicitTimeout());
    driver = new ChromeDriver(options);
  }

  @AfterEach
  public void teardown() {
    driver.quit();
  }

  @Test
  public void testCheckout() {
    driver.get("http://retailstore.test/");
    var cart = new HomePage(driver)
        .addHotProductToCart(0);
    var customerInfo = cart.checkout();
    var delivery = customerInfo
        .firstName("Kevin")
        .lastName("McCallister")
        .email("homealone@test.com")
        .address("671 Lincoln Ave")
        .city("Winnetka")
        .state("Illinois")
        .zip("12345")
        .next();
    var payment = delivery
        .priorityMail()
        .next();
    var review = payment
        .nameOnCard("Kevin McCallister")
        .cardNumber("12345")
        .expiration("12/24")
        .cvv(123)
        .next();
    var orderInfo = review.order();
    assertEquals("Thank you for order", orderInfo.messageTitle());
  }

}
