package us.abstracta.retailstore.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PaymentPage extends BasePage {

  @FindBy(id ="cc-name")
  private WebElement nameInput;
  @FindBy(id ="cc-number")
  private WebElement cardNumberInput;
  @FindBy(id ="cc-expiration")
  private WebElement expirationInput;
  @FindBy(id ="cc-cvv")
  private WebElement cvvInput;
  @FindBy(xpath="//button[contains(.,'Next')]")
  private WebElement nextButton;

  public PaymentPage(WebDriver driver) {
    super(driver);
  }

  public PaymentPage nameOnCard(String name) {
    input(name, nameInput);
    return this;
  }

  public PaymentPage cardNumber(String cardNumber) {
    input(cardNumber, cardNumberInput);
    return this;
  }

  public PaymentPage expiration(String expiration) {
    input(expiration, expirationInput);
    return this;
  }

  public PaymentPage cvv(int cvv) {
    input(String.valueOf(cvv), cvvInput);
    return this;
  }

  public OrderReviewPage next() {
    click(nextButton);
    return new OrderReviewPage(driver);
  }
}
