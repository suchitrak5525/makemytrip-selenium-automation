package pages;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.ConfigReader;
import utils.ReportLogger;

public class HomePage extends BasePage {

    private List<By> popupCloseLocators = Arrays.asList(
        By.cssSelector("span.commonModal__close"),
        By.cssSelector("[data-cy='closeModal']"),
        By.cssSelector("button[aria-label='Close']"),
        By.xpath(
            "//div[contains(@class,'commonModal')]" +
            "//span[contains(@class,'close')]"
        )
    );

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void navigateToHomestaysCategory() {
        WebDriverWait wait = waitFor(15);

        WebElement homestaysTab = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(
                    "//nav//li[contains(.,'Homestays') " +
                    "or contains(.,'Villas')] " +
                    "| //a[contains(@href,'/homestays')]"
                )
            )
        );

        ReportLogger.log(
            "Clicking the Villas & Homestays navigation option."
        );

        clickElement(homestaysTab);

        wait.until(d ->
            d.getCurrentUrl().contains("/homestays") ||
            !d.findElements(By.id("fromCity")).isEmpty() ||
            !d.findElements(By.id("city")).isEmpty()
        );
    }

    public void closePopupIfPresent() {
        for (By locator : popupCloseLocators) {
            try {
                WebElement closeButton = waitFor(3).until(
                    ExpectedConditions.elementToBeClickable(locator)
                );

                if (closeButton.isDisplayed()) {
                    clickElement(closeButton);

                    ReportLogger.log(
                        "Popup detected and closed successfully."
                    );
                    return;
                }

            } catch (TimeoutException
                     | NoSuchElementException
                     | StaleElementReferenceException
                     | ElementClickInterceptedException e) {
                // Try the next popup locator.
            }
        }

        ReportLogger.log(
            "No supported popup close button found. Continuing."
        );
    }

    public void enterCityAndSelectResult(String city) {
        WebDriverWait wait = waitFor(15);

        // The actual destination field has id="city" and is read-only.
        WebElement cityField = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("city"))
        );

        ReportLogger.log(
            "Destination before selection: " + getSelectedCity()
        );

        clickElement(cityField);

        // The editable autosuggest input that opens after clicking #city.
        WebElement searchBox = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(
                    "input.react-autosuggest__input, " +
                    "input[placeholder*='Where'], " +
                    "input[placeholder*='where']"
                )
            )
        );

        enterText(searchBox, city);

        
        By optionsLocator = By.cssSelector(
            "ul[role='listbox'] li[role='option'], " +
            ".react-autosuggest__suggestions-list li"
        );

        String cityLower = city.toLowerCase(Locale.ROOT);

        WebElement suggestion = wait.until(d -> d.findElements(optionsLocator)
            .stream()
            .filter(o -> o.isDisplayed() &&
                o.getText().toLowerCase(Locale.ROOT).contains(cityLower))
            .findFirst()
            .orElse(null));

        ReportLogger.log(
            "Selecting city suggestion: " +
            suggestion.getText().replace("\n", " | ").trim()
        );
        try {
            clickElement(suggestion);
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", suggestion);
        }

        // Verify the actual destination field, not just the dropdown.
        try {
            wait.until(d -> {
                String selectedCity = getSelectedCity();

                return selectedCity != null &&
                    selectedCity.toLowerCase(Locale.ROOT).contains(cityLower);
            });
        } catch (TimeoutException e) {
            throw new IllegalStateException(
                "City '" + city + "' was not selected. Destination value: " +
                getSelectedCity(), e
            );
        }

        ReportLogger.log(
            "Verified selected destination: " + getSelectedCity()
        );
    }

    private String getSelectedCity() {
        return driver.findElement(By.id("city")).getAttribute("value");
    }

    public void selectCheckInAndCheckOutDates() {
        WebDriverWait wait = waitFor(20);

        LocalDate checkIn = LocalDate.now();

        int checkOutDays = Integer.parseInt(
            ConfigReader.getProperty("checkOutDays")
        );

        LocalDate checkOut = checkIn.plusDays(checkOutDays);

        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern(
                "EEE MMM dd yyyy", Locale.ENGLISH
            );

        // Wait for the calendar to become visible.
        wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".DayPicker")
            )
        );

        // Check-in first, then the checkout date once it is clickable.
        clickElement(wait.until(
            ExpectedConditions.elementToBeClickable(
                dayLocator(checkIn.format(formatter))
            )
        ));

        clickElement(wait.until(
            ExpectedConditions.elementToBeClickable(
                dayLocator(checkOut.format(formatter))
            )
        ));

        ReportLogger.log(
            "Dates selected: Check-in = " + checkIn +
            " | Check-out = " + checkOut
        );
    }

    private By dayLocator(String ariaLabel) {
        return By.xpath(
            "//div[contains(@class,'DayPicker-Day')" +
            " and @aria-label='" + ariaLabel + "'" +
            " and not(contains(@class,'disabled'))" +
            " and not(contains(@class,'DayPicker-Day--disabled'))]"
        );
    }

    public void selectGuestsAndSearch() {
        WebDriverWait wait = waitFor(20);

        int expectedAdults = Integer.parseInt(
            ConfigReader.getProperty("adults")
        );

        wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".rmsGst__body")
            )
        );

        adjustGuestCount("Adults", expectedAdults);

        ReportLogger.log(
            "Guests selected: Adults = " + getGuestCount("Adults")
        );

        WebElement applyButton = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-cy='RoomsGuestsNew_327']")
            )
        );

        clickElement(applyButton);

        By searchButtonLocator = By.id("hsw_search_button");

        WebElement searchButton = wait.until(
            ExpectedConditions.elementToBeClickable(searchButtonLocator)
        );

        String previousUrl = driver.getCurrentUrl();
        clickElement(searchButton);

        try {
            wait.until(d -> {
                String currentUrl = d.getCurrentUrl();
                String pageText = d.findElement(By.tagName("body")).getText();

                return !currentUrl.equals(previousUrl)
                    || pageText.contains("200-OK")
                    || pageText.contains("Pretty-Print");
            });
        } catch (TimeoutException e) {
            ReportLogger.log(
                "Search response condition was not detected before timeout."
            );
        }

        diagnoseHotelResults();
    }

    private void adjustGuestCount(String guestType, int expectedCount) {
        WebDriverWait wait = waitFor(15);

        By rowLocator = guestRowLocator(guestType);

        wait.until(ExpectedConditions.visibilityOfElementLocated(rowLocator));

        while (getGuestCount(guestType) != expectedCount) {
            int previousCount = getGuestCount(guestType);
            boolean increase = previousCount < expectedCount;

            WebElement counterButton = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    rowLocator
                )
            ).findElement(By.cssSelector(increase
                ? ".counter__button--increment"
                : ".counter__button--decrement"));

            clickElement(counterButton);
            wait.until(d -> getGuestCount(guestType) != previousCount);

            ReportLogger.log(
                (increase ? "Incrementing " : "Decrementing ") + guestType +
                ". Current UI text: " + getGuestCount(guestType)
            );
        }
    }

    private By guestRowLocator(String guestType) {
        return By.xpath(
            "//div[contains(@class,'rmsGst__row')]" +
            "[.//p[normalize-space()='" + guestType + "']]"
        );
    }

    private int getGuestCount(String guestType) {
        WebElement row = driver.findElement(guestRowLocator(guestType));

        String count = row.findElement(
            By.cssSelector(".counter__value")
        ).getText();

        return Integer.parseInt(count.trim());
    }

    public void diagnoseHotelResults() {
        ReportLogger.log(
            "========== RESULTS STATUS RUNNING =========="
        );

        ReportLogger.log(
            "Final Loaded URL Context: " + driver.getCurrentUrl()
        );

        String pageText;
        try {
            pageText = driver.findElement(By.tagName("body")).getText();
        } catch (NoSuchElementException e) {
            pageText = "Body element not found.";
        }

        ReportLogger.log("Final page text: " + pageText);

        ReportLogger.log(
            "============================================"
        );
    }
}
