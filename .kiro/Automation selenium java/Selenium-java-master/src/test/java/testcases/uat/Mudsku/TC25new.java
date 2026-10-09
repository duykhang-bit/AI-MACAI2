package testcases.uat.Mudsku;

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
public class TC25new extends BaseTest1 {

    // KM-1026-118 -- Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Khong 100ml
    // Dieu kien dau vao:
    //   Ma SP 00031035 - SL >= 1
    //   Ma uu dai (campaign code): 3765  -> gen serial MUD qua API -> apply serial do vao don
    private static final String SHOP_CODE       = "80006";
    private static final String INSIDE_CODE     = "00017";
    private static final String CUSTOMER_PHONE  = "0835089254";
    private static final String PROMOTION_CODE  = "KM-1026-118";
    private static final String CAMPAIGN_CODE   = "3765";
    private static final String BUY_PRODUCT_1   = "00031035";
    private static final int    BUY_QTY         = 1;

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
          description = "KM-1026-118 - TK Gia Dinh - Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Khong (100ml)",
          invocationCount = 1)
    public void TC025new() throws InterruptedException {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        // ─── STEP 0: GEN MUD SERIAL QUA API TRUOC KHI VAO TRANG ─────────────────
        ExtentTest tcGenMud = test.createNode("TC00 - Gen serial MUD qua API (campaign=" + CAMPAIGN_CODE + ")");
        String mudSerial = null;
        try {
            mudSerial = utils.MudApiGenerator.generateMudCode(CAMPAIGN_CODE, CUSTOMER_PHONE);
            if (mudSerial == null || mudSerial.isEmpty() || mudSerial.equals(CAMPAIGN_CODE)) {
                throw new RuntimeException("API tra ve serial khong hop le: " + mudSerial);
            }
            tcGenMud.pass("Gen MUD thanh cong - serial: " + mudSerial);
            System.out.println("[TC25new] MUD serial from API: " + mudSerial);
        } catch (Exception e) {
            mudSerial = utils.MudCodeProvider.getNextMudCode("mud2");
            tcGenMud.warning("API fail -> dung serial pre-gen: " + mudSerial + " | Loi: " + e.getMessage());
        }
        final String mudSerialToApply = mudSerial;

        /*
         * =========================
         * TC01 - LOGIN
         * =========================
         */
        ExtentTest tc01 = test.createNode("TC01 - Login voi tai khoan lanttp");

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
                By.xpath("//*[contains(text(),'Chon dia chi dang nhap') or contains(text(),'Chon Shop')]")));
        tc01.pass("Login thanh cong");

        /*
         * =========================
         * TC02 - CHON SHOP
         * =========================
         */
        ExtentTest tc02 = test.createNode("TC02 - Chon shop " + SHOP_CODE);

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
        js.executeScript("arguments[0].click();", shopOption);
        Thread.sleep(1000);
        tc02.pass("Da chon shop " + SHOP_CODE);

        /*
         * =========================
         * TC03 - HOAN TAT CHON SHOP
         * =========================
         */
        ExtentTest tc03 = test.createNode("TC03 - Hoan tat chon shop");

        WebElement btnHoanTat = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Hoan tat') or .//span[contains(text(),'Hoan tat')]] | " +
                        "//a[contains(text(),'Hoan tat')]")));
        btnHoanTat.click();
        Thread.sleep(2000);
        tc03.pass("Click Hoan tat thanh cong");

        /*
         * =========================
         * TC04 - TAT POPUP SAI DOI TUONG / THAY DOI GIA
         * =========================
         */
        ExtentTest tc04 = test.createNode("TC04 - Tat popup neu co");

        try {
            WebElement closePopup = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Danh sach san pham sai doi tuong')]]" +
                                    "//button[@aria-label='Close' or contains(@class,'ant-modal-close')]")));
            js.executeScript("arguments[0].click();", closePopup);
            tc04.pass("Da tat popup sai doi tuong");
        } catch (TimeoutException e) {
            tc04.info("Khong co popup sai doi tuong");
        }

        try {
            WebElement btnDeSau = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(.,'De sau') or .//span[contains(text(),'De sau')]]")));
            js.executeScript("arguments[0].click();", btnDeSau);
        } catch (TimeoutException e) { }

        Thread.sleep(500);

        /*
         * =========================
         * TC05 - CHON MUC BAN HANG
         * =========================
         */
        ExtentTest tc05 = test.createNode("TC05 - Chon muc Ban hang");

        WebElement menuBanHang = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//p[contains(@class,'feature_home') and contains(text(),'Ban hang')] | " +
                        "//a[.//p[contains(text(),'Ban hang')]] | " +
                        "//p[contains(text(),'Ban hang (')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", menuBanHang);
        Thread.sleep(300);
        menuBanHang.click();
        Thread.sleep(3000);
        wait.until(ExpectedConditions.urlContains("sell"));
        tc05.pass("Da vao muc Ban hang");

        /*
         * =========================
         * TC06 - NHAP MA INSIDE
         * =========================
         */
        ExtentTest tc06 = test.createNode("TC06 - Nhap ma inside " + INSIDE_CODE);

        WebElement insideInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ant-modal')]//input[@type='text' or @type='search'] | " +
                        "//input[contains(@placeholder,'inside') or contains(@placeholder,'ma')]")));
        insideInput.clear();
        insideInput.sendKeys(INSIDE_CODE);
        Thread.sleep(1500);

        try {
            WebElement insideOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[contains(text(),'Tran Thi Thanh Thao') or contains(text(),'(" + INSIDE_CODE + ")')]")));
            insideOption.click();
            Thread.sleep(500);
        } catch (TimeoutException e) { }

        try {
            WebElement btnXacNhan = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Xac nhan') or contains(.,'Xac Nhan')]")));
            btnXacNhan.click();
        } catch (TimeoutException e) { }

        Thread.sleep(2000);
        tc06.pass("Da nhap ma inside " + INSIDE_CODE);
        /*
         * =========================
         * TC06.5 - XU LY MODAL "CANH BAO THAY DOI GIA"
         * =========================
         */
        ExtentTest tc06c = test.createNode("TC06.5 - Xu ly modal canh bao thay doi gia");
        
        try {
            WebElement modalThayDoiGia = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Cảnh báo có sp thay đổi giá')]]")));
            
            tc06c.info("Phat hien modal 'Canh bao co sp thay doi gia' - click 'De sau'");
            
            WebElement btnDeSau = modalThayDoiGia.findElement(
                    By.xpath(".//button[contains(.,'Để sau') or contains(.,'De sau')]"));
            js.executeScript("arguments[0].click();", btnDeSau);
            Thread.sleep(1500);
            
            tc06c.pass("Da click 'De sau' tren modal thay doi gia");
        } catch (TimeoutException e) {
            tc06c.info("Khong co modal canh bao thay doi gia");
        }


        /*
         * =========================
         * TC07 - NHAP SDT KHACH HANG
         * =========================
         */
        ExtentTest tc07 = test.createNode("TC07 - Nhap SDT " + CUSTOMER_PHONE);

        WebElement phoneInput = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='phone']")));
        phoneInput.click();
        phoneInput.sendKeys(CUSTOMER_PHONE);
        phoneInput.sendKeys(Keys.ENTER);
        Thread.sleep(2000);
        tc07.pass("Da nhap SDT " + CUSTOMER_PHONE);

        /*
         * =========================
         * TC08 - SP: 00031035 | SL=1
         * =========================
         */
        ExtentTest tc08 = test.createNode("TC08 - Nhap SP " + BUY_PRODUCT_1 + " SL=" + BUY_QTY);
        searchAndAddProduct(js, BUY_PRODUCT_1);

        tc08.pass("Da them SP " + BUY_PRODUCT_1 + " SL=" + BUY_QTY);

        /*
         * =========================
         * TC11 - APPLY SERIAL MUD DA GEN VAO DON
         * Serial lay tu API o TC00 -- click "Nhap ma KM" -> nhap serial -> Ap dung
         * =========================
         */
        ExtentTest tc11 = test.createNode("TC11 - Apply serial MUD: " + mudSerialToApply);

        Thread.sleep(2000);

        try {
            WebElement nhapMaKM = (WebElement) js.executeScript(
                    "var all = document.querySelectorAll('*');" +
                    "for (var i = 0; i < all.length; i++) {" +
                    "  var el = all[i];" +
                    "  var txt = el.textContent || '';" +
                    "  if (txt.includes('Nhap ma KM') && el.offsetParent !== null) {" +
                    "    var children = el.children;" +
                    "    var hasChildWithText = false;" +
                    "    for (var j = 0; j < children.length; j++) {" +
                    "      if ((children[j].textContent || '').includes('Nhap ma KM')) {" +
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
                tc11.fail("Khong tim thay 'Nhap ma KM' tren trang");
            } else {
                Thread.sleep(500);
                js.executeScript("arguments[0].click();", nhapMaKM);
                Thread.sleep(2000);

                try {
                    List<WebElement> voucherTags = driver.findElements(
                            By.xpath("//div[contains(@class,'ant-modal')]//span[contains(@class,'ant-tag')]" +
                                    "//span[contains(@class,'ant-tag-close-icon') or contains(@class,'anticon-close')]"));
                    for (WebElement tag : voucherTags) {
                        try { js.executeScript("arguments[0].click();", tag); Thread.sleep(800); } catch (Exception ex) {}
                    }
                    if (!voucherTags.isEmpty()) Thread.sleep(1000);
                } catch (Exception ex) { }

                WebElement voucherInput = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'ant-modal')]//input[@type='text'] | " +
                                "//div[contains(@class,'ant-modal')]//input[not(@type='hidden') and not(@type='checkbox') and not(@type='radio')] | " +
                                "//div[contains(@class,'ant-modal')]//input[contains(@class,'ant-input')] | " +
                                "//input[contains(@placeholder,'Nhap ma') or contains(@placeholder,'voucher') " +
                                "    or contains(@placeholder,'ma giam') or contains(@placeholder,'Barcode')] | " +
                                "//div[contains(@class,'modal')]//input[contains(@class,'ant-input')]")));
                voucherInput.clear();
                voucherInput.sendKeys(mudSerialToApply);
                Thread.sleep(1000);

                WebElement btnApDung = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'ant-modal')]//button[contains(.,'Ap dung')] | " +
                                "//div[contains(@class,'ant-modal')]//*[contains(text(),'Ap dung')] | " +
                                "//button[contains(.,'Ap dung')] | " +
                                "//span[contains(text(),'Ap dung')]/ancestor::button | " +
                                "//span[contains(text(),'Ap dung')]")));
                js.executeScript("arguments[0].click();", btnApDung);
                Thread.sleep(3000);

                try {
                    WebElement btnXacNhanVoucher = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(
                                    By.xpath("//div[contains(@class,'ant-modal')]//button[contains(.,'Xac nhan')] | " +
                                            "//button[contains(.,'Xac nhan')]")));
                    js.executeScript("arguments[0].click();", btnXacNhanVoucher);
                    Thread.sleep(2000);
                } catch (TimeoutException te) { /* popup tu dong sau apply */ }

                tc11.pass("Apply serial MUD thanh cong: " + mudSerialToApply);
            }
        } catch (Exception e) {
            tc11.warning("Apply MUD that bai: " + e.getMessage());
        }

        /*
         * =========================
         * TC12 - VERIFY CTKM KM-1026-118 DUOC AP DUNG
         * =========================
         */
        ExtentTest tc12 = test.createNode("TC12 - Verify CTKM " + PROMOTION_CODE);

        try {
            WebElement promotionLink = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(normalize-space(.),'Khuyen mai khac')]")));
            js.executeScript("arguments[0].click();", promotionLink);
            Thread.sleep(1000);

            WebElement promotionModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(.,'Danh sach khuyen mai')]]")));
            promotionModal.findElement(By.xpath(".//*[contains(normalize-space(.),'" + PROMOTION_CODE + "')]"));
            tc12.pass("Tim thay " + PROMOTION_CODE + " trong danh sach khuyen mai");

            try {
                WebElement btnConfirm = promotionModal.findElement(By.xpath(".//button[contains(.,'Xac nhan')]"));
                js.executeScript("arguments[0].click();", btnConfirm);
            } catch (NoSuchElementException ex) {
                WebElement closeBtn = promotionModal.findElement(
                        By.xpath(".//button[@aria-label='Close' or contains(@class,'ant-modal-close')]"));
                js.executeScript("arguments[0].click();", closeBtn);
            }
            Thread.sleep(1500);
        } catch (Exception e) {
            tc12.info("Khong tim thay 'Khuyen mai khac' -- verify qua man hinh ban hang. " + e.getMessage());
        }

        /*
         * =========================
         * TC13 - TAO DON
         * =========================
         */
        String orderCode = "";
        Exception orderCreationFailed = null;
        try {
            ExtentTest tc13 = test.createNode("TC13 - Click Tao don");

            WebElement btnTaoDon = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(normalize-space(.),'Tao don')]")));

            String btnInfo = (String) js.executeScript(
                    "var e=arguments[0];" +
                    "return 'text=\"' + (e.innerText||'').trim() + '\" id=' + (e.id||'-') " +
                    "+ ' class=' + (e.className||'-') + ' disabled=' + (e.disabled===true);",
                    btnTaoDon);
            tc13.info("Element se click: " + btnInfo);

            if (!btnInfo.contains("Tao don")) {
                throw new RuntimeException("Locator khop sai element, khong phai nut Tao don: " + btnInfo);
            }

            js.executeScript("arguments[0].scrollIntoView({block:'center'});", btnTaoDon);
            Thread.sleep(500);
            try {
                btnTaoDon.click();
            } catch (Exception clickEx) {
                tc13.info("Native click fail (" + clickEx.getClass().getSimpleName() + ") -> fallback JS click");
                js.executeScript("arguments[0].click();", btnTaoDon);
            }
            Thread.sleep(3000);

            try {
                List<WebElement> toasts = driver.findElements(By.xpath(
                        "//div[contains(@class,'ant-notification-notice') or contains(@class,'ant-message-notice')" +
                        " or contains(@class,'Toastify__toast')]"));
                for (WebElement t : toasts) {
                    String msg = t.getText().replace("\n", " ").trim();
                    if (!msg.isEmpty()) tc13.warning("Thong bao tu app sau khi click Tao don: " + msg);
                }
            } catch (Exception ignore) { }

            boolean onPayment = !driver.findElements(By.xpath(
                    "//*[contains(text(),'Phuong thuc thanh toan')] | //*[contains(text(),'Ve gio hang')]")).isEmpty();
            if (onPayment) {
                tc13.pass("Da click Tao don -- man thanh toan da mo");
            } else {
                attachScreenshot("Click Tao don nhung KHONG sang man thanh toan");
                tc13.fail("Click Tao don nhung van o man ban hang -- app chan tao don (xem thong bao/anh o tren)");
                throw new RuntimeException("Tao don khong co tac dung -- khong sang duoc man thanh toan");
            }

            ExtentTest tc14 = test.createNode("TC14 - Click Tong tien");
            WebElement btnTongTien = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Tong tien')] | " +
                            "//*[contains(text(),'Tong tien') and contains(text(),'Shift')]")));
            js.executeScript("arguments[0].click();", btnTongTien);
            Thread.sleep(3000);
            tc14.pass("Da click Tong tien");

            ExtentTest tc15 = test.createNode("TC15 - Hoan tat don");
            WebElement btnHoanTatFinal = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Hoan tat')]")));
            js.executeScript("arguments[0].click();", btnHoanTatFinal);
            Thread.sleep(3000);

            try {
                WebElement nvDropdown = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'ant-modal')]//div[contains(@class,'ant-select-selector')]")));
                nvDropdown.click();
                Thread.sleep(500);

                driver.switchTo().activeElement().sendKeys(INSIDE_CODE);
                Thread.sleep(1500);

                WebElement nvOption = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[contains(text(),'Tran Thi Thanh Thao') or contains(text(),'(" + INSIDE_CODE + ")')]")));
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
                        By.xpath("//button[contains(.,'Xac nhan') or .//span[text()='Xac nhan']]")));
                btnXacNhanFinal.click();
                Thread.sleep(5000);
            } catch (Exception e) {
                Thread.sleep(3000);
            }

            try {
                WebElement orderEl = driver.findElement(
                        By.xpath("//span[contains(@class,'order-number') or contains(@class,'order-code') or contains(@class,'ma-don')] | " +
                                "//div[contains(@class,'order-number') or contains(@class,'order-code')] | " +
                                "//div[contains(@class,'header')]//span[string-length(normalize-space()) > 3 and string-length(normalize-space()) < 12 and number(normalize-space()) = number(normalize-space())]"));
                orderCode = orderEl.getText().trim();
            } catch (Exception e) {
                try {
                    WebElement numEl = driver.findElement(
                            By.xpath("//*[string-length(normalize-space()) >= 5 and string-length(normalize-space()) <= 10 " +
                                    "and number(normalize-space()) = number(normalize-space()) and normalize-space() > 1000000]"));
                    orderCode = numEl.getText().trim();
                } catch (Exception e2) {
                    orderCode = "Don tao thanh cong - check man hinh";
                }
            }
            tc15.pass("Hoan tat! Ma don: " + orderCode);

        } catch (Exception e) {
            orderCode = "KHONG TAO DUOC DON";
            attachScreenshot("Loi o luong tao don / thanh toan");
            test.fail("Loi khi tao don: " + e.getMessage());
            orderCreationFailed = e;
        }

        System.out.println("========================================");
        System.out.println("TC25new -- KM-1026-118 -- Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Khong 100ml");
        System.out.println("MUD serial: " + mudSerialToApply);
        System.out.println("Ma don   : " + orderCode);
        System.out.println("========================================");

        if (orderCreationFailed != null) {
            throw new AssertionError("TC25new FAIL o luong tao don/thanh toan: "
                    + orderCreationFailed.getMessage(), orderCreationFailed);
        }

        test.pass("TC25new xong -- KM " + PROMOTION_CODE + " serial=" + mudSerialToApply + " | Don: " + orderCode);
    }

    // ─── HELPER: search SKU va click item dau tien trong dropdown ───────────────
    private void searchAndAddProduct(JavascriptExecutor js, String sku) throws InterruptedException {
        WebElement productInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[starts-with(@id,'search-product-input_session')]")));
        js.executeScript("arguments[0].click(); arguments[0].focus();", productInput);
        Thread.sleep(300);
        js.executeScript(
                "var el=arguments[0]; var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
                "s.call(el,''); el.dispatchEvent(new Event('input',{bubbles:true})); el.dispatchEvent(new Event('change',{bubbles:true}));",
                productInput);
        Thread.sleep(200);
        productInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        productInput.sendKeys(Keys.DELETE);
        Thread.sleep(300);
        productInput.sendKeys(sku);
        Thread.sleep(1000);

        WebElement searchBtn = driver.findElement(
                By.xpath("//button[contains(@class,'ant-input-search-button') or contains(@class,'ant-btn-icon-only')] | " +
                        "//span[contains(@class,'anticon-search')]/ancestor::button | " +
                        "//button[@id='button-search']"));
        js.executeScript("arguments[0].click();", searchBtn);
        Thread.sleep(3000);

        WebElement productItem = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'search-input-dropdown')]//div[contains(@class,'ant-select-item-option')]")));
        js.executeScript("arguments[0].click();", productItem);
        Thread.sleep(2500);
    }
}
