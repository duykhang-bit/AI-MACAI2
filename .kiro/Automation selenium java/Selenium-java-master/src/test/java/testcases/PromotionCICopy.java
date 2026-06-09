package testcases;

import com.aventstack.extentreports.ExtentTest;

import base.BaseTest1;
import listeners.TestListener;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;


@Listeners(TestListener.class)
public class PromotionCICopy extends BaseTest1 {

        @Override
        protected String getBaseUrl() {
                return "https://ci-promotion.frt.vn/manager-promotion-list";
        }

        // =================================================
        // FULL FLOW - TẠO CTKM (TC01 → TC06)
        // =================================================
        @Test(priority = 1, description = "FLOW 1 - CTKM SẢN PHẨM")

        public void testCreatePromotionFlowNhomCTKMSanpham() {

                /*
                 * =========================
                 * TC01 - LOGIN
                 * =========================
                 */
                ExtentTest tc01 = test.createNode("TC01 - Login hệ thống");

                tc01.info("Nhập username");
                WebElement userNameBox = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.UserNameOrEmailAddress")));
                userNameBox.clear();
                userNameBox.sendKeys("giant");

                tc01.info("Nhập password");
                WebElement passwordBox = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.Password")));
                passwordBox.clear();
                passwordBox.sendKeys("********");

                tc01.info("Click đăng nhập");
                driver.findElement(By.id("kt_login_signin_submit")).click();

                wait.until(ExpectedConditions.urlContains("manager"));
                Assert.assertTrue(driver.getCurrentUrl().contains("manager"),
                                "Login FAILED");

                tc01.pass("Login thành công");

                /*
                 * =========================
                 * TC02 - TẠO CTKM
                 * =========================
                 */
                ExtentTest tc02 = test.createNode("TC02 - Tạo CTKM");

                tc02.info("Click button tạo CTKM");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'actionHeader')]"))).click();

                tc02.info("Nhập tên CTKM");


           

