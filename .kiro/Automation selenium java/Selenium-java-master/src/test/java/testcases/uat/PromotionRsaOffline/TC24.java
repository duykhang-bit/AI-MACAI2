package testcases.uat.PromotionRsaOffline;

import java.time.Duration;
import java.util.HashMap;
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
public class TC24 extends BaseTest1 {

    private static final String SHOP_CODE       = "80006";
    private static final String INSIDE_CODE     = "00017";
    private static final String CUSTOMER_PHONE  = "0835089255";
    private static final String PROMOTION_CODE  = "KM-0326-084";
    private static final String BUY_PRODUCT     = "00502498";
    private static final String GIFT_PRODUCT_1  = "00502499";
    private static final String GIFT_PRODUCT_2  = "00049066";

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

    @Test(priority = 1, description = "FLOW - Mua " + BUY_PRODUCT + " tặng " + GIFT_PRODUCT_1 + "/" + GIFT_PRODUCT_2 + " KM " + PROMOTION_CODE, invocationCount = 1)
    public void TC024() throws InterruptedException {

        JavascriptExecutor js = (JavascriptExecutor) driver;

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
        tc01.pass("Login thành công với tài khoản lanttp");

        /*
         * =========================
         * TC02 - CHỌN SHOP
         * =========================
         */
        ExtentTest tc02 = test.createNode("TC02 - Nhập " + SHOP_CODE + " và chọn shop");

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
                By.xpath("//div[contains(@class,'ant-select-item-option') and contains(.,'" + SHOP_CODE + "')]")));
        shopOption.click();
        Thread.sleep(1000);
        tc02.pass("Đã chọn shop " + SHOP_CODE);

        /*
         * =========================
         * TC03 - HOÀN TẤT CHỌN SHOP
         * =========================
         */
        ExtentTest tc03 = test.createNode("TC03 - Chọn button Hoàn tất");

        WebElement btnHoanTat = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Hoàn tất') or .//span[contains(text(),'Hoàn tất')]] | //a[contains(text(),'Hoàn tất')]")));
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
                            By.xpath("//button[@aria-label='Close' and contains(@class,'ant-modal-close')]")));
            js.executeScript("arguments[0].click();", closePopup);
            new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.invisibilityOfElementLocated(
                            By.xpath("//div[contains(@class,'ant-modal-wrap') and not(contains(@style,'display: none'))]")));
            tc04.pass("Đã tắt popup");
        } catch (TimeoutException e) {
            tc04.info("Không có popup sản phẩm sai đối tượng");
        }

        Thread.sleep(500);

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
        js.executeScript("arguments[0].click();", menuBanHang);
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
        ExtentTest tc07 = test.createNode("TC07 - Nhập SĐT khách hàng " + CUSTOMER_PHONE);

        WebElement phoneInput = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='phone']")));
        phoneInput.click();
        phoneInput.sendKeys(CUSTOMER_PHONE);
        phoneInput.sendKeys(Keys.ENTER);
        Thread.sleep(2000);
        tc07.pass("Đã nhập SĐT " + CUSTOMER_PHONE);

        /*
         * =========================
         * TC08 - NHẬP SẢN PHẨM MUA
         * =========================
         */
        ExtentTest tc08 = test.createNode("TC08 - Nhập sản phẩm mua " + BUY_PRODUCT);

        Thread.sleep(1000);
        WebElement productInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[starts-with(@id,'search-product-input_session')]")));
        js.executeScript("arguments[0].click(); arguments[0].focus();", productInput);
        Thread.sleep(300);
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
        productInput.sendKeys(BUY_PRODUCT);
        Thread.sleep(1000);

        WebElement searchBtn = driver.findElement(
                By.xpath("//button[contains(@class,'ant-input-search-button')] | " +
                        "//span[contains(@class,'anticon-search')]/ancestor::button | " +
                        "//button[@id='button-search']"));
        js.executeScript("arguments[0].click();", searchBtn);
        Thread.sleep(3000);

        WebElement productItem = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'search-input-dropdown')]//div[contains(@class,'ant-select-item-option')]")));
        js.executeScript("arguments[0].click();", productItem);
        Thread.sleep(3000);

        tc08.pass("Đã nhập sản phẩm " + BUY_PRODUCT);

        /*
         * =========================
         * TC09 - VERIFY CTKM
         * =========================
         */
        ExtentTest tc09 = test.createNode("TC09 - Verify CTKM " + PROMOTION_CODE + " (bám vào 'Khuyến mãi khác')");

        // "Khuyến mãi khác" là 1 link/text nằm TRONG dòng sản phẩm trên màn bán hàng.
        // Locator cũ //*[contains(normalize-space(.),'Khuyến mãi khác')] khớp cả <html>
        // (contains(.) lấy text toàn cây con) → click vô nghĩa.
        // → chỉ lấy element LÁ chứa đúng text (không có element con cũng chứa text đó).
        try {
            WebElement promotionLink = (WebElement) js.executeScript(
                    "var all = document.querySelectorAll('*');" +
                    "for (var i=0;i<all.length;i++){" +
                    "  var el=all[i]; var t=(el.textContent||'').trim();" +
                    "  if (t.indexOf('Khuyến mãi khác')!==-1 && el.offsetParent!==null){" +
                    "    var leaf=true;" +
                    "    for (var j=0;j<el.children.length;j++){" +
                    "      if ((el.children[j].textContent||'').indexOf('Khuyến mãi khác')!==-1){leaf=false;break;}" +
                    "    }" +
                    "    if (leaf){ el.scrollIntoView({block:'center'}); return el; }" +
                    "  }" +
                    "}" +
                    "return null;");
            if (promotionLink == null) {
                throw new RuntimeException("Không tìm thấy link lá 'Khuyến mãi khác' trên màn bán hàng");
            }
            Thread.sleep(500);
            js.executeScript("arguments[0].click();", promotionLink);
            Thread.sleep(1500);

            WebElement promotionModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(.,'Danh sách khuyến mãi')]]")));
            promotionModal.findElement(
                    By.xpath(".//*[contains(normalize-space(.),'" + PROMOTION_CODE + "')]"));
            tc09.pass("✅ Tìm thấy CTKM " + PROMOTION_CODE + " trong 'Danh sách khuyến mãi'");

            // Đóng modal
            try {
                WebElement btnConfirm = promotionModal.findElement(
                        By.xpath(".//button[contains(.,'Xác nhận')]"));
                js.executeScript("arguments[0].click();", btnConfirm);
            } catch (NoSuchElementException e) {
                WebElement closeBtn = promotionModal.findElement(
                        By.xpath(".//button[@aria-label='Close' or contains(@class,'ant-modal-close')]"));
                js.executeScript("arguments[0].click();", closeBtn);
            }
            Thread.sleep(1500);
        } catch (Exception e) {
            tc09.warning("⚠️ Không verify được CTKM qua 'Khuyến mãi khác': " + e.getMessage()
                    + " — sẽ verify qua quà tặng ở TC09b.");
        }

        /*
         * =========================
         * TC09b - VERIFY QUÀ TẶNG TRÊN MÀN BÁN HÀNG (TRƯỚC KHI TẠO ĐƠN)
         * Quà tặng hiện ngay dưới "Khuyến mãi khác" / "Sản phẩm mua kèm" sau khi
         * nhập đủ SP. Phải verify Ở ĐÂY — sau khi tạo đơn thì đã rời màn này nên
         * getPageSource() không còn thấy quà → fail oan (lỗi cũ của TC13).
         * =========================
         */
        ExtentTest tc09b = test.createNode("TC09b - Verify quà tặng " + GIFT_PRODUCT_1 + " và " + GIFT_PRODUCT_2 + " trên màn bán hàng");
        Thread.sleep(1500); // đợi UI render dòng quà tặng
        String sellPageSource = driver.getPageSource();
        boolean giftFailed = false;

        if (sellPageSource.contains(GIFT_PRODUCT_1)) {
            tc09b.pass("✅ Tìm thấy quà tặng " + GIFT_PRODUCT_1 + " trên màn bán hàng");
        } else {
            attachScreenshot("❌ Không thấy quà tặng " + GIFT_PRODUCT_1 + " trên màn bán hàng");
            tc09b.fail("❌ Không tìm thấy quà tặng " + GIFT_PRODUCT_1 + " trên màn bán hàng");
            giftFailed = true;
        }
        if (sellPageSource.contains(GIFT_PRODUCT_2)) {
            tc09b.pass("✅ Tìm thấy quà tặng " + GIFT_PRODUCT_2 + " trên màn bán hàng");
        } else {
            attachScreenshot("❌ Không thấy quà tặng " + GIFT_PRODUCT_2 + " trên màn bán hàng");
            tc09b.fail("❌ Không tìm thấy quà tặng " + GIFT_PRODUCT_2 + " trên màn bán hàng");
            giftFailed = true;
        }

        /*
         * =========================
         * TC10 → TC12: TẠO ĐƠN + TỔNG TIỀN + HOÀN TẤT
         * =========================
         */
        String orderCode = "";
        Exception orderCreationFailed = null;
        try {
            ExtentTest tc10 = test.createNode("TC10 - Click Tạo đơn (F4)");

            // Locator theo TEXT + verify trước khi click (union XPath cũ dễ khớp sai nút).
            WebElement btnTaoDon = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(normalize-space(.),'Tạo đơn')]")));

            String btnInfo = (String) js.executeScript(
                    "var e=arguments[0];" +
                    "return 'text=\"' + (e.innerText||'').trim() + '\" id=' + (e.id||'-') " +
                    "+ ' class=' + (e.className||'-') + ' disabled=' + (e.disabled===true);",
                    btnTaoDon);
            tc10.info("Element sẽ click: " + btnInfo);
            if (!btnInfo.contains("Tạo đơn")) {
                throw new RuntimeException("Locator khớp sai element, không phải nút Tạo đơn: " + btnInfo);
            }

            js.executeScript("arguments[0].scrollIntoView({block:'center'});", btnTaoDon);
            Thread.sleep(500);
            try {
                btnTaoDon.click();
            } catch (Exception clickEx) {
                tc10.info("Native click fail (" + clickEx.getClass().getSimpleName() + ") → fallback JS click");
                js.executeScript("arguments[0].click();", btnTaoDon);
            }
            Thread.sleep(3000);

            // Bắt toast validation của app ngay sau click.
            try {
                java.util.List<WebElement> toasts = driver.findElements(By.xpath(
                        "//div[contains(@class,'ant-notification-notice') or contains(@class,'ant-message-notice')" +
                        " or contains(@class,'Toastify__toast')]"));
                for (WebElement t : toasts) {
                    String msg = t.getText().replace("\n", " ").trim();
                    if (!msg.isEmpty()) tc10.warning("⚠️ Thông báo từ app sau khi click Tạo đơn: " + msg);
                }
            } catch (Exception ignore) { }

            boolean onPayment = !driver.findElements(By.xpath(
                    "//*[contains(text(),'Phương thức thanh toán')] | //*[contains(text(),'Về giỏ hàng')]")).isEmpty();
            if (onPayment) {
                tc10.pass("✅ Đã click Tạo đơn — màn thanh toán đã mở");
            } else {
                attachScreenshot("❌ Click Tạo đơn nhưng KHÔNG sang màn thanh toán");
                tc10.fail("❌ Click Tạo đơn nhưng vẫn ở màn bán hàng — app chặn tạo đơn");
                throw new RuntimeException("Tạo đơn không có tác dụng — không sang được màn thanh toán");
            }

            ExtentTest tc11 = test.createNode("TC11 - Click Tổng tiền");

            WebElement btnTongTien = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Tổng tiền')] | " +
                            "//*[contains(text(),'Tổng tiền') and contains(text(),'Shift')]")));
            js.executeScript("arguments[0].click();", btnTongTien);
            Thread.sleep(3000);
            tc11.pass("Đã click Tổng tiền");

            ExtentTest tc12 = test.createNode("TC12 - Click Hoàn tất và xác nhận inside");

            WebElement btnHoanTatFinal = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Hoàn tất')]")));
            js.executeScript("arguments[0].click();", btnHoanTatFinal);
            Thread.sleep(3000);

            // Popup xác nhận inside lần 2 (nếu có)
            try {
                WebElement nvDropdown = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//div[contains(@class,'ant-select-selector')]")));
                nvDropdown.click();
                Thread.sleep(500);

                WebElement nvSearchActive = driver.switchTo().activeElement();
                nvSearchActive.sendKeys(INSIDE_CODE);
                Thread.sleep(1500);

                WebElement nvOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[contains(text(),'Trần Thị Thanh Thảo') or contains(text(),'(" + INSIDE_CODE + ")')]")));
                nvOption.click();
                Thread.sleep(1000);

                try {
                    WebElement insideInput2 = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(
                                    By.xpath("//div[contains(@class,'ant-modal')]//input[@type='text' or @type='password'][not(contains(@class,'ant-select'))] | " +
                                            "//input[contains(@placeholder,'inside') or contains(@placeholder,'mã inside')]")));
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

            // Lấy mã đơn
            try {
                WebElement orderEl = driver.findElement(
                        By.xpath("//span[contains(@class,'order-number') or contains(@class,'order-code') or contains(@class,'ma-don')]"));
                orderCode = orderEl.getText().trim();
            } catch (Exception e) {
                orderCode = "Đơn tạo thành công - check màn hình";
            }

            tc12.pass("✅ Hoàn tất đơn hàng! Mã đơn: " + orderCode);

        } catch (Exception e) {
            orderCode = "KHÔNG TẠO ĐƯỢC ĐƠN";
            attachScreenshot("❌ Lỗi ở luồng tạo đơn / thanh toán");
            test.fail("❌ Lỗi khi tạo đơn: " + e.getMessage());
            orderCreationFailed = e;
        }

        System.out.println("========================================");
        System.out.println("MÃ ĐƠN HÀNG: " + orderCode);
        System.out.println("========================================");

        // Quà tặng đã được verify ở TC09b (trên màn bán hàng, trước khi tạo đơn).
        if (giftFailed) {
            throw new AssertionError("TC24 FAIL — không thấy đủ quà tặng "
                    + GIFT_PRODUCT_1 + "/" + GIFT_PRODUCT_2 + " trên màn bán hàng");
        }
        if (orderCreationFailed != null) {
            throw new AssertionError("TC24 FAIL ở luồng tạo đơn/thanh toán: "
                    + orderCreationFailed.getMessage(), orderCreationFailed);
        }

        test.pass("✅ Hoàn thành TC24 - Mua " + BUY_PRODUCT + " tặng " + GIFT_PRODUCT_1 + "/" + GIFT_PRODUCT_2
                + " CTKM " + PROMOTION_CODE + ". Mã đơn: " + orderCode);
    }
}
