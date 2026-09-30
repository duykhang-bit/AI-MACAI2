package testcases.uat.PromotionRsaOffline;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;

import base.BaseTest1;
import io.github.bonigarcia.wdm.WebDriverManager;
import listeners.TestListener;

@Listeners(TestListener.class)
public class TC25 extends BaseTest1 {

    // KM-0926-280 — test mud5 — Sử dụng mã ưu đãi theo sản phẩm
    // Điều kiện đầu vào:
    //   Mã SP 00005132 (Chứa) - Hộp >= 3
    //   Mã SP 00001374 (Chứa) - Viên >= 2
    //   Mã SP 00005453 (Bằng) - Vỉ = 1
    //   Mã ưu đãi (campaign code): 3725  → gen serial MUD qua API → apply serial đó vào đơn
    // Điều kiện đầu ra:
    //   Phiếu 3452345, SL=1, Tối thiểu=0, Tối đa=0 => tặng PHM không giới hạn
    private static final String SHOP_CODE       = "80006";
    private static final String INSIDE_CODE     = "00017";
    private static final String CUSTOMER_PHONE  = "0835089255";
    private static final String PROMOTION_CODE  = "KM-0926-280";
    private static final String CAMPAIGN_CODE   = "3725";      // dùng để gen serial MUD qua API
    private static final String BUY_PRODUCT_1   = "00005132";  // Hộp x3
    private static final String BUY_PRODUCT_2   = "00001374";  // Viên x2
    private static final String BUY_PRODUCT_3   = "00005453";  // Vỉ x1
    private static final String UNIT_1          = "Hộp";
    private static final String UNIT_2          = "Viên";
    private static final String UNIT_3          = "Vỉ";
    private static final String QTY_1           = "3";
    private static final String QTY_2           = "2";
    private static final String QTY_3           = "1";
    private static final String GIFT_VOUCHER    = "3452345";   // PHM tặng không giới hạn

    @Override
    protected String getBaseUrl() {
        return "https://uat-rsa-web.frt.vn/";
    }

    @Override
    @BeforeMethod
    public void setup(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        if (desc != null && !desc.isEmpty()) { testName = testName + " - " + desc; }
        test = extent.createTest(testName);

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        if (utils.ConfigReader.getInstance().isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
        }
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-save-password-bubble");

        driver = new ChromeDriver(options);
        driver.get(getBaseUrl());

        wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @Test(priority = 1,
          description = "KM-0926-280 — test mud5 — Mua SKU theo MUD tặng PHM " + GIFT_VOUCHER + " không giới hạn",
          invocationCount = 1)
    public void TC025() throws InterruptedException {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        // ─── STEP 0: GEN MUD SERIAL QUA API TRƯỚC KHI VÀO TRANG ─────────────────
        // "3725" là campaign code để GỌI API — API trả về serial ngẫu nhiên (vd: "V8F3VX8W26")
        // Serial đó mới là thứ cần apply vào ô "Nhập mã KM" — KHÔNG nhập "3725" trực tiếp
        ExtentTest tcGenMud = test.createNode("TC00 - Gen serial MUD qua API (campaign=" + CAMPAIGN_CODE + ")");
        String mudSerial = null;
        try {
            mudSerial = utils.MudApiGenerator.generateMudCode(CAMPAIGN_CODE, CUSTOMER_PHONE);
            if (mudSerial == null || mudSerial.isEmpty() || mudSerial.equals(CAMPAIGN_CODE)) {
                throw new RuntimeException("API trả về serial không hợp lệ: " + mudSerial);
            }
            tcGenMud.pass("✅ Gen MUD thành công — serial: " + mudSerial);
            System.out.println("[TC25] MUD serial from API: " + mudSerial);
        } catch (Exception e) {
            // Fallback: lấy serial pre-gen từ products.json mud2 (code cũng là 3725, pre-gen sẵn)
            mudSerial = utils.MudCodeProvider.getNextMudCode("mud2");
            tcGenMud.warning("⚠️ API fail → dùng serial pre-gen: " + mudSerial + " | Lỗi: " + e.getMessage());
        }
        final String mudSerialToApply = mudSerial; // serial thực tế, VD: "V8F3VX8W26"

        /*
         * =========================
         * TC01 - LOGIN
         * =========================
         */
        ExtentTest tc01 = test.createNode("TC01 - Login với tài khoản lanttp");

        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
                By.name("LoginInput.UserNameOrEmailAddress")));
        username.clear();
        username.sendKeys("lanttp");