// Generate tên CTKM
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
                String promoName = "Automation Test " + LocalDateTime.now().format(formatter);

                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.id("promotiongeneralinfor_name")
                )).sendKeys(promoName);

                tc02.info("Nhập tên CTKM: " + promoName);

                // wait.until(ExpectedConditions.visibilityOfElementLocated(
                //                 By.id("promotiongeneralinfor_name")))
                //                 .sendKeys("Automation Test 31/12 ");

                // tc02.info("Nhập ghi chú");
                // wait.until(ExpectedConditions.visibilityOfElementLocated(
                //                 By.id("promotiongeneralinfor_remark")))
                //                 .sendKeys("AT ");

                tc02.pass("Tạo CTKM OK");

                /*
                 * =========================
                 * TC03 - THỜI GIAN + PHƯƠNG THỨC
                 * =========================
                 */
                    // Lấy ngày hiện tại
                    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                    ExtentTest tc03 = test.createNode("TC03 - Chọn thời gian & phương thức");
                    
                    // Click range picker
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[contains(@class,'ant-picker-range')]"))).click();
                    
                    // Click ngày bắt đầu
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//td[@title='" + today + "']"))).click();
                    
                    // Click ngày kết thúc (cùng ngày)
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//td[@title='" + today + "']"))).click();
                    
                    // Chọn phương thức
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[contains(@class,'ant-select-selector')]"))).click();
                    
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[text()='Zalo']"))).click();
                    
                    tc03.pass("Chọn thời gian & phương thức OK");

                /*
                 * =========================
                 * TC04 - THUỘC CHIẾN DỊCH
                 * =========================
                 */
                ExtentTest tc04 = test.createNode("TC04 - Thuộc chiến dịch");

                WebElement campaignBox = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.id("promotiongeneralinfor_campaignId")));
                campaignBox.sendKeys("CD-1225-237");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option') and contains(.,'CD-1225-237')]")))
                                .click();

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(),'Tiếp theo')]"))).click();

                tc04.pass("Hoàn tất màn 1");

                /*
                 * =========================
                 * TC05 - NHÓM CTKM
                 * =========================
                 */
                ExtentTest tc05 = test.createNode("TC05 - Nhóm CTKM");

                WebElement nhom = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.id("promotionClassId")));
                nhom.sendKeys("Sản Phẩm");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Sản phẩm')]")))
                                .click();

                tc05.pass("Chọn nhóm CTKM OK");

                /*
                 * =========================
                 * TC06 - LOẠI CTKM
                 * =========================
                 */
                ExtentTest tc06 = test.createNode("TC06 - Loại CTKM");

                WebElement loai = wait.until(ExpectedConditions.elementToBeClickable(
                                By.id("promotionTypeID")));
                loai.click();
                loai.sendKeys("Giảm giá sản phẩm");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Giảm giá sản phẩm')]")))
                                .click();

                tc06.pass("Chọn loại CTKM OK");

                /*
                 * =========================
                 * TC07 - KHU VỰC HIỂN THỊ
                 * =========================
                 */
                ExtentTest tc07 = test.createNode("TC07 - Khu vực hiển thị khuyến mãi");
                // cách bắt ant design element dropdown

                By displayAreaDropdownBy = By.xpath(
                                "//label[contains(text(),'Khu vực hiển thị khuyến mãi')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//div[contains(@class,'ant-select-selector')]");

                WebElement displayAreaDropdown = wait.until(
                                ExpectedConditions.elementToBeClickable(displayAreaDropdownBy));

                displayAreaDropdown.click();

                // ✅ CLICK OPTION SAU KHI MỞ DROPDOWN
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Khuyến mãi sản phẩm chính')]")))
                                .click();

                tc07.pass("Chọn khu vực hiển thị khuyến mãi OK");


                 // UPLOAD ẢNH
                 WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                 // Luôn tìm input file (dù UI đã đổi)
                 WebElement uploadInput = wait.until(
                                 ExpectedConditions.presenceOfElementLocated(
                                                 By.xpath("//input[@type='file']")));
 
                 // Path ảnh trong project
                 Path imagePath = Paths.get(
                                 System.getProperty("user.dir"),
                                 "src", "test", "resources", "images", "upload.png");
 
                 // Upload
                 uploadInput.sendKeys(imagePath.toAbsolutePath().toString());

                ExtentTest tc08 = test.createNode("TC08 - Chi phí phòng ban");

                // 1️⃣ CLICK DROPDOWN
                By dropdownCPPB = By.xpath(
                                "//label[contains(text(),'Chi phí phòng ban')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//div[contains(@class,'ant-select-selector')]");

                WebElement dropdownElement = wait.until(
                                ExpectedConditions.elementToBeClickable(dropdownCPPB));
                dropdownElement.click();

                // 2️⃣ CLICK OPTION
                By optionCPPB = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]"
                                                +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Phòng kiểm soát nghành hàng / KSNH']");

                wait.until(ExpectedConditions.elementToBeClickable(optionCPPB)).click();

                tc08.pass("Chọn Chi phí phòng ban OK");

                // 3️⃣ NEXT
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(),'Tiếp theo')]"))).click();

                // Step 3
                ExtentTest tc09 = test.createNode("Loại đầu vào");
                By dropdownLDV = By.xpath(
                                "//div[contains(@class,'ant-col ant-col-3')]" +
                                                "//div[contains(@class,'ant-select-selector')]");
                WebElement dropdownElement1 = wait.until(
                                ExpectedConditions.elementToBeClickable(dropdownLDV));
                dropdownElement1.click();

                By optionLDVBy = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]"
                                                +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Mã Ngành hàng']");

                wait.until(ExpectedConditions.elementToBeClickable(optionLDVBy)).click();

                tc09.pass("Loại đầu vào");

                ExtentTest tc10 = test.createNode("Phép toán");
                By dropdownPT = By.xpath(
                                "//div[contains(@class,'ant-col ant-col-2')]" +
                                                "//div[contains(@class,'ant-select-selector')]");
                WebElement dropdownElement2 = wait.until(
                                ExpectedConditions.elementToBeClickable(dropdownPT));
                dropdownElement2.click();

                By optionPTBy = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]"
                                                +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Bằng']");

                wait.until(ExpectedConditions.elementToBeClickable(optionPTBy)).click();

                tc10.pass("Phép toán  OK");

                ExtentTest tc11 = test.createNode("Giá trị");

                // CLICK DROPDOWN
                By dropdownGT = By.id(
                                "inputID"// div[contains(@class,'ant-select')]"
                );

                WebElement dropdownElement4 = wait.until(
                                ExpectedConditions.elementToBeClickable(dropdownGT));
                dropdownElement4.click();

                // CHỌN OPTION TRONG TABLE
                By optionGT = By.xpath(
                                "//td[contains(@class,'ant-table-cell') and contains(@class,'ant-table-selection-column')]");

                wait.until(ExpectedConditions.elementToBeClickable(optionGT)).click();

                // CLICK OK
                WebElement btnOK = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//button//span[text()='OK']")));
                btnOK.click();

                tc11.pass("Gía tri OK");

                ExtentTest tc12 = test.createNode("Loại đầu ra");
                By dropdownLDR = By.id("includeInput_0_groupCode_0_outputQualifierId");
                WebElement dropdownElement5 = wait.until(
                                ExpectedConditions.elementToBeClickable(dropdownLDR));
                dropdownElement5.click();

                By optionLDR = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]"
                                                +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Giảm theo giá cố định']");

                wait.until(ExpectedConditions.elementToBeClickable(optionLDR)).click();

                tc12.pass("Loại đầu ra  OK");

                ExtentTest tc13 = test.createNode("TC11 - Nhập giá trị");

                By inputGiaTriBy = By.xpath(
                        "//input[@placeholder='Nhập giảm theo giá cố định']"
                    );
                    
                WebElement inputGiaTri = wait.until(
                                ExpectedConditions.elementToBeClickable(inputGiaTriBy));
                
                inputGiaTri.sendKeys("20000");

                tc13.pass("Nhập giá trị thành công");

                // CLICK XÁC NHẬN
                WebElement btnXACNHAN = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//button[.//span[normalize-space()='Xác nhận']]")));
                btnXACNHAN.click();

                // Pop_up đồng ý
                By btnDongYBy = By.xpath(
                        "//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='Đồng ý']]"
                    );
                    
                    WebElement btnDongY = wait.until(
                        ExpectedConditions.elementToBeClickable(btnDongYBy)
                    );
                    btnDongY.click();
                    

        }






        // =================================================
        // FLOW 2 - CTKM SẢN PHẨM - KÊNH TRÌNH DƯỢC VIÊN
        // =================================================
        @Test(priority = 2, description = "FLOW 2 - CTKM SẢN PHẨM - KÊNH TRÌNH DƯỢC VIÊN")
        public void testCreatePromotionFlowTrinhDuocVien() {

                /*
                 * =========================
                 * TC01 - LOGIN
                 * =========================
                 */
                ExtentTest tc01 = test.createNode("TC01 - Login hệ thống");

                tc01.info("Nhập username");
                WebElement userNameBox = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.UserNameOrEmailAddress")));
                userNameBox.clear();
                userNameBox.sendKeys("giant");

                tc01.info("Nhập password");
                WebElement passwordBox = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.Password")));
                passwordBox.clear();
                passwordBox.sendKeys("********");

                tc01.info("Click đăng nhập");
                driver.findElement(By.id("kt_login_signin_submit")).click();

                wait.until(ExpectedConditions.urlContains("manager"));
                Assert.assertTrue(driver.getCurrentUrl().contains("manager"), "Login FAILED");
                tc01.pass("Login thành công");

                /*
                 * =========================
                 * TC02 - TẠO CTKM
                 * =========================
                 */
                ExtentTest tc02 = test.createNode("TC02 - Tạo CTKM");

                tc02.info("Click button tạo CTKM");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'actionHeader')]"))).click();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
                String promoName = "AT TDV " + LocalDateTime.now().format(formatter);

                wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.id("promotiongeneralinfor_name"))).sendKeys(promoName);

                tc02.info("Nhập tên CTKM: " + promoName);
                tc02.pass("Tạo CTKM OK");

                /*
                 * =========================
                 * TC03 - THỜI GIAN + PHƯƠNG THỨC
                 * =========================
                 */
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                ExtentTest tc03 = test.createNode("TC03 - Chọn thời gian & phương thức");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-picker-range')]"))).click();

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//td[@title='" + today + "']"))).click();

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//td[@title='" + today + "']"))).click();

                // Chọn phương thức gửi thông tin
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-selector')]"))).click();

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[text()='Zalo']"))).click();

                // Đóng dropdown phương thức (multi-select không tự đóng) bằng ESC
                driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
                try { Thread.sleep(600); } catch (InterruptedException ignored) {}

                tc03.pass("Ch\u1ecdn th\u1eddi gian & ph\u01b0\u01a1ng th\u1ee9c OK");

                /*
                 * =========================
                 * TC04 - KÊNH ÁP DỤNG: TRÌNH DƯỢC VIÊN
                 * =========================
                 */
                ExtentTest tc04KenhAD = test.createNode("TC04 - K\u00eanh \u00e1p d\u1ee5ng: Tr\u00ecnh d\u01b0\u1ee3c vi\u00ean");

                // Đợi dropdown Zalo đóng hẳn
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

                // Click vào body để đóng mọi dropdown đang mở
                driver.findElement(By.tagName("body")).click();
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}

                // Đóng mọi dropdown đang mở trước khi thao tác Kênh áp dụng
                driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
                try { Thread.sleep(300); } catch (InterruptedException ie0) {}

                // Bắt dropdown "Kênh áp dụng" - full unicode escape
                By kenhApDungDropdownBy = By.xpath(
                                "//label[contains(text(),'K\u00eanh \u00e1p d\u1ee5ng')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//div[contains(@class,'ant-select-selector')]");

                // Scroll vào view
                WebElement kenhApDungDropdown = wait.until(
                                ExpectedConditions.elementToBeClickable(kenhApDungDropdownBy));
                ((org.openqa.selenium.JavascriptExecutor) driver)
                                .executeScript("arguments[0].scrollIntoView({block:'center'});", kenhApDungDropdown);
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}

                // Xóa tag "Tất cả" - sau khi xóa cần click lại để mở list
                By removeTagBy = By.xpath(
                                "//label[contains(text(),'K\u00eanh \u00e1p d\u1ee5ng')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//span[contains(@class,'ant-select-selection-item-remove')]");
                try {
                        WebElement removeAllTag = wait.until(ExpectedConditions.elementToBeClickable(removeTagBy));
                        ((org.openqa.selenium.JavascriptExecutor) driver)
                                        .executeScript("arguments[0].click();", removeAllTag);
                        try { Thread.sleep(600); } catch (InterruptedException ie1) {}
                } catch (Exception noTag) {
                        try { Thread.sleep(300); } catch (InterruptedException ie2) {}
                }

                // Click lại vào dropdown để mở list options - native click (dropdown Zalo đã đóng)
                try { Thread.sleep(300); } catch (InterruptedException ie3) {}
                kenhApDungDropdown = wait.until(ExpectedConditions.elementToBeClickable(kenhApDungDropdownBy));
                ((org.openqa.selenium.JavascriptExecutor) driver)
                                .executeScript("arguments[0].scrollIntoView({block:'center'});", kenhApDungDropdown);
                try { Thread.sleep(300); } catch (InterruptedException ie4) {}
                kenhApDungDropdown.click();
                try { Thread.sleep(800); } catch (InterruptedException ie5) {}

                // Đợi popup visible
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.id("promotiongeneralinfor_channelIds_list")));
                try { Thread.sleep(300); } catch (InterruptedException ie6) {}

                By optionListBy = By.xpath(
                                "//div[@id='promotiongeneralinfor_channelIds_list']" +
                                "//div[contains(@class,'ant-select-item-option')]");

                java.util.List<WebElement> allOptions = wait.until(
                                ExpectedConditions.presenceOfAllElementsLocatedBy(optionListBy));
                // Option cuối cùng = Trình dược viên
                WebElement trinhDuocVienOption = allOptions.get(allOptions.size() - 1);
                trinhDuocVienOption.click();
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}

                // Đóng dropdown bằng ESC
                driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}

                tc04KenhAD.pass("Ch\u1ecdn k\u00eanh \u00e1p d\u1ee5ng Tr\u00ecnh d\u01b0\u1ee3c vi\u00ean OK");

                /*
                 * =========================
                 * TC05 - THUỘC CHIẾN DỊCH
                 * =========================
                 */
                ExtentTest tc05 = test.createNode("TC05 - Thu\u1ed9c chi\u1ebfn d\u1ecbch");

                WebElement campaignBox = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.id("promotiongeneralinfor_campaignId")));
                campaignBox.click();
                campaignBox.clear();
                campaignBox.sendKeys("CD-0326-042");
                // Đợi autocomplete load - tăng lên 3s
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option') and contains(.,'CD-0326-042')]")))
                                .click();
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}

                // Click Tiếp theo và đợi URL thay đổi hoặc step 2 xuất hiện
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(),'Ti\u1ebfp theo')]"))).click();

                // Đợi navigate sang bước 2 - tăng sleep
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

                tc05.pass("Ho\u00e0n t\u1ea5t m\u00e0n 1");

                /*
                 * =========================
                 * TC06 - NHÓM CTKM
                 * =========================
                 */
                ExtentTest tc06 = test.createNode("TC06 - Nhóm CTKM");

                WebElement nhom = wait.until(
                                ExpectedConditions.elementToBeClickable(By.id("promotionClassId")));
                nhom.sendKeys("Sản Phẩm");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Sản phẩm')]")))
                                .click();

                tc06.pass("Chọn nhóm CTKM OK");

                /*
                 * =========================
                 * TC07 - LOẠI CTKM
                 * =========================
                 */
                ExtentTest tc07 = test.createNode("TC07 - Loại CTKM");

                WebElement loai = wait.until(ExpectedConditions.elementToBeClickable(
                                By.id("promotionTypeID")));
                loai.click();
                loai.sendKeys("Giảm giá sản phẩm");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Giảm giá sản phẩm')]")))
                                .click();

                tc07.pass("Chọn loại CTKM OK");

                /*
                 * =========================
                 * TC08 - KHU VỰC HIỂN THỊ
                 * =========================
                 */
                ExtentTest tc08 = test.createNode("TC08 - Khu vực hiển thị khuyến mãi");

                By displayAreaDropdownBy = By.xpath(
                                "//label[contains(text(),'Khu vực hiển thị khuyến mãi')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//div[contains(@class,'ant-select-selector')]");

                WebElement displayAreaDropdown = wait.until(
                                ExpectedConditions.elementToBeClickable(displayAreaDropdownBy));
                displayAreaDropdown.click();

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Khuyến mãi sản phẩm chính')]")))
                                .click();

                tc08.pass("Chọn khu vực hiển thị khuyến mãi OK");

                // UPLOAD ẢNH
                WebDriverWait waitUpload = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement uploadInput = waitUpload.until(
                                ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//input[@type='file']")));

                Path imagePath = Paths.get(
                                System.getProperty("user.dir"),
                                "src", "test", "resources", "images", "upload.png");
                uploadInput.sendKeys(imagePath.toAbsolutePath().toString());

                /*
                 * =========================
                 * TC09 - CHI PHÍ PHÒNG BAN
                 * =========================
                 */
                ExtentTest tc09 = test.createNode("TC09 - Chi phí phòng ban");

                By dropdownCPPB = By.xpath(
                                "//label[contains(text(),'Chi phí phòng ban')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//div[contains(@class,'ant-select-selector')]");

                WebElement dropdownCPPBEl = wait.until(
                                ExpectedConditions.elementToBeClickable(dropdownCPPB));
                dropdownCPPBEl.click();

                By optionCPPB = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Phòng kiểm soát nghành hàng / KSNH']");

                wait.until(ExpectedConditions.elementToBeClickable(optionCPPB)).click();
                tc09.pass("Chọn Chi phí phòng ban OK");

                // NEXT sang bước 3
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(),'Tiếp theo')]"))).click();

                /*
                 * =========================
                 * TC10 - LOẠI ĐẦU VÀO
                 * =========================
                 */
                ExtentTest tc10 = test.createNode("TC10 - Loại đầu vào");

                By dropdownLDV = By.xpath(
                                "//div[contains(@class,'ant-col ant-col-3')]" +
                                                "//div[contains(@class,'ant-select-selector')]");
                WebElement dropdownLDVEl = wait.until(
                                ExpectedConditions.elementToBeClickable(dropdownLDV));
                dropdownLDVEl.click();

                By optionLDV = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Mã Ngành hàng']");

                wait.until(ExpectedConditions.elementToBeClickable(optionLDV)).click();
                tc10.pass("Loại đầu vào OK");

                /*
                 * =========================
                 * TC11 - PHÉP TOÁN
                 * =========================
                 */
                ExtentTest tc11 = test.createNode("TC11 - Phép toán");

                By dropdownPT = By.xpath(
                                "//div[contains(@class,'ant-col ant-col-2')]" +
                                                "//div[contains(@class,'ant-select-selector')]");
                WebElement dropdownPTEl = wait.until(
                                ExpectedConditions.elementToBeClickable(dropdownPT));
                dropdownPTEl.click();

                By optionPT = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Bằng']");

                wait.until(ExpectedConditions.elementToBeClickable(optionPT)).click();
                tc11.pass("Phép toán OK");

                /*
                 * =========================
                 * TC12 - GIÁ TRỊ
                 * =========================
                 */
                ExtentTest tc12 = test.createNode("TC12 - Giá trị");

                WebElement dropdownGTEl = wait.until(
                                ExpectedConditions.elementToBeClickable(By.id("inputID")));
                dropdownGTEl.click();

                By optionGT = By.xpath(
                                "//td[contains(@class,'ant-table-cell') and contains(@class,'ant-table-selection-column')]");
                wait.until(ExpectedConditions.elementToBeClickable(optionGT)).click();

                WebElement btnOK = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//button//span[text()='OK']")));
                btnOK.click();
                tc12.pass("Giá trị OK");

                /*
                 * =========================
                 * TC13 - LOẠI ĐẦU RA
                 * =========================
                 */
                ExtentTest tc13 = test.createNode("TC13 - Loại đầu ra");

                WebElement dropdownLDREl = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.id("includeInput_0_groupCode_0_outputQualifierId")));
                dropdownLDREl.click();

                By optionLDR = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Giảm theo giá cố định']");

                wait.until(ExpectedConditions.elementToBeClickable(optionLDR)).click();
                tc13.pass("Loại đầu ra OK");

                /*
                 * =========================
                 * TC14 - NHẬP GIÁ TRỊ GIẢM
                 * =========================
                 */
                ExtentTest tc14 = test.createNode("TC14 - Nhập giá trị giảm");

                WebElement inputGiaTri = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//input[@placeholder='Nhập giảm theo giá cố định']")));
                inputGiaTri.sendKeys("20000");
                tc14.pass("Nhập giá trị thành công");

                // CLICK XÁC NHẬN
                WebElement btnXACNHAN = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//button[.//span[normalize-space()='Xác nhận']]")));
                btnXACNHAN.click();

                // Pop-up đồng ý
                By btnDongYBy = By.xpath(
                                "//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='Đồng ý']]");
                WebElement btnDongY = wait.until(
                                ExpectedConditions.elementToBeClickable(btnDongYBy));
                btnDongY.click();

                test.pass("FLOW Trình dược viên hoàn tất");
        }

        //FLOW CTKM ĐƠN HÀNG
        // @Test(
        //         priority = 3,
        //         description = "FLOW 3 - CTKM ĐƠN HÀNG",
        //         dependsOnMethods = "testCreatePromotionFlowTrinhDuocVien"
        //     )

        // =================================================
        // FULL FLOW - TẠO CTKM (TC01 → TC06)
        // =================================================
    
