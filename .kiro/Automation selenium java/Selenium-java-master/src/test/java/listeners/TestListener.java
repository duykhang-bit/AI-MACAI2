package listeners;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import utils.ExtentManager;

public class TestListener implements ITestListener {

    private static ExtentReports extent = ExtentManager.getExtent();
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static ThreadLocal<Long> startTime = new ThreadLocal<>();

    public void onTestStart(ITestResult result) {
        startTime.set(System.currentTimeMillis());
        // Không tạo ExtentTest mới ở đây — BaseTest1 đã tạo rồi
    }

    public void onTestSuccess(ITestResult result) {
        // BaseTest1.teardown() đã xử lý screenshot + log
    }

    public void onTestFailure(ITestResult result) {
        // BaseTest1.teardown() đã xử lý screenshot + log lỗi
    }

    public void onTestSkipped(ITestResult result) {
        ExtentTest extentTest = test.get();
        extentTest.skip("⏭️ TEST SKIPPED");
        extentTest.skip("Skip Reason: " + (result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown"));
        extentTest.info("Test Skipped at: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
    }

    public void onStart(ITestContext context) {
        // Thông tin về test suite
        extent.setSystemInfo("Test Suite Name", context.getSuite().getName());
        extent.setSystemInfo("Total Tests", String.valueOf(context.getSuite().getAllMethods().size()));
    }

    public void onFinish(ITestContext context) {
        int total = context.getAllTestMethods().length;
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;

        // Tạo node tổng kết hiện trên sidebar
        ExtentTest summary = extent.createTest("📊 TỔNG KẾT: " + total + " TC | ✅ " + passed + " Pass | ❌ " + failed + " Fail");
        summary.info("📋 Total Test Cases: " + total);
        summary.info("✅ Passed: " + passed);
        summary.info("❌ Failed: " + failed);
        summary.info("⏭️ Skipped: " + skipped);
        summary.info("📊 Pass Rate: " + String.format("%.1f", passRate) + "%");
        summary.info("⏱️ Duration: " + ((context.getEndDate().getTime() - context.getStartDate().getTime()) / 1000) + "s");
        
        if (failed == 0) {
            summary.pass("🎉 ALL TESTS PASSED!");
        } else {
            summary.fail("⚠️ " + failed + " test(s) FAILED — cần check lại!");
        }

        // System info
        extent.setSystemInfo("Total Tests", String.valueOf(total));
        extent.setSystemInfo("Passed Tests", String.valueOf(passed));
        extent.setSystemInfo("Failed Tests", String.valueOf(failed));
        extent.setSystemInfo("Skipped Tests", String.valueOf(skipped));
        extent.setSystemInfo("Pass Rate", String.format("%.1f%%", passRate));
        
        extent.flush();
    }
}
