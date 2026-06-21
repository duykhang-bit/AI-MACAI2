package base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import utils.ConfigReader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BaseTest1 {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static ExtentReports extent;
    protected ExtentTest test;
    protected ConfigReader config;

    // Shared counter cho tất cả test classes - đánh số tăng dần liên tục
    private static final java.util.concurrent.atomic.AtomicInteger globalRunCount =
            new java.util.concurrent.atomic.AtomicInteger(0);

    /* =========================
     * DRIVER
     * ========================= */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Lấy số thứ tự tăng dần cho tên test (thread-safe)
     */
    protected int getNextRunNumber() {
        return globalRunCount.incrementAndGet();
    }

    /* =========================
     * REPORT
     * ========================= */
    @BeforeSuite
    public void setupReport() {
        config = ConfigReader.getInstance();

        ExtentSparkReporter spark =
                new ExtentSparkReporter(config.getReportPath());

        spark.config().setDocumentTitle("Automation Report");
        spark.config().setReportName("Selenium Test Results");
        spark.config().setTimelineEnabled(true);
        // Sort test: OLDEST_FIRST để TỔNG KẾT (startTime=epoch 0) hiện đầu tiên
        spark.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");

        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    /* =========================
     * SETUP
     * ========================= */
    @BeforeMethod
    public void setup(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        if (desc != null && !desc.isEmpty()) {
            testName = testName + " - " + desc;
        }
        test = extent.createTest(testName);

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.get(getBaseUrl());

        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    protected String getBaseUrl() {
        return "https://ci-promotion.frt.vn/manager-promotion-list";
    }

    /* =========================
     * TEARDOWN
     * ========================= */
    @AfterMethod
    public void teardown(ITestResult result) {

        if (result.getStatus() == ITestResult.SUCCESS) {
            attachScreenshot("✅ Test PASSED");
            // Force ExtentReports status = PASS khi TestNG PASS
            // (tránh sidebar hiện fail do node con dùng .fail() cho soft assertions)
            test.pass("✅ TestNG Result: PASSED");
        } else if (result.getStatus() == ITestResult.FAILURE) {
            attachScreenshot("❌ Test FAILED");
            if (result.getThrowable() != null) {
                test.fail(result.getThrowable());
            }
        } else {
            test.skip("⏭️ Test SKIPPED");
        }

        if (driver != null) {
          // driver.quit();// bật tắt chrome
        }
    }

    @AfterSuite
    public void flushReport(org.testng.ITestContext context) {
        // Tổng kết hiện TRÊN ĐẦU sidebar report
        int total = context.getAllTestMethods().length;
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;

        String passIcon = failed == 0 ? "🎉" : "⚠️";
        String summaryTitle = passIcon + " TỔNG KẾT: " + total + " TC | ✅" + passed + " Pass | ❌" + failed + " Fail | " + String.format("%.0f%%", passRate);

        com.aventstack.extentreports.ExtentTest summary = extent.createTest(summaryTitle);
        summary.info("📋 Total: " + total + " | ✅ Passed: " + passed + " | ❌ Failed: " + failed + " | ⏭️ Skipped: " + skipped);
        summary.info("📊 Pass Rate: " + String.format("%.1f%%", passRate));
        if (failed == 0) {
            summary.pass("🎉 ALL TESTS PASSED!");
        } else {
            summary.fail("⚠️ " + failed + " test(s) FAILED");
        }

        // Set startTime = epoch 0 để summary hiện ĐẦU TIÊN trên sidebar
        summary.getModel().setStartTime(new java.util.Date(0));
        summary.getModel().setEndTime(new java.util.Date(0));

        extent.flush();
    }

    /* =====================================================
     * SCREENSHOT
     * ===================================================== */
    protected void attachScreenshot(String message) {
        try {
            File src = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE);

            String dir = "test-output/screenshots";
            new File(dir).mkdirs();

            String fileName = System.currentTimeMillis() + ".png";
            File dest = new File(dir + "/" + fileName);
            FileUtils.copyFile(src, dest);

            test.info(message,
                    MediaEntityBuilder.createScreenCaptureFromPath(
                            "screenshots/" + fileName).build());

        } catch (Exception e) {
            test.warning("Cannot attach screenshot: " + e.getMessage());
        }
    }

    /* =====================================================
     * COMMON METHODS - ANT DESIGN SELECT 🔥
     * ===================================================== */

    /**
     * Mở dropdown Ant Design Select
     */
    protected void openAntDropdown(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
    }

    /**
     * Click option Ant Select theo text (contains)
     */
    protected void clickAntOptionByContains(String text) {
        By option = By.xpath(
                "//div[contains(@class,'ant-select-item-option-content')" +
                " and contains(normalize-space(),'" + text + "')]"
        );

        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    /**
     * Click option Ant Select theo title
     */
    protected void clickAntOptionByTitle(String title) {
        By option = By.xpath("//div[@title='" + title + "']");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }
}