//         public void testCreatePromotionFlowNhomCTKMDonhang() {

//                 /*
//                  * =========================
//                  * TC01 - LOGIN
//                  * =========================
//                  */
//                 ExtentTest tc01 = test.createNode("TC01 - Login hệ thống");

//                 tc01.info("Nhập username");
//                 WebElement userNameBox = wait.until(
//                                 ExpectedConditions.elementToBeClickable(
//                                                 By.name("LoginInput.UserNameOrEmailAddress")));
//                 userNameBox.clear();
//                 userNameBox.sendKeys("giant");

//                 tc01.info("Nhập password");
//                 WebElement passwordBox = wait.until(
//                                 ExpectedConditions.elementToBeClickable(
//                                                 By.name("LoginInput.Password")));
//                 passwordBox.clear();
//                 passwordBox.sendKeys("********");

//                 tc01.info("Click đăng nhập");
//                 driver.findElement(By.id("kt_login_signin_submit")).click();

//                 wait.until(ExpectedConditions.urlContains("manager"));
//                 Assert.assertTrue(driver.getCurrentUrl().contains("manager"),
//                                 "Login FAILED");

//                 tc01.pass("Login thành công");

//                 /*
//                  * =========================
//                  * TC02 - TẠO CTKM
//                  * =========================
//                  */
//                 ExtentTest tc02 = test.createNode("TC02 - Tạo CTKM");

