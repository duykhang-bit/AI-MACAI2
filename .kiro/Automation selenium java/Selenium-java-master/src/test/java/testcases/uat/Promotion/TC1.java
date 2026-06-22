package testcases.uat.Promotion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;

import base.BaseTest1;
import listeners.TestListener;

@Listeners(TestListener.class)
public class TC1 extends BaseTest1 {

    @Override
    protected String getBaseUrl() {
        return "https://uat-promotion.frt.vn/manager-promotion-list";
    }

    @Test(priority = 1, description = "FLOW - Tạo chiến dịch")
    public void testCreateCampaignFlow() throws InterruptedException {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        /* =========================
         * TC01 - LOGIN
         * ========================= */
        ExtentTest tc01 = test.createNode("TC01 - Login");

        WebElement username = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.name("LoginInput.UserNameOrEmailAddress")
                )
        );
        username.sendKeys("giant");

        WebElement password = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.name("LoginInput.Password")
                )
        );
        password.sendKeys("********");

        driver.findElement(By.id("kt_login_signin_submit")).click();

        wait.until(ExpectedConditions.urlContains("manager"));
        Assert.assertTrue(driver.getCurrentUrl().contains("manager"));
        tc01.pass("Login thành công");

        /* =========================
         * TC02 - MENU CHIẾN DỊCH
         * ========================= */
        ExtentTest tc02 = test.createNode("TC02 - Menu Chiến dịch");

        By menuCampaign = By.xpath(
                "//li[contains(@class,'ant-menu-item') " +
                "and contains(@data-menu-id,'manager-campaign')]"
        );

        wait.until(ExpectedConditions.elementToBeClickable(menuCampaign)).click();
        tc02.pass("Vào menu Chiến dịch");

        /* =========================
         * TC03 - TẠO CHIẾN DỊCH
         * ========================= */
        ExtentTest tc03 = test.createNode("TC03 - Tạo chiến dịch");

        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'actionHeader')]")
        )).click();

        WebElement txtName = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("name"))
        );
        txtName.clear();
        txtName.sendKeys("AT");
        tc03.pass("Nhập tên chiến dịch");

        /* =========================
         * TC04 - CHỌN NGÀY (ANT DESIGN)
         * Dùng sendKeys trực tiếp vào input sau khi click mở picker
         * ========================= */
        ExtentTest tc04 = test.createNode("TC04 - Chọn ngày");

        // Đợi modal render hoàn toàn (mask biến mất, content visible)
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.ant-modal-content")
        ));
        Thread.sleep(500); // thêm buffer cho animation AntD modal

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // FIX: startDate = ngày mai, endDate = ngày mốt — luôn là tương lai
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate   = startDate.plusDays(1);

        String start = startDate.format(formatter);
        String end   = endDate.format(formatter);

        // Đây là 2 DatePicker ĐỘC LẬP (không phải RangePicker)
        // Click trực tiếp vào từng input bằng placeholder

        // ===== START DATE =====
        WebElement startInput = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'ant-modal-content')]//input[@placeholder='Ngày bắt đầu']")
                )
        );
        js.executeScript("arguments[0].click();", startInput);
        Thread.sleep(300);
        // Clear và type ngày bằng JS để bypass AntD picker behavior
        js.executeScript(
            "arguments[0].value = '';" +
            "arguments[0].focus();",
            startInput
        );
        startInput.sendKeys(start);
        Thread.sleep(200);
        startInput.sendKeys(Keys.ENTER);
        Thread.sleep(400);

        // Đóng calendar nếu mở
        js.executeScript("document.activeElement.blur();");
        Thread.sleep(200);

        // ===== END DATE =====
        WebElement endInput = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'ant-modal-content')]//input[@placeholder='Ngày kết thúc']")
                )
        );
        js.executeScript("arguments[0].click();", endInput);
        Thread.sleep(300);
        js.executeScript(
            "arguments[0].value = '';" +
            "arguments[0].focus();",
            endInput
        );
        endInput.sendKeys(end);
        Thread.sleep(200);
        endInput.sendKeys(Keys.ENTER);
        Thread.sleep(400);

        // Đóng calendar
        js.executeScript("document.activeElement.blur();");
        Thread.sleep(300);

        tc04.pass("Chọn ngày thành công: " + start + " → " + end);

        /* =========================
         * TC05 - SUBMIT
         * Modal "Tạo chiến dịch" đang mở — click nút "Tạo mới" trong modal footer
         * ========================= */
        ExtentTest tc05 = test.createNode("TC05 - Click Tạo mới");

        // Nút "Tạo mới" nằm trong modal footer (KHÔNG phải ngoài modal)
        By btnTaoMoi = By.xpath(
                "//div[contains(@class,'ant-modal-footer')]" +
                "//button[contains(@class,'ant-btn-primary')]"
        );

        WebElement btnSubmit = wait.until(
                ExpectedConditions.elementToBeClickable(btnTaoMoi)
        );
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", btnSubmit);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", btnSubmit);

        // Đợi modal đóng sau khi tạo thành công
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("div.ant-modal")
        ));

        tc05.pass("Click Tạo mới và tạo chiến dịch thành công");
    }
}
