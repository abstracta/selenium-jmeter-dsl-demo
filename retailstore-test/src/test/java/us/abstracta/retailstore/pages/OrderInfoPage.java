package us.abstracta.retailstore.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OrderInfoPage extends BasePage {

  @FindBy(xpath="//h4")
  private WebElement message;

  public OrderInfoPage(WebDriver driver) {
    super(driver);
  }

  public String messageTitle() {
    return getText(message);
  }

}