//                 tc02.info("Click button tạo CTKM");
//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//div[contains(@class,'actionHeader')]"))).click();

//                 tc02.info("Nhập tên CTKM");


           

// // Generate tên CTKM
//                 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
//                 String promoName = "AT " + LocalDateTime.now().format(formatter);

//                 wait.until(ExpectedConditions.visibilityOfElementLocated(
//                         By.id("promotiongeneralinfor_name")
//                 )).sendKeys(promoName);

//                 tc02.info("Nhập tên CTKM: " + promoName);

//                 // wait.until(ExpectedConditions.visibilityOfElementLocated(
//                 //                 By.id("promotiongeneralinfor_name")))
//                 //                 .sendKeys("Automation Test 31/12 ");

//                 tc02.info("Nhập ghi chú");
//                 wait.until(ExpectedConditions.visibilityOfElementLocated(
//                                 By.id("promotiongeneralinfor_remark")))
//                                 .sendKeys("AT ");

//                 tc02.pass("Tạo CTKM OK");

//                 /*
//                  * =========================
//                  * TC03 - THỜI GIAN + PHƯƠNG THỨC
//                  * =========================
//                  */
//                 ExtentTest tc03 = test.createNode("TC03 - Chọn thời gian & phương thức");

//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//div[contains(@class,'ant-picker-range')]"))).click();