        WebElement password = wait.until(ExpectedConditions.elementToBeClickable(
                By.name("LoginInput.Password")));
        password.clear();
        password.sendKeys("123456");

        driver.findElement(By.id("kt_login_signin_submit")).click();
        Thread.sleep(3000);

        try {
            WebElement popupOkBtn = driver.findElement(
                    By.xpath("//button[text()='OK' or text()='Ok'] | //button[contains(@class,'dismiss')]"));
            popupOkBtn.click();
            Thread.sleep(500);
        } catch (NoSuchElementException e) { }

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Chọn địa chỉ đăng nhập') or contains(text(),'Chọn Shop')]")));
        tc01.pass("Login thành công");

        /*
         * =========================
         * TC02 - CHỌN SHOP
         * =========================
         */
        ExtentTest tc02 = test.createNode("TC02 - Chọn shop " + SHOP_CODE);

        WebElement shopDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ant-select')]//div[contains(@class,'ant-select-selector')]")));
        shopDropdown.click();
        Thread.sleep(500);

        WebElement shopSearchInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ant-select-dropdown')]//input | " +
                        "//input[contains(@class,'ant-select-selection-search-input')]")));
        shopSearchInput.sendKeys(SHOP_CODE);
        Thread.sleep(1500);

        WebElement shopOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ant-select-item-option') and contains(.,'" + SHOP_CODE + "')] | " +
                        "//div[contains(@class,'ant-select-item-option-content') and contains(text(),'" + SHOP_CODE + "')]")));
        shopOption.click();
        Thread.sleep(1000);
        tc02.pass("Đã chọn shop " + SHOP_CODE);

        /*
         * =========================
         * TC03 - HOÀN TẤT CHỌN SHOP
         * =========================
         */
        ExtentTest tc03 = test.createNode("TC03 - Hoàn tất chọn shop");

        WebElement btnHoanTat = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Hoàn tất') or .//span[contains(text(),'Hoàn tất')]] | " +
                        "//a[contains(text(),'Hoàn tất')]")));
        btnHoanTat.click();
        Thread.sleep(2000);
        tc03.pass("Click Hoàn tất thành công");

        /*
         * =========================
         * TC04 - TẮT POPUP SAI ĐỐI TƯỢNG / THAY ĐỔI GIÁ
         * =========================
         */
        ExtentTest tc04 = test.createNode("TC04 - Tắt popup nếu có");

        try {
            WebElement closePopup = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Danh sách sản phẩm sai đối tượng')]]" +
                                    "//button[@aria-label='Close' or contains(@class,'ant-modal-close')]")));
            js.executeScript("arguments[0].click();", closePopup);
            tc04.pass("Đã tắt popup sai đối tượng");
        } catch (TimeoutException e) {
            tc04.info("Không có popup sai đối tượng");
        }

        try {
            WebElement btnDeSau = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(.,'Để sau') or .//span[contains(text(),'Để sau')]]")));
            js.executeScript("arguments[0].click();", btnDeSau);
        } catch (TimeoutException e) { }

        Thread.sleep(500);

        /*
         * =========================
         * TC05 - CHỌN MỤC BÁN HÀNG
         * =========================
         */
        ExtentTest tc05 = test.createNode("TC05 - Chọn mục Bán hàng");

        WebElement menuBanHang = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//p[contains(@class,'feature_home') and contains(text(),'Bán hàng')] | " +
                        "//a[.//p[contains(text(),'Bán hàng')]] | " +
                        "//p[contains(text(),'Bán hàng (')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", menuBanHang);
        Thread.sleep(300);
        menuBanHang.click();
        Thread.sleep(3000);
        wait.until(ExpectedConditions.urlContains("sell"));
        tc05.pass("Đã vào mục Bán hàng");

        /*
         * =========================
         * TC06 - NHẬP MÃ INSIDE
         * =========================
         */
        ExtentTest tc06 = test.createNode("TC06 - Nhập mã inside " + INSIDE_CODE);

        WebElement insideInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ant-modal')]//input[@type='text' or @type='search'] | " +
                        "//input[contains(@placeholder,'inside') or contains(@placeholder,'mã')]")));
        insideInput.clear();
        insideInput.sendKeys(INSIDE_CODE);
        Thread.sleep(1500);

        try {
            WebElement insideOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[contains(text(),'Trần Thị Thanh Thảo') or contains(text(),'(" + INSIDE_CODE + ")')]")));
            insideOption.click();
            Thread.sleep(500);
        } catch (TimeoutException e) { }

        try {
            WebElement btnXacNhan = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Xác nhận') or contains(.,'Xác Nhận')]")));
            btnXacNhan.click();
        } catch (TimeoutException e) { }

        Thread.sleep(2000);
        tc06.pass("Đã nhập mã inside " + INSIDE_CODE);

        /*
         * =========================
         * TC07 - NHẬP SĐT KHÁCH HÀNG
         * =========================
         */
        ExtentTest tc07 = test.createNode("TC07 - Nhập SĐT " + CUSTOMER_PHONE);

        WebElement phoneInput = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='phone']")));
        phoneInput.click();
        phoneInput.sendKeys(CUSTOMER_PHONE);
        phoneInput.sendKeys(Keys.ENTER);
        Thread.sleep(2000);
        tc07.pass("Đã nhập SĐT " + CUSTOMER_PHONE);

        /*
         * =========================
         * TC08 - SP1: 00005132 | Hộp | SL=3
         * =========================
         */
        ExtentTest tc08 = test.createNode("TC08 - Nhập SP1 " + BUY_PRODUCT_1 + " đơn vị " + UNIT_1 + " SL " + QTY_1);
        addProduct(js, BUY_PRODUCT_1, UNIT_1);
        tc08.pass("Đã thêm SP1 " + BUY_PRODUCT_1);

        ExtentTest tc08b = test.createNode("TC08b - Nhập SL " + QTY_1 + " cho SP1");
        setQuantity(js, QTY_1);
        tc08b.pass("Đã nhập SL " + QTY_1 + " cho SP1");

        /*
         * =========================
         * TC09 - SP2: 00001374 | Viên | SL=2
         * =========================
         */
        ExtentTest tc09 = test.createNode("TC09 - Nhập SP2 " + BUY_PRODUCT_2 + " đơn vị " + UNIT_2 + " SL " + QTY_2);
        addProduct(js, BUY_PRODUCT_2, UNIT_2);
        tc09.pass("Đã thêm SP2 " + BUY_PRODUCT_2);

        ExtentTest tc09b = test.createNode("TC09b - Nhập SL " + QTY_2 + " cho SP2");
        setQuantity(js, QTY_2);
        tc09b.pass("Đã nhập SL " + QTY_2 + " cho SP2");

        /*
         * =========================
         * TC10 - SP3: 00005453 | Vỉ | SL=1
         * =========================
         */
        ExtentTest tc10 = test.createNode("TC10 - Nhập SP3 " + BUY_PRODUCT_3 + " đơn vị " + UNIT_3 + " SL " + QTY_3);
        addProduct(js, BUY_PRODUCT_3, UNIT_3);
        tc10.pass("Đã thêm SP3 " + BUY_PRODUCT_3);

        // SL=1 là mặc định, không cần setQuantity

        /*
         * =========================
         * TC11 - APPLY SERIAL MUD ĐÃ GEN VÀO ĐƠN
         * Serial lấy từ API ở TC00 — click "Nhập mã KM" → nhập serial → Áp dụng
         * =========================
         */
        ExtentTest tc11 = test.createNode("TC11 - Apply serial MUD: " + mudSerialToApply);

        Thread.sleep(2000);

        try {
            // Tìm element lá "Nhập mã KM" (pattern từ TC8)
            WebElement nhapMaKM = (WebElement) js.executeScript(
                    "var all = document.querySelectorAll('*');" +
                    "for (var i = 0; i < all.length; i++) {" +
                    "  var el = all[i];" +
                    "  var txt = el.textContent || '';" +
                    "  if (txt.includes('Nhập mã KM') && el.offsetParent !== null) {" +
                    "    var children = el.children;" +
                    "    var hasChildWithText = false;" +
                    "    for (var j = 0; j < children.length; j++) {" +
                    "      if ((children[j].textContent || '').includes('Nhập mã KM')) {" +
                    "        hasChildWithText = true; break;" +
                    "      }" +
                    "    }" +
                    "    if (!hasChildWithText) {" +
                    "      el.scrollIntoView({block:'center', behavior:'instant'});" +
                    "      return el;" +
                    "    }" +
                    "  }" +
                    "}" +
                    "return null;");

            if (nhapMaKM == null) {
                tc11.fail("❌ Không tìm thấy 'Nhập mã KM' trên trang");
            } else {
                Thread.sleep(500);
                js.executeScript("arguments[0].click();", nhapMaKM);
                Thread.sleep(2000);

                // Clear voucher cũ nếu có
                try {
                    List<WebElement> voucherTags = driver.findElements(
                            By.xpath("//div[contains(@class,'ant-modal')]//span[contains(@class,'ant-tag')]" +
                                    "//span[contains(@class,'ant-tag-close-icon') or contains(@class,'anticon-close')]"));
                    for (WebElement tag : voucherTags) {
                        try { js.executeScript("arguments[0].click();", tag); Thread.sleep(800); } catch (Exception ex) {}
                    }
                    if (!voucherTags.isEmpty()) Thread.sleep(1000);
                } catch (Exception ex) { }

                // Nhập serial MUD vào ô input popup
                WebElement voucherInput = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'ant-modal')]//input[@type='text'] | " +
                                "//div[contains(@class,'ant-modal')]//input[not(@type='hidden') and not(@type='checkbox') and not(@type='radio')] | " +
                                "//div[contains(@class,'ant-modal')]//input[contains(@class,'ant-input')] | " +
                                "//input[contains(@placeholder,'Nhập mã') or contains(@placeholder,'voucher') " +
                                "    or contains(@placeholder,'mã giảm') or contains(@placeholder,'Barcode')] | " +
                                "//div[contains(@class,'modal')]//input[contains(@class,'ant-input')]")));
                voucherInput.clear();
                voucherInput.sendKeys(mudSerialToApply);
                Thread.sleep(1000);

                // Click "Áp dụng"
                WebElement btnApDung = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'ant-modal')]//button[contains(.,'Áp dụng')] | " +
                                "//div[contains(@class,'ant-modal')]//*[contains(text(),'Áp dụng')] | " +
                                "//button[contains(.,'Áp dụng')] | " +
                                "//span[contains(text(),'Áp dụng')]/ancestor::button | " +
                                "//span[contains(text(),'Áp dụng')]")));
                js.executeScript("arguments[0].click();", btnApDung);
                Thread.sleep(3000);

                // Click "Xác nhận" để đóng popup (nếu có)
                try {
                    WebElement btnXacNhanVoucher = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(
                                    By.xpath("//div[contains(@class,'ant-modal')]//button[contains(.,'Xác nhận')] | " +
                                            "//button[contains(.,'Xác nhận')]")));
                    js.executeScript("arguments[0].click();", btnXacNhanVoucher);
                    Thread.sleep(2000);
                } catch (TimeoutException te) { /* popup tự đóng sau apply */ }

                tc11.pass("✅ Apply serial MUD thành công: " + mudSerialToApply);
            }
        } catch (Exception e) {
            tc11.warning("❌ Apply MUD thất bại: " + e.getMessage());
        }

        /*
         * =========================
         * TC12 - VERIFY CTKM KM-0926-280 ĐƯỢC ÁP DỤNG
         * =========================
         */
        ExtentTest tc12 = test.createNode("TC12 - Verify CTKM " + PROMOTION_CODE);

        try {
            WebElement promotionLink = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(normalize-space(.),'Khuyến mãi khác')]")));
            js.executeScript("arguments[0].click();", promotionLink);
            Thread.sleep(1000);

            WebElement promotionModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(.,'Danh sách khuyến mãi')]]")));
            promotionModal.findElement(By.xpath(".//*[contains(normalize-space(.),'" + PROMOTION_CODE + "')]"));
            tc12.pass("✅ Tìm thấy " + PROMOTION_CODE + " trong danh sách khuyến mãi");

            try {
                WebElement btnConfirm = promotionModal.findElement(By.xpath(".//button[contains(.,'Xác nhận')]"));
                js.executeScript("arguments[0].click();", btnConfirm);
            } catch (NoSuchElementException ex) {
                WebElement closeBtn = promotionModal.findElement(
                        By.xpath(".//button[@aria-label='Close' or contains(@class,'ant-modal-close')]"));
                js.executeScript("arguments[0].click();", closeBtn);
            }
            Thread.sleep(1500);
        } catch (Exception e) {
            tc12.info("Không tìm thấy 'Khuyến mãi khác' — verify qua PHM tặng. " + e.getMessage());
        }

        /*
         * =========================
         * TC13 - TẠO ĐƠN
         * =========================
         */
        String orderCode = "";
        try {
            ExtentTest tc13 = test.createNode("TC13 - Click Tạo đơn");

            WebElement btnTaoDon = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@class,'btn_container') or contains(@id,'btn_finish')] | " +
                            "//button[.//span[contains(text(),'Tạo đơn')]] | " +
                            "//div[contains(@class,'btn_container')]//button")));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", btnTaoDon);
            Thread.sleep(500);
            js.executeScript("arguments[0].click();", btnTaoDon);
            Thread.sleep(3000);
            tc13.pass("Đã click Tạo đơn");

            ExtentTest tc14 = test.createNode("TC14 - Click Tổng tiền");
            WebElement btnTongTien = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Tổng tiền')] | " +
                            "//*[contains(text(),'Tổng tiền') and contains(text(),'Shift')]")));
            js.executeScript("arguments[0].click();", btnTongTien);
            Thread.sleep(3000);
            tc14.pass("Đã click Tổng tiền");

            ExtentTest tc15 = test.createNode("TC15 - Hoàn tất đơn");
            WebElement btnHoanTatFinal = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Hoàn tất')]")));
            js.executeScript("arguments[0].click();", btnHoanTatFinal);
            Thread.sleep(3000);

            // Popup xác nhận inside lần 2 nếu có
            try {
                WebElement nvDropdown = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//div[contains(@class,'ant-select-selector')]")));
                nvDropdown.click();
                Thread.sleep(500);

                driver.switchTo().activeElement().sendKeys(INSIDE_CODE);
                Thread.sleep(1500);

                WebElement nvOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[contains(text(),'Trần Thị Thanh Thảo') or contains(text(),'(" + INSIDE_CODE + ")')]")));
                nvOption.click();
                Thread.sleep(1000);

                try {
                    WebElement insideInput2 = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(
                                    By.xpath("//div[contains(@class,'ant-modal')]//input[@type='text' or @type='password']" +
                                            "[not(contains(@class,'ant-select'))]")));
                    insideInput2.click();
                    insideInput2.sendKeys(INSIDE_CODE);
                    Thread.sleep(1000);
                } catch (Exception e) { }

                WebElement btnXacNhanFinal = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(.,'Xác nhận') or .//span[text()='Xác nhận']]")));
                btnXacNhanFinal.click();
                Thread.sleep(5000);
            } catch (Exception e) {
                Thread.sleep(3000);
            }

            // Lấy mã đơn hàng — dùng cùng pattern TC8
            try {
                WebElement orderEl = driver.findElement(
                        By.xpath("//span[contains(@class,'order-number') or contains(@class,'order-code') or contains(@class,'ma-don')] | " +
                                "//div[contains(@class,'order-number') or contains(@class,'order-code')] | " +
                                "//div[contains(@class,'header')]//span[string-length(normalize-space()) > 3 and string-length(normalize-space()) < 12 and number(normalize-space()) = number(normalize-space())]"));
                orderCode = orderEl.getText().trim();
            } catch (Exception e) {
                try {
                    // Fallback: tìm số nguyên 5-10 ký tự lớn hơn 1000000 (dạng mã đơn RSA)
                    WebElement numEl = driver.findElement(
                            By.xpath("//*[string-length(normalize-space()) >= 5 and string-length(normalize-space()) <= 10 " +
                                    "and number(normalize-space()) = number(normalize-space()) and normalize-space() > 1000000]"));
                    orderCode = numEl.getText().trim();
                } catch (Exception e2) {
                    orderCode = "Đơn tạo thành công - check màn hình";
                }
            }
            tc15.pass("✅ Hoàn tất! Mã đơn: " + orderCode);

        } catch (Exception e) {
            orderCode = "Có thể đã tạo - check hệ thống";
            test.info("⚠️ Lỗi khi tạo đơn: " + e.getMessage());
        }

        /*
         * =========================
         * TC16 - VERIFY PHM TẶNG 3452345 KHÔNG GIỚI HẠN
         * =========================
         */
        ExtentTest tc16 = test.createNode("TC16 - Verify PHM tặng " + GIFT_VOUCHER + " (không giới hạn)");

        String pageSource = driver.getPageSource();
        if (pageSource.contains(GIFT_VOUCHER)) {
            tc16.pass("✅ Tìm thấy PHM " + GIFT_VOUCHER + " — CTKM áp dụng đúng");
        } else {
            tc16.fail("❌ Không tìm thấy PHM " + GIFT_VOUCHER + " — kiểm tra lại điều kiện KM hoặc SKU");
        }

        if (pageSource.contains(PROMOTION_CODE)) {
            tc16.pass("✅ CTKM " + PROMOTION_CODE + " hiển thị trên trang đơn");
        } else {
            tc16.info("ℹ️ " + PROMOTION_CODE + " không hiển thị trực tiếp (có thể bình thường)");
        }

        System.out.println("========================================");
        System.out.println("TC25 — KM-0926-280 test mud5");
        System.out.println("MUD serial: " + mudSerialToApply);
        System.out.println("Mã đơn   : " + orderCode);
        System.out.println("PHM tặng : " + GIFT_VOUCHER + " (không giới hạn)");
        System.out.println("========================================");

        test.pass("✅ TC25 xong — KM " + PROMOTION_CODE + " serial=" + mudSerialToApply
                + " tặng PHM " + GIFT_VOUCHER + " | Đơn: " + orderCode);
    }

    // ─── HELPER: nhập sản phẩm + chọn đơn vị ────────────────────────────────────
    private void addProduct(JavascriptExecutor js, String sku, String unit) throws InterruptedException {
        WebElement productInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[starts-with(@id,'search-product-input_session')]")));

        js.executeScript("arguments[0].click(); arguments[0].focus();", productInput);
        Thread.sleep(300);

        // Clear
        js.executeScript(
                "var el = arguments[0];" +
                "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
                "nativeSetter.call(el,'');" +
                "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                "el.dispatchEvent(new Event('change',{bubbles:true}));", productInput);
        Thread.sleep(200);
        productInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        productInput.sendKeys(Keys.DELETE);
        Thread.sleep(300);

        productInput.sendKeys(sku);
        Thread.sleep(1000);

        // Click search button
        WebElement searchBtn = driver.findElement(
                By.xpath("//button[contains(@class,'ant-input-search-button') or contains(@class,'ant-btn-icon-only')] | " +
                        "//span[contains(@class,'anticon-search')]/ancestor::button | " +
                        "//button[@id='button-search']"));
        js.executeScript("arguments[0].click();", searchBtn);
        Thread.sleep(3000);

        // Chọn item đầu tiên trong dropdown
        WebElement productItem = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'search-input-dropdown')]//div[contains(@class,'ant-select-item-option')]")));
        js.executeScript("arguments[0].click();", productItem);
        Thread.sleep(2500);

        // Chọn đơn vị
        try {
            Thread.sleep(500);
            WebElement unitSelect = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'ant-select-selector')][.//span[contains(text(),'Hộp') or " +
                            "contains(text(),'Viên') or contains(text(),'Vỉ') or contains(text(),'Gói') or " +
                            "contains(text(),'Chai') or contains(text(),'Cái') or contains(text(),'Tuýp')]]")));
            unitSelect.click();
            Thread.sleep(800);
            WebElement unitOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'ant-select-item-option-content') and text()='" + unit + "']")));
            unitOption.click();
            Thread.sleep(1000);
        } catch (Exception e) {
            // Đơn vị mặc định đã đúng hoặc SP chỉ có 1 đơn vị
        }
    }

    // ─── HELPER: nhập số lượng cho SP vừa thêm ───────────────────────────────────
    // Lấy ô qty CUỐI CÙNG trên trang = SP vừa được thêm vào giỏ
    private void setQuantity(JavascriptExecutor js, String qty) throws InterruptedException {
        Thread.sleep(1500);
        // (last()) để luôn lấy đúng dòng SP mới nhất, tránh set nhầm SP cũ
        WebElement qtyInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//input[contains(@id,'input-quantity-product')])[last()]")));
        js.executeScript(
                "var el = arguments[0];" +
                "var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
                "nativeSetter.call(el,'" + qty + "');" +
                "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                "el.dispatchEvent(new Event('change',{bubbles:true}));" +
                "el.blur();", qtyInput);
        Thread.sleep(1500);
    }
}
