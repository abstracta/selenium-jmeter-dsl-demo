package us.abstracta.retailstore.pages;

import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import us.abstracta.retailstore.TestConfig;

public abstract class BasePage {

  private static final Duration THINK_TIME = TestConfig.getThinkTime();
  protected final WebDriver driver;

  public BasePage(WebDriver driver) {
    this.driver = driver;
    PageFactory.initElements(driver, this);
  }

  protected void click(WebElement element) {
    think();
    /*
     for some reason can't use element.click because we get: Element is not clickable at point
     (x, y). So using javascript was the only option to actually make it work
     */
    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", element);
  }

  private void think() {
    try {
      Thread.sleep(THINK_TIME);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  protected void input(String text, WebElement element) {
    think();
    element.clear();
    element.sendKeys(text);
  }

  protected void select(String text, WebElement element) {
    think();
    new Select(element).selectByVisibleText(text);
  }

  protected String getText(WebElement elem) {
    think();
    return elem.getText();
  }

}