//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//td[@title='2026-01-02']"))).click();

//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//td[@title='2026-01-02']"))).click();

//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//div[contains(@class,'ant-select-selector')]"))).click();

//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//div[contains(text(),'Zalo')]"))).click();

//                 tc03.pass("Chọn thời gian & phương thức OK");

//                 /*
//                  * =========================
//                  * TC04 - THUỘC CHIẾN DỊCH
//                  * =========================
//                  */
//                 ExtentTest tc04 = test.createNode("TC04 - Thuộc chiến dịch");

//                 WebElement campaignBox = wait.until(
//                                 ExpectedConditions.elementToBeClickable(
//                                                 By.id("promotiongeneralinfor_campaignId")));
//                 campaignBox.sendKeys("CD-1225-237");

//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//div[contains(@class,'ant-select-item-option') and contains(.,'CD-1225-237')]")))
//                                 .click();

//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//span[contains(text(),'Tiếp theo')]"))).click();

//                 tc04.pass("Hoàn tất màn 1");

//                 /*
//                  * =========================
//                  * TC05 - NHÓM CTKM
//                  * =========================
//                  */
//                 ExtentTest tc05 = test.createNode("TC05 - Nhóm CTKM");

//                 WebElement nhom1 = wait.until(
//                                 ExpectedConditions.elementToBeClickable(
//                                                 By.id("promotionClassId")));
//                 nhom1.sendKeys("Đơn hàng");

