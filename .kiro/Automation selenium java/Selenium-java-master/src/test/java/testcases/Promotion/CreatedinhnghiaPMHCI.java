package testcases.Promotion;

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
public class CreatedinhnghiaPMHCI extends BaseTest1 {

        @Override
        protected String getBaseUrl() {
                return "https://ci-promotion.frt.vn/manager-promotion-list";
        }

        @Test(priority = 1, description = "FLOW - Tạo Định nghĩa phiếu mua hàng")
        public void testCreateCampaignFlow() throws InterruptedException {

                JavascriptExecutor js = (JavascriptExecutor) driver;

                /*
                 * =========================
                 * TC01 - LOGIN
                 * =========================
                 */
                ExtentTest tc01 = test.createNode("TC01 - Login");

                WebElement username = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.UserNameOrEmailAddress")));
                username.sendKeys("giant");

                WebElement password = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.Password")));
                password.sendKeys("********");

                driver.findElement(By.id("kt_login_signin_submit")).click();

                wait.until(ExpectedConditions.urlContains("manager"));
                Assert.assertTrue(driver.getCurrentUrl().contains("manager"));
                tc01.pass("Login thành công");

                /*
                 * =========================
                 * TC02 - MENU CHIẾN DỊCH
                 * =========================
                 */
                ExtentTest tc02 = test.createNode("TC02 - Menu định nghĩa phiếu mua hàng");

                By voucherMenu = By.xpath(
                                "//span[normalize-space()='Voucher']/ancestor::li[contains(@class,'ant-menu-submenu')]");
                wait.until(ExpectedConditions.elementToBeClickable(voucherMenu)).click();
                tc02.pass("Vào Menu định nghĩa phiếu mua hàng");

                // Chọn Định nghĩa phiếu mua hàng
                By dinhNghiaPhiemuaHangMenu = By.xpath(
                                "//li[contains(@data-menu-id,'manager-item-voucher')]");
                WebElement dinhNghiaPhiemuaHangMenu1 = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(dinhNghiaPhiemuaHangMenu));
                                dinhNghiaPhiemuaHangMenu1.click();

                /*
                 * =========================
                 * TC03 - TẠO CHIẾN DỊCH
                 * =========================
                 */
                //  nhập Loại
                ExtentTest tc03 = test.createNode("TC03 - Tạo chiến dịch");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'actionHeader')]"))).click();

                WebElement txtName = wait.until(
                                ExpectedConditions.elementToBeClickable(By.id("name")));
                txtName.clear();
                txtName.sendKeys("AT");
                tc03.pass("Nhập tên chiến dịch");

                /*
                 * =========================
                 * TC04 - CHỌN NGÀY (ANT DESIGN)
                 * =========================
                 */
                ExtentTest tc04 = test.createNode("TC04 - Chọn ngày");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                LocalDate startDate = LocalDate.of(2026, 1, 16);
                LocalDate endDate = startDate.plusDays(1);

                String start = startDate.format(formatter);
                String end = endDate.format(formatter);

                // ===== START DATE =====
                WebElement startInput = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//input[@placeholder='Ngày bắt đầu']")));

                startInput.click();
                startInput.sendKeys(Keys.CONTROL + "a");
                startInput.sendKeys(start);
                startInput.sendKeys(Keys.ENTER); // commit AntD

                // ===== END DATE =====
                WebElement endInput = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//input[@placeholder='Ngày kết thúc']")));

                endInput.click();
                endInput.sendKeys(Keys.CONTROL + "a");
                endInput.sendKeys(end);
                endInput.sendKeys(Keys.ESCAPE); // 🔥 BẮT BUỘC với Ant Design

                // VERIFY
                Assert.assertEquals(startInput.getAttribute("value"), start);
                Assert.assertEquals(endInput.getAttribute("value"), end);

                tc04.pass("Chọn ngày thành công");

                /*
                 * =========================
                 * TC05 - SUBMIT
                 * 
                 * =========================
                 */

                driver.findElement(By.tagName("body"))
                                .sendKeys(Keys.ENTER);

                ExtentTest tc05 = test.createNode("TC05 - Submit");

                // đợi modal render xong
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("div.ant-modal")));

                By btnCreate = By.cssSelector(
                                "div.ant-modal-footer button.ant-btn-primary");

                WebElement btn = wait.until(
                                ExpectedConditions.presenceOfElementLocated(btnCreate));

                js.executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                Thread.sleep(200);
                js.executeScript("arguments[0].click();", btn);

                tc05.pass("Click Tạo mới thành công");
        }
}
