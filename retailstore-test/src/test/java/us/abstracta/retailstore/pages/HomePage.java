package us.abstracta.retailstore.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePage {

  @FindBy(css = ".product")
  private List<WebElement> products;

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public CartPage addHotProductToCart(int productIndex) {
    WebElement addToCartButton = products.get(productIndex)
        .findElement(By.xpath("//a[contains(.,'Add to cart')]"));
    click(addToCartButton);
    return new CartPage(driver);
  }

}