//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Đơn hàng')]")))
//                                 .click();

//                 tc05.pass("Chọn nhóm CTKM OK");

//                 /*
//                  * =========================
//                  * TC06 - LOẠI CTKM
//                  * =========================
//                  */
//                 ExtentTest tc06 = test.createNode("TC06 - Loại CTKM");

//                 WebElement loai1 = wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.id("promotionTypeID")));
//                 loai1.click();
//                 loai1.sendKeys("Tổng Tiền Đơn Hàng");

//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Tổng Tiền Đơn Hàng')]")))
//                                 .click();

//                 tc06.pass("Chọn loại CTKM OK");

//                 /*
//                  * =========================
//                  * TC07 - KHU VỰC HIỂN THỊ
//                  * =========================
//                  */
//                 ExtentTest tc07 = test.createNode("TC07 - Khu vực hiển thị khuyến mãi");
//                 // cách bắt ant design element dropdown

//                 By displayAreaDropdownBy1 = By.xpath(
//                                 "//label[contains(text(),'Khu vực hiển thị khuyến mãi')]" +
//                                                 "/ancestor::div[contains(@class,'ant-form-item')]" +
//                                                 "//div[contains(@class,'ant-select-selector')]");

//                 WebElement displayAreaDropdown1 = wait.until(
//                                 ExpectedConditions.elementToBeClickable(displayAreaDropdownBy1));

