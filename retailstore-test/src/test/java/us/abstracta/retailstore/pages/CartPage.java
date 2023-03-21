package us.abstracta.retailstore.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CartPage extends BasePage {

  @FindBy(xpath = "//a[contains(.,'Checkout')]")
  private WebElement checkoutButton;

  public CartPage(WebDriver driver) {
    super(driver);
  }

  public CustomerInfoPage checkout() {
    click(checkoutButton);
    return new CustomerInfoPage(driver);
  }

}
