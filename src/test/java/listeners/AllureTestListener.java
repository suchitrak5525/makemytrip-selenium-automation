package listeners;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

import io.qameta.allure.Allure;
import tests.BaseTest;
import utils.ReportLogger;

public class AllureTestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        ReportLogger.clear();
        ReportLogger.log("Test started: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        attachTestEvidence(result, "PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ReportLogger.log(
            "Failure reason: " + result.getThrowable()
        );

        attachTestEvidence(result, "FAILED");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        attachTestEvidence(result, "SKIPPED");
    }

    private void attachTestEvidence(
            ITestResult result, String status) {

        WebDriver driver = BaseTest.getDriver();

        // Attach all captured System.out.println replacement messages.
        String logs = ReportLogger.getLogs();

        if (logs == null || logs.trim().isEmpty()) {
            logs = "No log messages were recorded.";
        }

        Allure.addAttachment(
            "Execution Logs - " + result.getName(),
            "text/plain",
            new ByteArrayInputStream(
                logs.getBytes(StandardCharsets.UTF_8)
            ),
            ".txt"
        );

        // Capture the browser screenshot, when a browser is available.
        if (driver != null) {
            try {
                byte[] screenshot =
                    ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.BYTES);

                Allure.addAttachment(
                    "Screenshot - " + status,
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
                );

            } catch (Exception e) {
                Allure.addAttachment(
                    "Screenshot Capture Error",
                    "text/plain",
                    "Could not capture screenshot: " + e.getMessage()
                );
            }
        }

        ReportLogger.clear();
    }
}