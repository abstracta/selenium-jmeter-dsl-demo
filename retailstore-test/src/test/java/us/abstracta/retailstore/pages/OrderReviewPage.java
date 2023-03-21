package us.abstracta.retailstore.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OrderReviewPage extends BasePage {

  @FindBy(xpath="//button[contains(.,'Order')]")
  private WebElement orderButton;

  public OrderReviewPage(WebDriver driver) {
    super(driver);
  }

  public OrderInfoPage order() {
    click(orderButton);
    return new OrderInfoPage(driver);
  }

}
