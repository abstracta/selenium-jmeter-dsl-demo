package us.abstracta.retailstore.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CustomerInfoPage extends BasePage {

  @FindBy(id="firstName")
  private WebElement firstNameInput;
  @FindBy(id="lastName")
  private WebElement lastNameInput;
  @FindBy(id="email")
  private WebElement emailInput;
  @FindBy(id="address1")
  private WebElement addressInput;
  @FindBy(id="city")
  private WebElement cityInput;
  @FindBy(id="state")
  private WebElement stateSelect;
  @FindBy(id="zip")
  private WebElement zipInput;
  @FindBy(xpath="//button[contains(.,'Next')]")
  private WebElement nextButton;

  public CustomerInfoPage(WebDriver driver) {
    super(driver);
  }

  public CustomerInfoPage firstName(String firstName) {
    input(firstName, firstNameInput);
    return this;
  }

  public CustomerInfoPage lastName(String lastName) {
    input(lastName, lastNameInput);
    return this;
  }

  public CustomerInfoPage email(String email) {
    input(email, emailInput);
    return this;
  }

  public CustomerInfoPage address(String address) {
    input(address, addressInput);
    return this;
  }

  public CustomerInfoPage city(String city) {
    input(city, cityInput);
    return this;
  }

  public CustomerInfoPage state(String state) {
    select(state, stateSelect);
    return this;
  }

  public CustomerInfoPage zip(String zip) {
    input(zip, zipInput);
    return this;
  }

  public DeliveryPage next() {
    click(nextButton);
    return new DeliveryPage(driver);
  }

}