//                 displayAreaDropdown1.click();

//                 // ✅ CLICK OPTION SAU KHI MỞ DROPDOWN
//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Khuyến mãi có liên quan đến giá trị bill')]")))
//                                 .click();

//                 tc07.pass("Chọn khu vực hiển thị khuyến mãi OK");

//                 ExtentTest tc08 = test.createNode("TC08 - Chi phí phòng ban");

//                 // 1️⃣ CLICK DROPDOWN
//                 By dropdownCPPB1 = By.xpath(
//                                 "//label[contains(text(),'Chi phí phòng ban')]" +
//                                                 "/ancestor::div[contains(@class,'ant-form-item')]" +
//                                                 "//div[contains(@class,'ant-select-selector')]");

//                 WebElement dropdownElement = wait.until(
//                                 ExpectedConditions.elementToBeClickable(dropdownCPPB1));
//                 dropdownElement.click();

//                 // 2️⃣ CLICK OPTION
//                 By optionCPPB1 = By.xpath(
//                                 "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]"
//                                                 +
//                                                 "//div[contains(@class,'ant-select-item-option-content') and " +
//                                                 "normalize-space()='Phòng Marketing / FMK']");

//                 wait.until(ExpectedConditions.elementToBeClickable(optionCPPB1)).click();

//                 tc08.pass("Chọn Chi phí phòng ban OK");

//                 // 3️⃣ NEXT
//                 wait.until(ExpectedConditions.elementToBeClickable(
//                                 By.xpath("//span[contains(text(),'Tiếp theo')]"))).click();

//                 // Step 3
//                 ExtentTest tc09 = test.createNode("Loại đầu vào");
//                 By dropdownLDV1 = By.xpath(
//                                 "//div[contains(@class,'ant-col ant-col-3')]" +
//                                                 "//div[contains(@class,'ant-select-selector')]");
//                 WebElement dropdownElement1 = wait.until(
//                                 ExpectedConditions.elementToBeClickable(dropdownLDV1));
//                 dropdownElement1.click();

