package testcases.uat.PromotionRsaOffline;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
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

    private static final String SHOP_CODE = "80006";
    private static final String INSIDE_CODE = "00017";
    private static final String INSIDE_NAME = "Trần Thị Thanh Thảo";
    private static final String CUSTOMER_PHONE = "0835089255";

    private static final String PROMOTION_CODE = "KM-0926-313";
    private static final String BUY_PRODUCT_CODE = "00502498";
    private static final String BUY_PRODUCT_NAME = "NƯỚC BÙ ĐIỆN GIẢI KAMIZOL VỊ CHANH 250ML";
    private static final long BUY_PRODUCT_PRICE = 252000L;
    private static final String GIFT_PRODUCT_CODE = "00502497";
    private static final String GIFT_PRODUCT_NAME = "NƯỚC BÙ ĐIỆN GIẢI KAMIZOL VỊ CAM 250ML";

    @Override
    protected String getBaseUrl() {
        return "https://uat-rsa-web.frt.vn/";
    }

    @Override
    @BeforeMethod
    public void setup(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        if (description != null && !description.isEmpty()) {
            testName += " - " + description;
        }
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @Test(
            priority = 1,
            description = "Mua Kamizol chanh 00502498 - tặng Kamizol cam 00502497 từ kho KM, CTKM KM-0926-313",
            invocationCount = 1)
    public void TC024() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        login();
        selectShop(js);
        dismissOptionalPopups(js);
        openSalesScreen(js);
        selectInside();
        enterCustomerPhone();
        addBuyProduct(js);
        verifyBuyProduct();
        verifyPromotion(js);
        verifyGiftProduct();
        String orderCode = completeOrder(js);

        test.pass("✅ PASS TC24: mua " + BUY_PRODUCT_CODE
                + " tặng " + GIFT_PRODUCT_CODE
                + " từ kho khuyến mãi, CTKM " + PROMOTION_CODE
                + ". Mã đơn: " + orderCode);
    }

    private void login() throws InterruptedException {
        ExtentTest step = test.createNode("TC24.01 - Đăng nhập RSA UAT");

        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
                By.name("LoginInput.UserNameOrEmailAddress")));
        username.clear();
        username.sendKeys("lanttp");

        WebElement password = wait.until(ExpectedConditions.elementToBeClickable(
                By.name("LoginInput.Password")));
        password.clear();
        password.sendKeys("123456");
        driver.findElement(By.id("kt_login_signin_submit")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Chọn địa chỉ đăng nhập') or contains(text(),'Chọn Shop')]")));
        Thread.sleep(500);
        step.pass("Đăng nhập thành công");
    }

    private void selectShop(JavascriptExecutor js) throws InterruptedException {
        ExtentTest step = test.createNode("TC24.02 - Chọn shop " + SHOP_CODE);

        WebElement shopDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ant-select')]//div[contains(@class,'ant-select-selector')]")));
        shopDropdown.click();

        WebElement shopSearchInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ant-select-dropdown')]//input | "
                        + "//input[contains(@class,'ant-select-selection-search-input')]")));
        shopSearchInput.sendKeys(SHOP_CODE);

        WebElement shopOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ant-select-item-option') and contains(.,'" + SHOP_CODE + "')]")));
        shopOption.click();

        WebElement completeButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Hoàn tất')] | //a[contains(.,'Hoàn tất')]")));
        js.executeScript("arguments[0].click();", completeButton);
        Thread.sleep(1500);
        step.pass("Đã chọn shop " + SHOP_CODE);
    }

    private void dismissOptionalPopups(JavascriptExecutor js) {
        dismissModalButton(js, "Danh sách sản phẩm sai đối tượng", "Close");
        dismissModalButton(js, "Cảnh báo có sp thay đổi giá", "Để sau");

        try {
            WebElement closeAd = new WebDriverWait(driver, Duration.ofSeconds(3)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath(
                            "//div[contains(@class,'ant-modal') or contains(@class,'popup')]"
                                    + "//button[contains(@class,'close') or @aria-label='Close'] | "
                                    + "//span[contains(@class,'anticon-close')]/ancestor::button")));
            js.executeScript("arguments[0].click();", closeAd);
        } catch (TimeoutException ignored) {
            // Popup quảng cáo không xuất hiện.
        }
    }

    private void dismissModalButton(JavascriptExecutor js, String modalTitle, String buttonText) {
        try {
            WebElement button = new WebDriverWait(driver, Duration.ofSeconds(4)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath(
                            "//*[contains(@class,'ant-modal') and .//*[contains(text(),'" + modalTitle + "')]]"
                                    + "//button[@aria-label='" + buttonText + "' or contains(.,'" + buttonText + "')]")));
            js.executeScript("arguments[0].click();", button);
        } catch (TimeoutException ignored) {
            // Popup tùy chọn không xuất hiện.
        }
    }

    private void openSalesScreen(JavascriptExecutor js) {
        ExtentTest step = test.createNode("TC24.03 - Mở màn hình Bán hàng");
        WebElement salesMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//p[contains(@class,'feature_home') and contains(text(),'Bán hàng')] | "
                        + "//a[.//p[contains(text(),'Bán hàng')]] | "
                        + "//p[contains(text(),'Bán hàng (')]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", salesMenu);
        wait.until(ExpectedConditions.urlContains("sell"));
        step.pass("Đã mở màn hình Bán hàng");
    }

    private void selectInside() throws InterruptedException {
        ExtentTest step = test.createNode("TC24.04 - Chọn inside " + INSIDE_CODE);
        WebElement insideInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//div[contains(@class,'modal') or contains(@class,'popup') or contains(@class,'ant-modal')]"
                        + "//input[@type='text' or @type='search'] | "
                        + "//input[contains(@placeholder,'inside') or contains(@placeholder,'mã')]")));
        insideInput.clear();
        insideInput.sendKeys(INSIDE_CODE);

        WebElement insideOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(text(),'" + INSIDE_NAME + "') or contains(text(),'("
                        + INSIDE_CODE + ")')]")));
        insideOption.click();

        try {
            WebElement confirmButton = new WebDriverWait(driver, Duration.ofSeconds(4)).until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Xác nhận')]")));
            confirmButton.click();
        } catch (TimeoutException ignored) {
            // Popup tự đóng sau khi chọn inside.
        }
        Thread.sleep(1000);
        step.pass("Đã chọn " + INSIDE_NAME + " (" + INSIDE_CODE + ")");
    }

    private void enterCustomerPhone() throws InterruptedException {
        ExtentTest step = test.createNode("TC24.05 - Nhập khách hàng " + CUSTOMER_PHONE);
        WebElement phoneInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='phone']")));
        phoneInput.clear();
        phoneInput.sendKeys(CUSTOMER_PHONE);
        phoneInput.sendKeys(org.openqa.selenium.Keys.ENTER);
        Thread.sleep(1000);
        step.pass("Đã nhập SĐT khách hàng");
    }

    private void addBuyProduct(JavascriptExecutor js) throws InterruptedException {
        ExtentTest step = test.createNode("TC24.06 - Thêm sản phẩm mua " + BUY_PRODUCT_CODE);
        WebElement productInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[starts-with(@id,'search-product-input_session')]")));
        setReactInputValue(js, productInput, BUY_PRODUCT_CODE);

        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//input[starts-with(@id,'search-product-input_session')]"
                        + "/ancestor::*[contains(@class,'ant-input-search')][1]//button | "
                        + "//button[@id='button-search']")));
        js.executeScript("arguments[0].click();", searchButton);

        By exactProductResult = By.xpath(
                "//div[contains(@class,'search-input-dropdown')]"
                        + "//div[contains(@class,'ant-select-item-option')]"
                        + "[contains(.,'" + BUY_PRODUCT_CODE + "') or contains(.,'" + BUY_PRODUCT_NAME + "')]"
        );
        WebElement productItem = wait.until(ExpectedConditions.elementToBeClickable(exactProductResult));
        js.executeScript("arguments[0].click();", productItem);
        Thread.sleep(2000);

        WebElement buyRow = findProductContainer(
                BUY_PRODUCT_CODE, BUY_PRODUCT_NAME, BUY_PRODUCT_PRICE, false);
        List<WebElement> quantityInputs = buyRow.findElements(
                By.xpath(".//input[contains(@id,'input-quantity-product')]"));
        Assert.assertFalse(quantityInputs.isEmpty(),
                "Không tìm thấy ô số lượng trong dòng sản phẩm " + BUY_PRODUCT_CODE);
        setReactInputValue(js, quantityInputs.get(0), "1");
        Thread.sleep(2000);
        step.pass("Đã thêm đúng sản phẩm " + BUY_PRODUCT_CODE + " với số lượng 1");
    }

    private void verifyBuyProduct() {
        ExtentTest step = test.createNode("TC24.07 - Verify sản phẩm mua và giá 252.000đ");
        WebElement buyRow = findProductContainer(
                BUY_PRODUCT_CODE, BUY_PRODUCT_NAME, BUY_PRODUCT_PRICE, false);

        Assert.assertTrue(normalizeText(buyRow.getText()).contains(normalizeText(BUY_PRODUCT_NAME)),
                "Sai tên sản phẩm mua. Nội dung dòng: " + buyRow.getText());
        Assert.assertTrue(containsMoneyValue(buyRow, BUY_PRODUCT_PRICE),
                "Không tìm thấy giá 252.000đ trong dòng sản phẩm " + BUY_PRODUCT_CODE
                        + ". Nội dung: " + buyRow.getText());
        assertQuantity(buyRow, "1", BUY_PRODUCT_CODE);
        step.pass("Sản phẩm mua đúng mã, tên, số lượng 1 và giá 252.000đ");
    }

    private void verifyPromotion(JavascriptExecutor js) throws InterruptedException {
        ExtentTest step = test.createNode("TC24.08 - Verify CTKM " + PROMOTION_CODE);
        WebElement promotionLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[contains(normalize-space(.),'Khuyến mãi khác')]")));
        js.executeScript("arguments[0].click();", promotionLink);

        WebElement promotionModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//*[contains(@class,'ant-modal') and .//*[contains(.,'Danh sách khuyến mãi')]]")));
        WebElement promotion = promotionModal.findElement(
                By.xpath(".//*[contains(normalize-space(.),'" + PROMOTION_CODE + "')]"));
        Assert.assertTrue(promotion.isDisplayed(),
                "Không tìm thấy CTKM " + PROMOTION_CODE + " trong Danh sách khuyến mãi");

        if (isPromotionSelected(promotion)) {
            step.pass("Tìm thấy CTKM " + PROMOTION_CODE + " và có trạng thái được chọn");
        } else {
            step.info("Tìm thấy CTKM " + PROMOTION_CODE
                    + "; UI không expose được thuộc tính checked, trạng thái áp dụng sẽ được xác minh bằng dòng quà tặng");
        }

        List<WebElement> confirmButtons = promotionModal.findElements(
                By.xpath(".//button[contains(.,'Xác nhận') or contains(.,'Xác Nhận')]"));
        if (!confirmButtons.isEmpty()) {
            js.executeScript("arguments[0].click();", confirmButtons.get(0));
        } else {
            WebElement closeButton = promotionModal.findElement(By.xpath(
                    ".//button[@aria-label='Close' or contains(@class,'ant-modal-close')]"));
            js.executeScript("arguments[0].click();", closeButton);
        }
        Thread.sleep(1500);
    }

    private void verifyGiftProduct() {
        ExtentTest step = test.createNode(
                "TC24.09 - Verify quà " + GIFT_PRODUCT_CODE + " giá 0đ từ kho KM");
        WebElement giftRow = findProductContainer(
                GIFT_PRODUCT_CODE, GIFT_PRODUCT_NAME, 0L, true);
        String giftText = normalizeText(giftRow.getText());

        Assert.assertTrue(giftText.contains(normalizeText(GIFT_PRODUCT_NAME)),
                "Sai tên sản phẩm tặng. Nội dung dòng: " + giftRow.getText());
        assertQuantity(giftRow, "1", GIFT_PRODUCT_CODE);
        Assert.assertTrue(containsMoneyValue(giftRow, 0L),
                "Quà tặng " + GIFT_PRODUCT_CODE + " không có giá 0đ. Nội dung: " + giftRow.getText());
        Assert.assertTrue(
                giftText.contains("KHO HÀNG KM") || giftText.contains("KHO HANG KM"),
                "Quà tặng không lấy từ kho khuyến mãi (Kho hàng KM). Nội dung: " + giftRow.getText());

        step.pass("Quà đúng mã, tên, số lượng 1, giá 0đ và kho hàng KM");
    }

    private String completeOrder(JavascriptExecutor js) throws InterruptedException {
        ExtentTest step = test.createNode("TC24.10 - Tạo và hoàn tất đơn hàng");

        WebElement createOrderButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//button[contains(@class,'btn_container') or contains(@id,'btn_finish')] | "
                        + "//button[.//span[contains(text(),'Tạo đơn')]]")));
        js.executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();",
                createOrderButton);
        Thread.sleep(2000);

        WebElement totalButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Tổng tiền')] | "
                        + "//*[contains(text(),'Tổng tiền') and contains(text(),'Shift')]")));
        js.executeScript("arguments[0].click();", totalButton);
        Thread.sleep(2000);

        WebElement finalCompleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Hoàn tất')]")));
        js.executeScript("arguments[0].click();", finalCompleteButton);
        Thread.sleep(2500);
        confirmInsideIfRequested(js);

        String orderCode = readOrderCode();
        step.pass("Đã hoàn tất luồng tạo đơn. Mã đơn: " + orderCode);
        return orderCode;
    }

    private void confirmInsideIfRequested(JavascriptExecutor js) throws InterruptedException {
        try {
            WebElement modal = new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(
                            "//*[contains(@class,'ant-modal') and "
                                    + ".//*[contains(.,'inside') or contains(.,'Inside') or contains(.,'nhân viên')]]")));

            List<WebElement> inputs = modal.findElements(By.xpath(
                    ".//input[contains(@placeholder,'inside') or contains(@placeholder,'Inside') "
                            + "or contains(@placeholder,'Nhập mã')]"));
            if (!inputs.isEmpty()) {
                setReactInputValue(js, inputs.get(0), INSIDE_CODE);
            }

            List<WebElement> insideOptions = driver.findElements(
                    By.xpath("//*[contains(text(),'" + INSIDE_NAME + "') or contains(text(),'("
                            + INSIDE_CODE + ")')]"));
            if (!insideOptions.isEmpty()) {
                js.executeScript("arguments[0].click();", insideOptions.get(insideOptions.size() - 1));
            }

            List<WebElement> dayInputs = modal.findElements(
                    By.xpath(".//input[contains(@placeholder,'ngày') or contains(@placeholder,'số ngày')]"));
            if (!dayInputs.isEmpty()) {
                setReactInputValue(js, dayInputs.get(0), "1");
            }

            WebElement confirmButton = modal.findElement(
                    By.xpath(".//button[contains(.,'Xác nhận')]"));
            js.executeScript("arguments[0].click();", confirmButton);
            Thread.sleep(3000);
        } catch (TimeoutException ignored) {
            // Đơn không yêu cầu xác nhận inside lần hai.
        }
    }

    private String readOrderCode() {
        List<WebElement> orderElements = driver.findElements(By.xpath(
                "//span[contains(@class,'order-number') or contains(@class,'order-code') or contains(@class,'ma-don')] | "
                        + "//*[contains(text(),'Mã đơn')]/following::*[1]"));
        for (WebElement element : orderElements) {
            String value = element.getText().trim();
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "Không hiển thị - kiểm tra lịch sử đơn UAT";
    }

    private WebElement findProductContainer(
            String productCode,
            String productName,
            long expectedPrice,
            boolean requirePromotionWarehouse) {
        return new WebDriverWait(driver, Duration.ofSeconds(30)).until(currentDriver -> {
            List<WebElement> anchors = currentDriver.findElements(By.xpath(
                    "//*[contains(normalize-space(text()),'" + productCode + "')]"));
            for (WebElement anchor : anchors) {
                if (!anchor.isDisplayed()) {
                    continue;
                }
                WebElement candidate = anchor;
                for (int level = 0; level < 12; level++) {
                    String text = normalizeText(candidate.getText());
                    boolean hasIdentity = text.contains(productCode)
                            && text.contains(normalizeText(productName));
                    boolean hasPrice = containsMoneyValue(candidate, expectedPrice);
                    boolean hasWarehouse = !requirePromotionWarehouse
                            || text.contains("KHO HÀNG KM")
                            || text.contains("KHO HANG KM");
                    boolean hasQuantity = requirePromotionWarehouse
                            ? containsQuantity(candidate, "1")
                            : !candidate.findElements(
                                    By.xpath(".//input[contains(@id,'input-quantity-product')]")).isEmpty();
                    if (hasIdentity && hasPrice && hasWarehouse && hasQuantity) {
                        return candidate;
                    }
                    try {
                        candidate = candidate.findElement(By.xpath(".."));
                    } catch (NoSuchElementException e) {
                        break;
                    }
                }
            }
            return null;
        });
    }

    private void assertQuantity(WebElement container, String expected, String productCode) {
        Assert.assertTrue(containsQuantity(container, expected),
                "Sản phẩm " + productCode + " không có số lượng " + expected
                        + ". Nội dung dòng: " + container.getText());
    }

    private boolean containsQuantity(WebElement container, String expected) {
        List<WebElement> inputs = container.findElements(By.xpath(".//input"));
        for (WebElement input : inputs) {
            if (expected.equals(input.getAttribute("value"))) {
                return true;
            }
        }
        for (WebElement leaf : leafElements(container)) {
            if (expected.equals(leaf.getText().trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsMoneyValue(WebElement container, long expected) {
        for (WebElement leaf : leafElements(container)) {
            String text = leaf.getText().trim();
            String digits = text.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                try {
                    if (Long.parseLong(digits) == expected) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    // Không phải giá tiền hợp lệ.
                }
            }
        }
        return false;
    }

    private List<WebElement> leafElements(WebElement container) {
        return container.findElements(By.xpath(".//*[not(*) and normalize-space(text()) != '']"));
    }

    private boolean isPromotionSelected(WebElement promotion) {
        WebElement candidate = promotion;
        for (int level = 0; level < 8; level++) {
            String className = String.valueOf(candidate.getAttribute("class")).toLowerCase(Locale.ROOT);
            String ariaChecked = candidate.getAttribute("aria-checked");
            if (className.contains("checked") || className.contains("selected")
                    || className.contains("active") || "true".equalsIgnoreCase(ariaChecked)
                    || !candidate.findElements(By.cssSelector(
                            "input[type='checkbox']:checked, input[type='radio']:checked, "
                                    + ".ant-checkbox-checked, .ant-radio-checked")).isEmpty()) {
                return true;
            }
            try {
                candidate = candidate.findElement(By.xpath(".."));
            } catch (NoSuchElementException e) {
                break;
            }
        }
        return false;
    }

    private void setReactInputValue(JavascriptExecutor js, WebElement input, String value) {
        js.executeScript(
                "var el=arguments[0], value=arguments[1];"
                        + "var setter=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;"
                        + "setter.call(el,value);"
                        + "el.dispatchEvent(new Event('input',{bubbles:true}));"
                        + "el.dispatchEvent(new Event('change',{bubbles:true}));"
                        + "el.blur();",
                input,
                value);
    }

    private String normalizeText(String value) {
        return value == null
                ? ""
                : value.replace('\u00A0', ' ')
                        .replaceAll("\\s+", " ")
                        .trim()
                        .toUpperCase(Locale.ROOT);
    }
}
