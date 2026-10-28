
package tests;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import pages.HomePage;
import utils.ConfigReader;
import utils.ReportLogger;

public class MMTLandingPageTest extends BaseTest {

    private HomePage homePage;

    @Test(priority = 1)
    public void verifyHomestaysPageNavigation() {

        String rootUrl = ConfigReader.getProperty("baseUrl");

        ReportLogger.log("Test 1: Open MakeMyTrip Homestays.");
        ReportLogger.log("Opening URL: " + rootUrl);

        driver.get(rootUrl);

        homePage = new HomePage(driver);

        homePage.closePopupIfPresent();
        homePage.navigateToHomestaysCategory();

        String currentUrl = driver.getCurrentUrl();

        ReportLogger.log("Current page URL: " + currentUrl);

        Assert.assertTrue(
            currentUrl.contains("/homestays")
                || !driver.findElements(By.id("fromCity")).isEmpty(),
            "Browser did not navigate to the Homestays page."
        );

        ReportLogger.log(
            "Test 1 passed: Homestays page navigation verified."
        );
    }

    @Test(
        priority = 2,
        dependsOnMethods = "verifyHomestaysPageNavigation"
    )
    public void verifyCityAndDateSelection() {

        ReportLogger.log("Test 2: Select city and travel dates.");

        String city = ConfigReader.getProperty("city");

        ReportLogger.log("City configured for search: " + city);

        homePage.enterCityAndSelectResult(city);
        homePage.selectCheckInAndCheckOutDates();

        ReportLogger.log(
            "Test 2 passed: City and travel dates selected."
        );
    }

    @Test(
    priority = 3,
    dependsOnMethods = "verifyCityAndDateSelection"
)
public void verifyGuestSelectionAndSearch() {

    ReportLogger.log(
        "Test 3: Select guests and search for homestays."
    );

    homePage.selectGuestsAndSearch();

    String currentUrl = driver.getCurrentUrl();
    //String pageSource = driver.getPageSource();
    String pageText = driver.findElement(By.tagName("body")).getText();

    ReportLogger.log("Final URL: " + currentUrl);
    ReportLogger.log("Page text: " + pageText);

    // Fail if the browser displays the Pretty-Print 200 OK response.
    boolean isPrettyPrintPage =
        pageText.contains("200-OK")
        || pageText.contains("Pretty-Print");

    Assert.assertFalse(
        isPrettyPrintPage,
        "Search failed: Pretty-Print 200 OK appeared instead of homestay listings."
    );

    // Also fail if the response page is blank or has no meaningful content.
    Assert.assertTrue(
        pageText != null && !pageText.trim().isEmpty(),
        "Search failed: The results page is blank."
    );

    ReportLogger.log(
        "Test 3 passed: Search returned a non-empty page without the Pretty-Print response."
    );
}
}