//                 By optionLDVBy1 = By.xpath(
//                                 "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]"
//                                                 +
//                                                 "//div[contains(@class,'ant-select-item-option-content') and " +
//                                                 "normalize-space()='Mã Ngành hàng']");

//                 wait.until(ExpectedConditions.elementToBeClickable(optionLDVBy1)).click();

//                 tc09.pass("Loại đầu vào");

//                 ExtentTest tc10 = test.createNode("Phép toán");
//                 By dropdownPT1 = By.xpath(
//                                 "//div[contains(@class,'ant-col ant-col-2')]" +
//                                                 "//div[contains(@class,'ant-select-selector')]");
//                 WebElement dropdownElement2 = wait.until(
//                                 ExpectedConditions.elementToBeClickable(dropdownPT1));
//                 dropdownElement2.click();

//                 By optionPTBy1 = By.xpath(
//                                 "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]"
//                                                 +
//                                                 "//div[contains(@class,'ant-select-item-option-content') and " +
//                                                 "normalize-space()='Bằng']");

//                 wait.until(ExpectedConditions.elementToBeClickable(optionPTBy1)).click();

//                 tc10.pass("Phép toán  OK");

//                 ExtentTest tc11 = test.createNode("Giá trị");

//                 // CLICK DROPDOWN
//                 By dropdownGT = By.id(
//                                 "inputID"// div[contains(@class,'ant-select')]"
//                 );

//                 WebElement dropdownElement4 = wait.until(
//                                 ExpectedConditions.elementToBeClickable(dropdownGT));
//                 dropdownElement4.click();

//                 // CHỌN OPTION TRONG TABLE
//                 By optionGT = By.xpath(
//                                 "//td[contains(@class,'ant-table-cell') and contains(@class,'ant-table-selection-column')]");

//                 wait.until(ExpectedConditions.elementToBeClickable(optionGT)).click();

//                 // CLICK OK
//                 WebElement btnOK = wait.until(
//                                 ExpectedConditions.elementToBeClickable(
//                                                 By.xpath("//button//span[text()='OK']")));
//                 btnOK.click();

//                 tc11.pass("Gía tri OK");

//                 ExtentTest tc12 = test.createNode("Loại đầu ra");
//                 By dropdownLDR = By.id("includeInput_0_groupCode_0_outputQualifierId");
//                 WebElement dropdownElement5 = wait.until(
//                                 ExpectedConditions.elementToBeClickable(dropdownLDR));
//                 dropdownElement5.click();

//                 By optionLDR = By.xpath(
//                                 "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]"
//                                                 +
//                                                 "//div[contains(@class,'ant-select-item-option-content') and " +
//                                                 "normalize-space()='Giảm theo giá cố định']");

//                 wait.until(ExpectedConditions.elementToBeClickable(optionLDR)).click();

//                 tc12.pass("Loại đầu ra  OK");

//                 ExtentTest tc13 = test.createNode("TC11 - Nhập giá trị");

//                 By inputGiaTriBy = By.xpath(
//                         "//input[@placeholder='Nhập giảm theo giá cố định']"
//                     );
                    
//                 WebElement inputGiaTri = wait.until(
//                                 ExpectedConditions.elementToBeClickable(inputGiaTriBy));
                
//                 inputGiaTri.sendKeys("20000");

//                 tc13.pass("Nhập giá trị thành công");

//                 // CLICK XÁC NHẬN
//                 WebElement btnXACNHAN = wait.until(
//                                 ExpectedConditions.elementToBeClickable(
//                                                 By.xpath("//button[.//span[normalize-space()='Xác nhận']]")));
//                 btnXACNHAN.click();

//                 // Pop_up đồng ý
//                 By btnDongYBy = By.xpath(
//                         "//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='Đồng ý']]"
//                     );
                    
//                     WebElement btnDongY = wait.until(
//                         ExpectedConditions.elementToBeClickable(btnDongYBy)
//                     );
//                     btnDongY.click();
                    

//         }



}

// displayArea

// thứ tự run test
// 1. @BeforeSuite (setupReport)
// ↓
// 2. @BeforeMethod (setup) → Mở browser, navigate
// ↓
// 3. TestListener.onTestStart()
// ↓
// 4. @Test (testLogin) → Chạy test
// ↓
// 5. TestListener.onTestSuccess/Failure()
// ↓
// 6. @AfterMethod (teardown) → Screenshot, đóng browser
// ↓
// 7. @AfterSuite (flushReport) → Ghi report