
package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import utils.ConfigReader;
import utils.ReportLogger;

public class BaseTest {

    protected static WebDriver driver;

    @BeforeSuite
    public void setUp() {
        String browser = ConfigReader.getProperty("browser");

        if (browser.equalsIgnoreCase("chrome")) {
            driver = new ChromeDriver();
        } else {
            throw new RuntimeException(
                "Unsupported browser configuration: " + browser
            );
        }

        driver.manage().window().maximize();

        ReportLogger.log("Chrome browser launched successfully.");
        ReportLogger.log(
            "Browser session initialized for the test suite."
        );
    }

      @AfterSuite(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;

            ReportLogger.log("Browser closed successfully.");
        }
    } 
   public static WebDriver getDriver() {
    return driver;
}
}