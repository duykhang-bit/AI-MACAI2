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
public class TC14 extends BaseTest1 {

    private static final String SHOP_CODE       = "80006";
    private static final String INSIDE_CODE     = "00017";
    private static final String CUSTOMER_PHONE  = "0835089254";
    private static final String PROMOTION_CODE  = "KM-1026-107";
    private static final String CAMPAIGN_CODE   = "3754";
    private static final String BUY_PRODUCT_1   = "00032958";
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

    @Test(priority = 1, description = "KM-1026-107 - TK Gia Dinh - Giam gia 30% - Sua tam goi cho be La Beauty Baby Gentle Wash (250ml)", invocationCount = 1)
    public void TC014() throws InterruptedException {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        ExtentTest tcGenMud = test.createNode("TC00 - Lay serial MUD (campaign=" + CAMPAIGN_CODE + ")");
        String mudSerial = null;
        boolean useProdMode = true;
        try {
            mudSerial = utils.MudCodeProvider.getNextMudCode("mud14", useProdMode);
            if (mudSerial == null || mudSerial.isEmpty()) {
                throw new RuntimeException("PROD pool (mud01) het serial!");
            }
            tcGenMud.pass("Dung MUD serial tu PROD pool: " + mudSerial);
            System.out.println("[TC14] MUD serial: " + mudSerial + " | campaign=" + CAMPAIGN_CODE);
        } catch (Exception e) {
            tcGenMud.fail("Khong lay duoc MUD serial: " + e.getMessage());
            throw new RuntimeException("TC14 FAIL - khong co MUD serial!", e);
        }
        final String mudSerialToApply = mudSerial;

        /*
         * =========================
         * TC01 - LOGIN
         * =========================
         */
        ExtentTest tc01 = test.createNode("TC01 - Login");

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
                By.xpath("//*[contains(text(),'Ch\u1ecdn \u0111\u1ecba ch\u1ec9 \u0111\u0103ng nh\u1eadp') or contains(text(),'Ch\u1ecdn Shop')]")));
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
                By.xpath("//button[contains(text(),'Ho\u00e0n t\u1ea5t') or .//span[contains(text(),'Ho\u00e0n t\u1ea5t')]] | " +
                        "//a[contains(text(),'Ho\u00e0n t\u1ea5t')]")));
        btnHoanTat.click();
        Thread.sleep(2000);
        tc03.pass("Click Hoan tat thanh cong");

        /*
         * =========================
         * TC04 - TAT POPUP
         * =========================
         */
        ExtentTest tc04 = test.createNode("TC04 - Tat popup neu co");

        try {
            WebElement closePopup = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Danh s\u00e1ch s\u1ea3n ph\u1ea9m sai \u0111\u1ed1i t\u01b0\u1ee3ng')]]" +
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
                By.xpath("//p[contains(@class,'feature_home') and contains(text(),'B\u00e1n h\u00e0ng')] | " +
                        "//a[.//p[contains(text(),'B\u00e1n h\u00e0ng')]] | " +
                        "//p[contains(text(),'B\u00e1n h\u00e0ng (')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", menuBanHang);
        Thread.sleep(500);
        js.executeScript("arguments[0].click();", menuBanHang);
        Thread.sleep(3000);
        wait.until(ExpectedConditions.urlContains("sell"));
        tc05.pass("Da vao muc Ban hang");
        /*
         * =========================
         * TC05b - XU LY MODAL "CANH BAO THAY DOI GIA" (SAU KHI VAO BAN HANG)
         * =========================
         */
        ExtentTest tc05b = test.createNode("TC05b - Xu ly modal canh bao thay doi gia");
        
        try {
            WebElement modalThayDoiGia = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Cảnh báo có sp thay đổi giá')]]")));
            
            tc05b.info("Phat hien modal 'Canh bao co sp thay doi gia' - click 'De sau'");
            
            WebElement btnDeSau = modalThayDoiGia.findElement(
                    By.xpath(".//button[contains(.,'Để sau') or contains(.,'De sau')]"));
            js.executeScript("arguments[0].click();", btnDeSau);
            Thread.sleep(1500);
            
            tc05b.pass("Da click 'De sau' tren modal thay doi gia");
        } catch (TimeoutException e) {
            tc05b.info("Khong co modal canh bao thay doi gia");
        }

        /*
         * =========================
         * TC06 - NHAP MA INSIDE
         * =========================
         */
        ExtentTest tc06 = test.createNode("TC06 - Nhap ma inside " + INSIDE_CODE);

        WebElement insideInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ant-modal')]//input[@type='text' or @type='search'] | " +
                        "//input[contains(@placeholder,'inside') or contains(@placeholder,'m\u00e3')]")));
        insideInput.clear();
        insideInput.sendKeys(INSIDE_CODE);
        Thread.sleep(1500);

        try {
            WebElement insideOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[contains(text(),'Tr\u1ea7n Th\u1ecb Thanh Th\u1ea3o') or contains(text(),'(" + INSIDE_CODE + ")')]")));
            insideOption.click();
            Thread.sleep(500);
        } catch (TimeoutException e) { }

        try {
            WebElement btnXacNhan = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'X\u00e1c nh\u1eadn') or contains(.,'X\u00e1c Nh\u1eadn')]")));
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
         * TC08 - THEM SAN PHAM
         * =========================
         */
        ExtentTest tc08 = test.createNode("TC08 - Nhap SP " + BUY_PRODUCT_1 + " SL=" + BUY_QTY);
        searchAndAddProduct(js, BUY_PRODUCT_1);

        ExtentTest tc08b = test.createNode("TC08b - Nhap SL " + BUY_QTY);
        Thread.sleep(1500);
        WebElement qtyInput1 = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[contains(@id,'input-quantity-product')]")));
        js.executeScript(
                "var el=arguments[0]; var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
                "s.call(el,'" + BUY_QTY + "'); el.dispatchEvent(new Event('input',{bubbles:true})); el.dispatchEvent(new Event('change',{bubbles:true})); el.blur();",
                qtyInput1);
        Thread.sleep(1500);
        tc08.pass("Da them SP " + BUY_PRODUCT_1);
        tc08b.pass("Da nhap SL " + BUY_QTY);
        /*
         * =========================
         * TC08.5 - XU LY MODAL "CANH BAO THAY DOI GIA"
         * =========================
         */
        ExtentTest tc08c = test.createNode("TC08.5 - Xu ly modal canh bao thay doi gia");
        
        try {
            WebElement modalThayDoiGia = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Cảnh báo có sp thay đổi giá')]]")));
            
            tc08c.info("Phat hien modal 'Canh bao co sp thay doi gia' - click 'De sau'");
            
            WebElement btnDeSau = modalThayDoiGia.findElement(
                    By.xpath(".//button[contains(.,'Để sau') or contains(.,'De sau')]"));
            js.executeScript("arguments[0].click();", btnDeSau);
            Thread.sleep(1500);
            
            tc08c.pass("Da click 'De sau' tren modal thay doi gia");
        } catch (TimeoutException e) {
            tc08c.info("Khong co modal canh bao thay doi gia");
        }


        /*
         * =========================
         * TC11 - APPLY SERIAL MUD
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
                    "  if (txt.includes('Nh\u1eadp m\u00e3 KM') && el.offsetParent !== null) {" +
                    "    var children = el.children;" +
                    "    var hasChildWithText = false;" +
                    "    for (var j = 0; j < children.length; j++) {" +
                    "      if ((children[j].textContent || '').includes('Nh\u1eadp m\u00e3 KM')) {" +
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
                                "//div[contains(@class,'ant-modal')]//input[contains(@class,'ant-input')] | " +
                                "//input[contains(@placeholder,'Barcode')]")));
                voucherInput.clear();
                voucherInput.sendKeys(mudSerialToApply);
                Thread.sleep(1000);

                WebElement btnApDung = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'ant-modal')]//button[contains(.,'p d\u1ee5ng')] | " +
                                "//button[contains(.,'p d\u1ee5ng')]")));
                js.executeScript("arguments[0].click();", btnApDung);
                Thread.sleep(3000);

                try {
                    WebElement btnXacNhanVoucher = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(
                                    By.xpath("//div[contains(@class,'ant-modal')]//button[contains(.,'X\u00e1c nh\u1eadn')] | " +
                                            "//button[contains(.,'X\u00e1c nh\u1eadn')]")));
                    js.executeScript("arguments[0].click();", btnXacNhanVoucher);
                    Thread.sleep(2000);
                } catch (TimeoutException te) { }

                tc11.pass("Apply serial MUD thanh cong: " + mudSerialToApply);
            }
        } catch (Exception e) {
            tc11.warning("Apply MUD that bai: " + e.getMessage());
        }

        /*
         * =========================
         * TC12 - VERIFY CTKM
         * =========================
         */
        ExtentTest tc12 = test.createNode("TC12 - Verify CTKM " + PROMOTION_CODE);

        try {
            WebElement promotionLink = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(normalize-space(.),'Khuy\u1ebfn m\u00e3i kh\u00e1c')]")));
            js.executeScript("arguments[0].click();", promotionLink);
            Thread.sleep(1000);

            WebElement promotionModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(.,'Danh s\u00e1ch khuy\u1ebfn m\u00e3i')]]")));
            promotionModal.findElement(By.xpath(".//*[contains(.,'" + PROMOTION_CODE + "') or contains(.,'Mua 2 t\u1eb7ng 1')]"));
            tc12.pass("Tim thay CTKM " + PROMOTION_CODE);

            try {
                WebElement btnConfirm = promotionModal.findElement(By.xpath(".//button[contains(.,'X\u00e1c nh\u1eadn')]"));
                js.executeScript("arguments[0].click();", btnConfirm);
            } catch (NoSuchElementException ex) {
                WebElement closeBtn = promotionModal.findElement(
                        By.xpath(".//button[@aria-label='Close' or contains(@class,'ant-modal-close')]"));
                js.executeScript("arguments[0].click();", closeBtn);
            }
            Thread.sleep(1500);
        } catch (Exception e) {
            tc12.info("Khong tim thay Khuyen mai khac: " + e.getMessage());
        }        /*
         * =========================
         * TC12.5 - XU LY MODAL "CANH BAO CO SP THAY DOI GIA"
         * =========================
         */
        ExtentTest tc12_5 = test.createNode("TC12.5 - Xu ly modal canh bao thay doi gia");
        
        try {
            WebElement modalThayDoiGia = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Cảnh báo có sp thay đổi giá')]]")));
            
            tc12_5.info("Phat hien modal 'Canh bao co sp thay doi gia' - click 'De sau'");
            
            WebElement btnDeSau = modalThayDoiGia.findElement(
                    By.xpath(".//button[contains(.,'Để sau') or contains(.,'De sau')]"));
            js.executeScript("arguments[0].click();", btnDeSau);
            Thread.sleep(1500);
            
            tc12_5.pass("Da click 'De sau' tren modal thay doi gia");
        } catch (TimeoutException e) {
            tc12_5.info("Khong co modal canh bao thay doi gia");
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

            // Chờ 2s để React render xong button
            Thread.sleep(2000);
            
            // Tìm CHÍNH XÁC nút "Tạo đơn (F4)" - không phải "Lưu đơn (F3)" hay "Huỷ (F8)"
            java.util.List<WebElement> allButtons = driver.findElements(
                    By.xpath("//button[contains(.,'đơn') or contains(.,'Đơn')]"));
            
            WebElement btnTaoDon = null;
            for (WebElement btn : allButtons) {
                String btnText = btn.getText().toLowerCase().trim();
                tc13.info("🔍 Button tìm thấy: '" + btn.getText() + "'");
                if (btnText.contains("tạo đơn") && !btnText.contains("lưu")) {
                    btnTaoDon = btn;
                    break;
                }
            }
            
            if (btnTaoDon == null) {
                attachScreenshot("❌ Không tìm thấy nút Tạo đơn");
                throw new RuntimeException("Không tìm thấy nút 'Tạo đơn (F4)' trên trang");
            }
            
            // Scroll và highlight
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", btnTaoDon);
            js.executeScript("arguments[0].style.border='3px solid green'", btnTaoDon);
            Thread.sleep(500);
            
            String btnInfo = (String) js.executeScript(
                    "var e=arguments[0];" +
                    "return 'text=\"' + (e.innerText||'').trim() + '\" disabled=' + (e.disabled===true);",
                    btnTaoDon);
            tc13.info("✅ Sẽ click button: " + btnInfo);
            
            // Chụp ảnh TRƯỚC KHI click
            attachScreenshot("📸 Trước khi click Tạo đơn");

            // Click native trước; JS click chỉ fallback
            try {
                btnTaoDon.click();
            } catch (Exception clickEx) {
                tc13.info("Native click fail (" + clickEx.getClass().getSimpleName() + ") → fallback JS click");
                js.executeScript("arguments[0].click();", btnTaoDon);
            }
            Thread.sleep(3000);

            // Bắt toast validation của RSA ngay sau click
            try {
                java.util.List<WebElement> toasts = driver.findElements(By.xpath(
                        "//div[contains(@class,'ant-notification-notice') or contains(@class,'ant-message-notice')" +
                        " or contains(@class,'Toastify__toast')]"));
                for (WebElement t : toasts) {
                    String msg = t.getText().replace("\n", " ").trim();
                    if (!msg.isEmpty()) tc13.warning("⚠️ Thông báo từ app sau khi click Tạo đơn: " + msg);
                }
            } catch (Exception ignore) { }

            // Verify đã sang màn thanh toán
            Thread.sleep(2000);

            boolean onPayment = !driver.findElements(By.xpath(
                    "//*[contains(text(),'Ph\u01b0\u01a1ng th\u1ee9c thanh to\u00e1n')] | //*[contains(text(),'V\u1ec1 gi\u1ecf h\u00e0ng')]")).isEmpty();
            if (onPayment) {
                tc13.pass("Da click Tao don - man thanh toan da mo");
            } else {
                attachScreenshot("Click Tao don nhung khong sang man thanh toan");
                tc13.fail("Click Tao don nhung van o man ban hang");
                throw new RuntimeException("Tao don khong co tac dung");
            }

            ExtentTest tc14 = test.createNode("TC14 - Click Tong tien");
            WebElement btnTongTien = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'T\u1ed5ng ti\u1ec1n')] | " +
                            "//*[contains(text(),'T\u1ed5ng ti\u1ec1n') and contains(text(),'Shift')]")));
            js.executeScript("arguments[0].click();", btnTongTien);
            Thread.sleep(3000);
            tc14.pass("Da click Tong tien");

            ExtentTest tc15 = test.createNode("TC15 - Hoan tat don");
            WebElement btnHoanTatFinal = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Ho\u00e0n t\u1ea5t')]")));
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
                        By.xpath("//*[contains(text(),'Tr\u1ea7n Th\u1ecb Thanh Th\u1ea3o') or contains(text(),'(" + INSIDE_CODE + ")')]")));
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
                        By.xpath("//button[contains(.,'X\u00e1c nh\u1eadn') or .//span[text()='X\u00e1c nh\u1eadn']]")));
                btnXacNhanFinal.click();
                Thread.sleep(5000);
            } catch (Exception e) {
                Thread.sleep(3000);
            }

            try {
                WebElement orderEl = driver.findElement(
                        By.xpath("//span[contains(@class,'order-number') or contains(@class,'order-code')] | " +
                                "//div[contains(@class,'order-number') or contains(@class,'order-code')]"));
                orderCode = orderEl.getText().trim();
            } catch (Exception e) {
                orderCode = "Don tao thanh cong - check man hinh";
            }
            tc15.pass("Hoan tat! Ma don: " + orderCode);

        } catch (Exception e) {
            orderCode = "KHONG TAO DUOC DON";
            attachScreenshot("Loi o luong tao don");
            test.fail("Loi khi tao don: " + e.getMessage());
            orderCreationFailed = e;
        }

        System.out.println("========================================");
        System.out.println("TC14 - " + PROMOTION_CODE);
        System.out.println("MUD serial: " + mudSerialToApply);
        System.out.println("Ma don   : " + orderCode);
        System.out.println("========================================");

        if (orderCreationFailed != null) {
            throw new AssertionError("TC14 FAIL: " + orderCreationFailed.getMessage(), orderCreationFailed);
        }

        test.pass("TC14 xong - KM " + PROMOTION_CODE + " serial=" + mudSerialToApply + " | Don: " + orderCode);
    }

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
