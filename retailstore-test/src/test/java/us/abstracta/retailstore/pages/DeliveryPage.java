package us.abstracta.retailstore.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DeliveryPage extends BasePage {

  @FindBy(xpath="//input[@value='priority-mail']")
  private WebElement priorityMailOption;

  @FindBy(xpath="//button[contains(.,'Next')]")
  private WebElement nextButton;

  public DeliveryPage(WebDriver driver) {
    super(driver);
  }

  public DeliveryPage priorityMail() {
    click(priorityMailOption);
    return this;
  }

  public PaymentPage next() {
    click(nextButton);
    return new PaymentPage(driver);
  }

}
