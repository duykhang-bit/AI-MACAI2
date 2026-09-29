package testcases.uat.Promotion;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;

import base.BaseTest1;
import listeners.TestListener;

@Listeners(TestListener.class)
public class TC4 extends BaseTest1 {

        @Override
        protected String getBaseUrl() {
                return "https://uat-promotion.frt.vn/manager-promotion-list";
        }

        // FLOW CTKM ĐƠN HÀNG
        @Test(priority = 1, description = "FLOW 2 - CTKM ĐƠN HÀNG")
        public void testCreatePromotionFlowNhomCTKMDonhang() throws InterruptedException {

                ExtentTest tc01 = test.createNode("TC01 - Login hệ thống");
                tc01.info("Nhập username");
                WebElement userNameBox2 = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.UserNameOrEmailAddress")));
                userNameBox2.clear();
                userNameBox2.sendKeys("giant");

                tc01.info("Nhập password");
                WebElement passwordBox2 = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.Password")));
                passwordBox2.clear();
                passwordBox2.sendKeys("********");

                tc01.info("Click đăng nhập");
                driver.findElement(By.id("kt_login_signin_submit")).click();
                wait.until(ExpectedConditions.urlContains("manager"));
                tc01.pass("Login thành công");

                ExtentTest tc02 = test.createNode("TC02 - Tạo CTKM");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'actionHeader')]"))).click();

                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
                String promoName2 = "Nhóm hàng giảm giá_" + LocalDateTime.now().format(formatter2);/// NOTE NAME
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.id("promotiongeneralinfor_name"))).sendKeys(promoName2);
                tc02.pass("Tạo CTKM OK: " + promoName2);

                ExtentTest tc03 = test.createNode("TC03 - Thời gian");
                String today2 = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-picker-range')]"))).click();
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//td[@title='" + today2 + "']"))).click();
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//td[@title='" + today2 + "']"))).click();
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-selector')]"))).click();
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[text()='Zalo']"))).click();
                tc03.pass("Thời gian OK");

                ExtentTest tc04 = test.createNode("TC04 - Chiến dịch");
                WebElement campaignBox2 = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.id("promotiongeneralinfor_campaignId")));
                campaignBox2.sendKeys("CD-1225-073");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option') and contains(.,'CD-1225-073')]")))
                                .click();
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(),'Tiếp theo')]"))).click();
                tc04.pass("Chiến dịch OK");

                ExtentTest tc05 = test.createNode("TC05 - Nhóm CTKM: Đơn hàng");
                WebElement nhom2 = wait.until(
                                ExpectedConditions.elementToBeClickable(By.id("promotionClassId")));
                nhom2.sendKeys("Đơn hàng");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Đơn hàng')]")))
                                .click();
                tc05.pass("Nhóm CTKM OK");

                ExtentTest tc06 = test.createNode("TC06 - Loại CTKM: Tổng Tiền Đơn Hàng Theo Sản phẩm");
                WebElement loai2 = wait.until(ExpectedConditions.elementToBeClickable(By.id("promotionTypeID")));
                loai2.click();
                loai2.sendKeys("Tổng Tiền Đơn Hàng Theo Sản phẩm");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Tổng Tiền Đơn Hàng Theo Sản phẩm')]")))
                                .click();
                tc06.pass("Loại CTKM OK");

                ExtentTest tc07 = test.createNode("TC07 - Khu vực hiển thị");
                By displayArea2 = By.xpath(
                                "//label[contains(text(),'Khu vực hiển thị khuyến mãi')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(displayArea2)).click();
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Khuyến mãi có liên quan đến giá trị bill')]")))
                                .click();
                tc07.pass("Khu vực hiển thị OK");

                ExtentTest tc08 = test.createNode("TC08 - Chi phí phòng ban");
                WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement uploadInput2 = wait2.until(
                                ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file']")));
                Path imagePath2 = Paths.get(System.getProperty("user.dir"),
                                "src", "test", "resources", "images", "upload.png");
                uploadInput2.sendKeys(imagePath2.toAbsolutePath().toString());

                By dropdownCPPB2 = By.xpath(
                                "//label[contains(text(),'Chi phí phòng ban')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownCPPB2)).click();
                By optionCPPB2 = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Phòng kiểm soát nghành hàng / KSNH']");
                wait.until(ExpectedConditions.elementToBeClickable(optionCPPB2)).click();
                tc08.pass("Chi phí phòng ban OK");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(),'Tiếp theo')]"))).click();

                // Step 3 - Thiết lập điều kiện
                ExtentTest tc09 = test.createNode("TC09 - Điều kiện đầu vào: Tổng đơn hàng");

                // Click dropdown "Chọn điều kiện"
                By dropdownDK = By.xpath(
                                "(//div[contains(@class,'ant-select-selector')]//span[contains(text(),'Chọn điều kiện') or contains(text(),'Chọn điểu kiện')])[1]/ancestor::div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownDK)).click();
                // Chọn "Tổng đơn hàng"
                By optionDK = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "contains(.,'Tổng đơn hàng')]");
                wait.until(ExpectedConditions.elementToBeClickable(optionDK)).click();
                tc09.pass("Điều kiện đầu vào OK");

                ExtentTest tc10 = test.createNode("TC10 - Phép toán: Nhỏ hơn hoặc bằng");
                // Click dropdown Phép toán
                By dropdownPT2 = By.xpath(
                                "(//div[contains(@class,'ant-select-selector')]//span[contains(text(),'Chọn phép') or contains(text(),'phép toán')])[1]/ancestor::div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownPT2)).click();
                By optionPT2 = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "contains(.,'Nhỏ hơn hoặc bằng')]");
                wait.until(ExpectedConditions.elementToBeClickable(optionPT2)).click();
                tc10.pass("Phép toán OK");

                ExtentTest tc11 = test.createNode("TC11 - Giá trị: 300000");
                WebElement giaTriInput = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//input[@placeholder='Giá trị' or @placeholder='Nhập giá trị' or @placeholder='Nhập số tiền']")));
                giaTriInput.sendKeys("300000");
                tc11.pass("Giá trị OK");

                // Thêm điều kiện vào: Mã Nhóm hàng
                ExtentTest tc11b = test.createNode("TC11b - Thêm điều kiện: Mã Nhóm hàng");

                // Click "+ Thêm điều kiện vào"
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(),'Thêm điều kiện vào') or contains(text(),'Thêm điều kiện và')]"))).click();

                // Click dropdown dòng mới
                Thread.sleep(1000);
                By dropdownLoai2 = By.xpath(
                                "(//div[contains(@class,'ant-select-selector')]//span[contains(text(),'Chọn điều kiện') or contains(text(),'Chọn điểu kiện')])[last()]/ancestor::div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownLoai2)).click();

                // Gõ "Mã Nhóm" rồi nhấn ENTER để chọn
                Thread.sleep(500);
                WebElement activeSearchInput = driver.switchTo().activeElement();
                activeSearchInput.sendKeys("Mã Nhóm");
                Thread.sleep(1000);
                activeSearchInput.sendKeys(org.openqa.selenium.Keys.ENTER);

                // Chọn Phép toán: Chứa - dùng ID trực tiếp
                Thread.sleep(1000);
                // Click vào select element bằng ID
                WebElement phepToanSelect = wait.until(ExpectedConditions.elementToBeClickable(
                                By.id("includeInput_0_groupCode_1_operatorId")));
                phepToanSelect.click();
                Thread.sleep(500);
                phepToanSelect.sendKeys("Chứa");
                Thread.sleep(1000);
                phepToanSelect.sendKeys(org.openqa.selenium.Keys.ENTER);

                // Click nút "Sửa" bên cạnh ô "Chọn nhóm hàng" để mở popup
                Thread.sleep(1000);
                // Tìm input placeholder="Chọn nhóm hàng" rồi click addon "Sửa" bên cạnh
                WebElement maNhomBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//input[@placeholder='Chọn nhóm hàng']/following-sibling::span[contains(@class,'ant-input-group-addon')] | //input[@placeholder='Chọn nhóm hàng']/parent::*//span[contains(@class,'ant-input-group-addon')]")));
                maNhomBtn.click();

                // Đợi popup xuất hiện
                Thread.sleep(1500);

                // Search "chống nắng toàn thân" trong popup bên trái
                WebElement searchNhom = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("(//div[contains(@class,'ant-modal')]//input[contains(@placeholder,'Tìm') or contains(@placeholder,'tìm')])[1]")));
                searchNhom.clear();
                searchNhom.sendKeys("chống nắng toàn thân");
                Thread.sleep(1500);

                // Click dấu + bên cạnh "CHỐNG NẮNG TOÀN THÂN"
                WebElement addNhom = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//tr[contains(.,'CHỐNG NẮNG TOÀN THÂN')]//span[contains(@class,'anticon-plus') or contains(@class,'anticon-plus-circle')]")));
                addNhom.click();
                Thread.sleep(500);

                // Click OK
                WebElement btnOKNhom = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//button[.//span[text()='OK']]")));
                btnOKNhom.click();
                Thread.sleep(500);

                tc11b.pass("Thêm Mã Nhóm hàng: 1880311b-e95a-e98d-d715-3a190984608c OK");

                // Điều kiện đầu ra
                ExtentTest tc12 = test.createNode("TC12 - Loại đầu ra: Phiếu Mua Hàng");
                // Click dropdown Loại (dòng đầu ra)
                By dropdownLoaiOut = By.xpath(
                                "(//div[contains(@class,'ant-select-selector')]//span[contains(text(),'Chọn loại') or contains(text(),'Chọn Loại')])[1]/ancestor::div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownLoaiOut)).click();
                By optionLoaiOut = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "contains(.,'Phiếu Mua Hàng')]");
                wait.until(ExpectedConditions.elementToBeClickable(optionLoaiOut)).click();
                tc12.pass("Loại đầu ra OK");

                ExtentTest tc13 = test.createNode("TC13 - Phép toán đầu ra: Bằng");
                By dropdownPTOut = By.xpath(
                                "(//div[contains(@class,'ant-select-selector')]//span[contains(text(),'Chọn phép') or contains(text(),'phép toán')])[1]/ancestor::div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownPTOut)).click();
                By optionPTOut = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Bằng']");
                wait.until(ExpectedConditions.elementToBeClickable(optionPTOut)).click();
                tc13.pass("Phép toán đầu ra OK");

                ExtentTest tc14 = test.createNode("TC14 - Giá trị PMH: 216216216");
                // Click nút "Sửa" ở dòng Điều kiện đầu ra (Chọn phiếu mua hàng)
                WebElement giaTriBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//input[@placeholder='Chọn phiếu mua hàng']/following-sibling::span[contains(@class,'ant-input-group-addon')] | //input[contains(@placeholder,'phiếu mua hàng')]/parent::*//span[contains(@class,'addon')]")));
                giaTriBtn.click();

                // Đợi popup xuất hiện
                Thread.sleep(2000);

                // Nhập mã vào ô tìm kiếm bên trái
                WebElement searchPMH = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//input[@placeholder='Tìm kiếm mã, tên' or contains(@placeholder,'Tìm')]")));
                searchPMH.clear();
                searchPMH.sendKeys("216216216");
                Thread.sleep(2000);

                // Click dấu + bên cạnh kết quả
                WebElement addPMH = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//tr[contains(.,'216216216')]//span[contains(@class,'anticon-plus')] | //div[contains(@class,'ant-modal')]//tr[contains(.,'216216216')]//td[1]")));
                addPMH.click();
                Thread.sleep(1000);

                // Click OK
                WebElement btnOK = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//button[.//span[text()='OK']]")));
                btnOK.click();
                Thread.sleep(1000);
                tc14.pass("Giá trị PMH 216216216 OK");

                ExtentTest tc15 = test.createNode("TC15 - Số lượng: 1");
                WebElement soLuong = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//input[@placeholder='Số lượng' or contains(@placeholder,'số lượng')]")));
                soLuong.clear();
                soLuong.sendKeys("1");
                tc15.pass("Số lượng OK");

                // Xác nhận
                WebElement btnXACNHAN2 = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//button[.//span[normalize-space()='Xác nhận']]")));
                btnXACNHAN2.click();

                By btnDongY2 = By.xpath(
                                "//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='Đồng ý']]");
                wait.until(ExpectedConditions.elementToBeClickable(btnDongY2)).click();

                test.pass("✅ Hoàn thành tạo CTKM Đơn Hàng - Tổng Tiền Đơn Hàng");
        }

}

// thứ tự run test
// 1. @BeforeSuite (setupReport)
// 2. @BeforeMethod (setup)
// 3. TestListener.onTestStart()
// 4. @Test
// 5. TestListener.onTestSuccess/Failure()
// 6. @AfterMethod (teardown)
// 7. @AfterSuite (flushReport)
