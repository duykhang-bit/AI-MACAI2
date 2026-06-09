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
public class PromotionUATDH extends BaseTest1 {

        @Override
        protected String getBaseUrl() {
                return "https://uat-promotion.frt.vn/manager-promotion-list";
        }

        // =================================================
        // FULL FLOW - TẠO CTKM (TC01 → TC06)
        // =================================================
        @Test(priority = 1, description = "FLOW 1 - CTKM SẢN PHẨM")

        public void testCreatePromotionFlowNhomCTKMSanphamUAT() {

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

                String promoName = "Automation test 25052026_115716";

                wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.id("promotiongeneralinfor_name"))).sendKeys(promoName);

                tc02.info("Nhập tên CTKM: " + promoName);

                // wait.until(ExpectedConditions.visibilityOfElementLocated(
                // By.id("promotiongeneralinfor_name")))
                // .sendKeys("Automation Test 31/12 ");

                // tc02.info("Nhập ghi chú");
                // wait.until(ExpectedConditions.visibilityOfElementLocated(
                // By.id("promotiongeneralinfor_remark")))
                // .sendKeys("AT ");

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
                campaignBox.sendKeys("CD-0626-046");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option') and contains(.,'CD-0626-046')]")))
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

                ExtentTest tc08 = test.createNode("TC08 - Chi phí phòng ban");

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
                                                "normalize-space()='Mã sản phẩm']");

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

                By inputSearchBy = By.xpath(
                                "//input[@placeholder='Tìm kiếm mã, tên' and contains(@class,'ant-input')]");

                WebElement inputSearch = wait.until(
                                ExpectedConditions.elementToBeClickable(inputSearchBy));

                inputSearch.clear();
                inputSearch.sendKeys("00001958");

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
                                "//input[@placeholder='Nhập giảm theo giá cố định']");

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
                                "//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='Đồng ý']]");

                WebElement btnDongY = wait.until(
                                ExpectedConditions.elementToBeClickable(btnDongYBy));
                btnDongY.click();

        }

        // FLOW CTKM ĐƠN HÀNG
        @Test(priority = 2, description = "FLOW 2 - CTKM \u0110\u01A0N H\u00C0NG")
        public void testCreatePromotionFlowNhomCTKMDonhang() throws InterruptedException {

                ExtentTest tc01 = test.createNode("TC01 - Login h\u1EC7 th\u1ED1ng");
                tc01.info("Nh\u1EADp username");
                WebElement userNameBox2 = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.UserNameOrEmailAddress")));
                userNameBox2.clear();
                userNameBox2.sendKeys("giant");

                tc01.info("Nh\u1EADp password");
                WebElement passwordBox2 = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.name("LoginInput.Password")));
                passwordBox2.clear();
                passwordBox2.sendKeys("********");

                tc01.info("Click \u0111\u0103ng nh\u1EADp");
                driver.findElement(By.id("kt_login_signin_submit")).click();
                wait.until(ExpectedConditions.urlContains("manager"));
                tc01.pass("Login th\u00E0nh c\u00F4ng");

                ExtentTest tc02 = test.createNode("TC02 - T\u1EA1o CTKM");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'actionHeader')]"))).click();

                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
                String promoName2 = "AT_DH_" + LocalDateTime.now().format(formatter2);
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.id("promotiongeneralinfor_name"))).sendKeys(promoName2);
                tc02.pass("T\u1EA1o CTKM OK: " + promoName2);

                ExtentTest tc03 = test.createNode("TC03 - Th\u1EDDi gian");
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
                tc03.pass("Th\u1EDDi gian OK");

                ExtentTest tc04 = test.createNode("TC04 - Chi\u1EBFn d\u1ECBch");
                WebElement campaignBox2 = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.id("promotiongeneralinfor_campaignId")));
                campaignBox2.sendKeys("CD-0626-046");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option') and contains(.,'CD-0626-046')]")))
                                .click();
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(),'Ti\u1EBFp theo')]"))).click();
                tc04.pass("Chi\u1EBFn d\u1ECBch OK");

                ExtentTest tc05 = test.createNode("TC05 - Nh\u00F3m CTKM: \u0110\u01A1n h\u00E0ng");
                WebElement nhom2 = wait.until(
                                ExpectedConditions.elementToBeClickable(By.id("promotionClassId")));
                nhom2.sendKeys("\u0110\u01A1n h\u00E0ng");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'\u0110\u01A1n h\u00E0ng')]")))
                                .click();
                tc05.pass("Nh\u00F3m CTKM OK");

                ExtentTest tc06 = test.createNode("TC06 - Lo\u1EA1i CTKM: T\u1ED5ng Ti\u1EC1n \u0110\u01A1n H\u00E0ng");
                WebElement loai2 = wait.until(ExpectedConditions.elementToBeClickable(By.id("promotionTypeID")));
                loai2.click();
                loai2.sendKeys("T\u1ED5ng Ti\u1EC1n \u0110\u01A1n H\u00E0ng");
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'T\u1ED5ng Ti\u1EC1n \u0110\u01A1n H\u00E0ng')]")))
                                .click();
                tc06.pass("Lo\u1EA1i CTKM OK");

                ExtentTest tc07 = test.createNode("TC07 - Khu v\u1EF1c hi\u1EC3n th\u1ECB");
                By displayArea2 = By.xpath(
                                "//label[contains(text(),'Khu v\u1EF1c hi\u1EC3n th\u1ECB khuy\u1EBFn m\u00E3i')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(displayArea2)).click();
                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-select-item-option-content') and contains(.,'Khuy\u1EBFn m\u00E3i c\u00F3 li\u00EAn quan \u0111\u1EBFn gi\u00E1 tr\u1ECB bill')]")))
                                .click();
                tc07.pass("Khu v\u1EF1c hi\u1EC3n th\u1ECB OK");

                ExtentTest tc08 = test.createNode("TC08 - Chi ph\u00ED ph\u00F2ng ban");
                WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement uploadInput2 = wait2.until(
                                ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file']")));
                Path imagePath2 = Paths.get(System.getProperty("user.dir"),
                                "src", "test", "resources", "images", "upload.png");
                uploadInput2.sendKeys(imagePath2.toAbsolutePath().toString());

                By dropdownCPPB2 = By.xpath(
                                "//label[contains(text(),'Chi ph\u00ED ph\u00F2ng ban')]" +
                                                "/ancestor::div[contains(@class,'ant-form-item')]" +
                                                "//div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownCPPB2)).click();
                By optionCPPB2 = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='Ph\u00F2ng ki\u1EC3m so\u00E1t ngh\u00E0nh h\u00E0ng / KSNH']");
                wait.until(ExpectedConditions.elementToBeClickable(optionCPPB2)).click();
                tc08.pass("Chi ph\u00ED ph\u00F2ng ban OK");

                wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[contains(text(),'Ti\u1EBFp theo')]"))).click();

                // Step 3 - Thi\u1EBFt l\u1EADp \u0111i\u1EC1u ki\u1EC7n
                ExtentTest tc09 = test.createNode("TC09 - \u0110i\u1EC1u ki\u1EC7n \u0111\u1EA7u v\u00E0o: T\u1ED5ng \u0111\u01A1n h\u00E0ng");
                
                // Click dropdown "Ch\u1ECDn \u0111i\u1EC1u ki\u1EC7n"
                By dropdownDK = By.xpath(
                                "(//div[contains(@class,'ant-select-selector')]//span[contains(text(),'Ch\u1ECDn \u0111i\u1EC1u ki\u1EC7n') or contains(text(),'Ch\u1ECDn \u0111i\u1EC3u ki\u1EC7n')])[1]/ancestor::div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownDK)).click();
                // Ch\u1ECDn "T\u1ED5ng \u0111\u01A1n h\u00E0ng"
                By optionDK = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "contains(.,'T\u1ED5ng \u0111\u01A1n h\u00E0ng')]");
                wait.until(ExpectedConditions.elementToBeClickable(optionDK)).click();
                tc09.pass("\u0110i\u1EC1u ki\u1EC7n \u0111\u1EA7u v\u00E0o OK");

                ExtentTest tc10 = test.createNode("TC10 - Ph\u00E9p to\u00E1n: Nh\u1ECF h\u01A1n ho\u1EB7c b\u1EB1ng");
                // Click dropdown Ph\u00E9p to\u00E1n
                By dropdownPT2 = By.xpath(
                                "(//div[contains(@class,'ant-select-selector')]//span[contains(text(),'Ch\u1ECDn ph\u00E9p') or contains(text(),'ph\u00E9p to\u00E1n')])[1]/ancestor::div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownPT2)).click();
                By optionPT2 = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "contains(.,'Nh\u1ECF h\u01A1n ho\u1EB7c b\u1EB1ng')]");
                wait.until(ExpectedConditions.elementToBeClickable(optionPT2)).click();
                tc10.pass("Ph\u00E9p to\u00E1n OK");

                ExtentTest tc11 = test.createNode("TC11 - Gi\u00E1 tr\u1ECB: 300000");
                WebElement giaTriInput = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//input[@placeholder='Gi\u00E1 tr\u1ECB' or @placeholder='Nh\u1EADp gi\u00E1 tr\u1ECB']")));
                giaTriInput.sendKeys("300000");
                tc11.pass("Gi\u00E1 tr\u1ECB OK");

                // \u0110i\u1EC1u ki\u1EC7n \u0111\u1EA7u ra
                ExtentTest tc12 = test.createNode("TC12 - Lo\u1EA1i \u0111\u1EA7u ra: Phi\u1EBFu Mua H\u00E0ng");
                // Click dropdown Lo\u1EA1i (d\u00F2ng \u0111\u1EA7u ra)
                By dropdownLoaiOut = By.xpath(
                                "(//div[contains(@class,'ant-select-selector')]//span[contains(text(),'Ch\u1ECDn lo\u1EA1i') or contains(text(),'Ch\u1ECDn Lo\u1EA1i')])[1]/ancestor::div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownLoaiOut)).click();
                By optionLoaiOut = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "contains(.,'Phi\u1EBFu Mua H\u00E0ng')]");
                wait.until(ExpectedConditions.elementToBeClickable(optionLoaiOut)).click();
                tc12.pass("Lo\u1EA1i \u0111\u1EA7u ra OK");

                ExtentTest tc13 = test.createNode("TC13 - Ph\u00E9p to\u00E1n \u0111\u1EA7u ra: B\u1EB1ng");
                By dropdownPTOut = By.xpath(
                                "(//div[contains(@class,'ant-select-selector')]//span[contains(text(),'Ch\u1ECDn ph\u00E9p') or contains(text(),'ph\u00E9p to\u00E1n')])[1]/ancestor::div[contains(@class,'ant-select-selector')]");
                wait.until(ExpectedConditions.elementToBeClickable(dropdownPTOut)).click();
                By optionPTOut = By.xpath(
                                "//div[contains(@class,'ant-select-dropdown') and not(contains(@style,'display: none'))]" +
                                                "//div[contains(@class,'ant-select-item-option-content') and " +
                                                "normalize-space()='B\u1EB1ng']");
                wait.until(ExpectedConditions.elementToBeClickable(optionPTOut)).click();
                tc13.pass("Ph\u00E9p to\u00E1n \u0111\u1EA7u ra OK");

                ExtentTest tc14 = test.createNode("TC14 - Gi\u00E1 tr\u1ECB PMH: 215216216");
                // Click v\u00E0o \u00F4 Gi\u00E1 tr\u1ECB \u0111\u1EC3 m\u1EDF popup "Phi\u1EBFu mua h\u00E0ng"
                WebElement giaTriBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//span[text()='S\u1EEDa' or text()='Ch\u1ECDn']/ancestor::button | //input[@value='0']/following-sibling::*[contains(text(),'S\u1EEDa')] | //button[contains(.,'S\u1EEDa')]")));
                giaTriBtn.click();
                
                // \u0110\u1EE3i popup xu\u1EA5t hi\u1EC7n
                Thread.sleep(2000);
                
                // Nh\u1EADp m\u00E3 v\u00E0o \u00F4 t\u00ECm ki\u1EBFm b\u00EAn tr\u00E1i
                WebElement searchPMH = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//input[@placeholder='T\u00ECm ki\u1EBFm m\u00E3, t\u00EAn' or contains(@placeholder,'T\u00ECm')]")));
                searchPMH.clear();
                searchPMH.sendKeys("215216216");
                Thread.sleep(2000);
                
                // Click d\u1EA5u + b\u00EAn c\u1EA1nh k\u1EBFt qu\u1EA3
                WebElement addPMH = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//tr[contains(.,'215216216')]//span[contains(@class,'anticon-plus')] | //div[contains(@class,'ant-modal')]//tr[contains(.,'215216216')]//td[1]")));
                addPMH.click();
                Thread.sleep(1000);
                
                // Click OK
                WebElement btnOK = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//button[.//span[text()='OK']]")));
                btnOK.click();
                Thread.sleep(1000);
                tc14.pass("Gi\u00E1 tr\u1ECB PMH 215216216 OK");

                ExtentTest tc15 = test.createNode("TC15 - S\u1ED1 l\u01B0\u1EE3ng: 1");
                WebElement soLuong = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//input[@placeholder='S\u1ED1 l\u01B0\u1EE3ng' or contains(@placeholder,'s\u1ED1 l\u01B0\u1EE3ng')]")));
                soLuong.clear();
                soLuong.sendKeys("1");
                tc15.pass("S\u1ED1 l\u01B0\u1EE3ng OK");

                // X\u00E1c nh\u1EADn
                WebElement btnXACNHAN2 = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//button[.//span[normalize-space()='X\u00E1c nh\u1EADn']]")));
                btnXACNHAN2.click();

                By btnDongY2 = By.xpath(
                                "//div[contains(@class,'ant-modal-footer')]//button[.//span[normalize-space()='\u0110\u1ED3ng \u00FD']]");
                wait.until(ExpectedConditions.elementToBeClickable(btnDongY2)).click();

                test.pass("\u2705 Ho\u00E0n th\u00E0nh t\u1EA1o CTKM \u0110\u01A1n H\u00E0ng - T\u1ED5ng Ti\u1EC1n \u0110\u01A1n H\u00E0ng");
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
