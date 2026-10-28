package pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {
    protected WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    /** Clicks an element using a standard Selenium interaction. */
    protected void clickElement(WebElement element) {
        element.click();
    }

    /** Enters text directly instead of typing one character at a time. */
    protected void enterText(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
    }

    /** Creates an explicit wait with the given timeout in seconds. */
    protected WebDriverWait waitFor(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }
}
