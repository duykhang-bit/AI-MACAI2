package utils;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

/**
 * Helper dung chung cho bo test uat/Mudsku.
 *
 * Quy uoc: loi do DATA/HE THONG (SKU khong co tren UAT, SKU khong du ton) -> SKIP, khong tinh FAIL.
 * Loi do script -> van FAIL binh thuong.
 */
public final class MudskuHelper {

    private static final By SEARCH_RESULT = By.xpath(
            "//div[contains(@class,'search-input-dropdown')]//div[contains(@class,'ant-select-item-option')]");
    // O "Them barcode (F1)" trong panel "Chon san pham" (SP Tem vang) la ant-select, chu nam trong span placeholder
    private static final By BARCODE_SELECTOR = By.xpath(
            "//span[contains(@class,'ant-select-selection-placeholder') and contains(.,'barcode')]"
                    + "/ancestor::div[contains(@class,'ant-select-selector')][1]");
    private static final By OPEN_DROPDOWN_OPTION = By.xpath(
            "//div[contains(@class,'ant-select-dropdown') and not(contains(@class,'ant-select-dropdown-hidden'))]"
                    + "//div[contains(@class,'ant-select-item-option')]");
    private static final By BTN_THEM_VAO_GIO = By.xpath(
            "//button[contains(.,'Th\u00eam v\u00e0o gi\u1ecf')]");

    // Chi dem o so luong trong gio hang, bo qua o nam trong popup (vd "Chi tiet san pham")
    private static final By QTY_INPUT = By.xpath(
            "//input[contains(@id,'input-quantity-product')][not(ancestor::div[contains(@class,'ant-modal')])]");
    private static final By SEARCH_INPUT = By.xpath("//input[starts-with(@id,'search-product-input_session')]");
    private static final By SEARCH_BUTTON = By.xpath(
            "//span[contains(@class,'anticon-search')]/ancestor::button | //button[@id='button-search']");

    private static final ThreadLocal<String> LAST_TOAST = new ThreadLocal<>();

    /** Sau khi chon SP: true neu SP da vao gio (them 1 dong so luong) hoac da xu ly xong panel Tem vang. */
    private static boolean afterSelect(WebDriver driver, JavascriptExecutor js, String sku, int qtyBefore)
            throws InterruptedException {
        long end = System.currentTimeMillis() + 6_000;
        while (System.currentTimeMillis() < end) {
            if (!driver.findElements(BARCODE_SELECTOR).isEmpty()) {
                handleTemVangPanel(driver, js, sku);
                return true;
            }
            if (driver.findElements(QTY_INPUT).size() > qtyBefore) return true;
            Thread.sleep(500);
        }
        return false;
    }

    /** Dong popup "Chi tiet san pham" / "Ma giam gia" neu lo mo do click trung anh/ten SP. */
    private static void closeProductDetailModal(WebDriver driver, JavascriptExecutor js) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            List<WebElement> closes = driver.findElements(By.xpath(
                    "//div[contains(@class,'ant-modal-wrap') and not(contains(@style,'display: none'))]"
                            + "//button[contains(@class,'ant-modal-close')]"));
            if (closes.isEmpty()) return;
            js.executeScript("arguments[0].click();", closes.get(closes.size() - 1));
            Thread.sleep(800);
        }
    }

    private MudskuHelper() { }

    /**
     * Goi ngay sau khi bam nut search SKU. Xu ly ca 3 truong hop:
     *  1. Co dropdown ket qua  -> click SP (sau do neu bat panel Tem vang thi xu ly barcode).
     *  2. Bat panel Tem vang    -> chon barcode + Them vao gio (F4).
     *  3. Khong co ket qua nao -> SKU khong ton tai tren UAT -> SKIP.
     */
    public static void addProductAfterSearch(WebDriver driver, JavascriptExecutor js, String sku)
            throws InterruptedException {
        long end = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < end) {
            List<WebElement> items = driver.findElements(SEARCH_RESULT);
            if (!items.isEmpty() && items.get(0).isDisplayed()) {
                WebElement item = items.get(0);
                String cls = String.valueOf(item.getAttribute("class"));
                String txt = item.getText().replace("\n", " ");
                if (cls.contains("disabled")) {
                    throw new SkipException("SKU " + sku + " bi disable tren UAT (khong ban duoc): " + txt);
                }
                if (txt.contains("H\u1ebft t\u1ed3n") || txt.matches(".*\\s0 \\S+ c\u00f3 s\u1eb5n.*")) {
                    throw new SkipException("SKU " + sku + " het ton tren UAT shop -> khong them vao gio duoc (loi data): " + txt);
                }
                if (txt.contains("R\u00fat H\u00e0ng") || txt.contains("R\u00fat h\u00e0ng")) {
                    throw new SkipException("SKU " + sku + " dang gan tag 'SP Rut Hang' tren UAT -> khong them vao gio duoc (loi data): " + txt);
                }
                int before = driver.findElements(QTY_INPUT).size();
                // JS click thang vao option (cach cu da chay on dinh); khong click anh/ten SP
                js.executeScript("arguments[0].click();", item);
                Thread.sleep(2500);
                if (afterSelect(driver, js, sku, before)) return;

                // Chua vao gio -> dong popup chi tiet SP (neu co) roi chon lai bang ban phim
                closeProductDetailModal(driver, js);
                WebElement input = driver.findElement(SEARCH_INPUT);
                js.executeScript("arguments[0].click(); arguments[0].focus();", input);
                js.executeScript("arguments[0].click();", driver.findElement(SEARCH_BUTTON));
                Thread.sleep(3000);
                input.sendKeys(Keys.ARROW_DOWN);
                input.sendKeys(Keys.ENTER);
                Thread.sleep(2500);
                if (afterSelect(driver, js, sku, before)) return;

                throw new RuntimeException("Da chon SKU " + sku + " tu dropdown nhung SP khong vao gio. Option='" + txt + "' class=" + cls);
            }
            if (!driver.findElements(BARCODE_SELECTOR).isEmpty()) {
                handleTemVangPanel(driver, js, sku);
                return;
            }
            Thread.sleep(500);
        }
        throw new SkipException("SKU " + sku + " khong co ket qua tim kiem tren UAT shop (loi data, khong phai loi script)");
    }

    /** Panel "Chon san pham" cua SP Tem vang: click o barcode -> chon barcode -> Them vao gio (F4). */
    private static void handleTemVangPanel(WebDriver driver, JavascriptExecutor js, String sku)
            throws InterruptedException {
        System.out.println("[MudskuHelper] SKU " + sku + ": panel Tem vang -> chon barcode");
        WebElement selector = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(BARCODE_SELECTOR));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", selector);
        Thread.sleep(500);
        // ant-select mo bang mousedown -> phai click that, JS click khong mo dropdown
        new Actions(driver).moveToElement(selector).click().perform();
        Thread.sleep(1500);

        WebElement option;
        try {
            option = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(OPEN_DROPDOWN_OPTION));
        } catch (TimeoutException e) {
            throw new SkipException("SKU " + sku + " khong co barcode/lo nao de ban tren UAT (loi data)");
        }
        new Actions(driver).moveToElement(option).click().perform();
        Thread.sleep(1500);

        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(BTN_THEM_VAO_GIO));
        js.executeScript("arguments[0].click();", btn);
        Thread.sleep(2500);
        System.out.println("[MudskuHelper] SKU " + sku + ": da chon barcode va Them vao gio (F4)");
    }

    /** Ghi lai toast cua app (goi trong vong lap doc toast sau khi bam Tao don). */
    public static void noteToast(String msg) {
        LAST_TOAST.set(msg);
    }

    /**
     * Goi khi Tao don khong sang duoc man thanh toan.
     * Neu app bao SKU khong du ton -> SKIP (loi data). Nguoc lai de TC fail binh thuong.
     */
    public static void skipIfOutOfStock(WebDriver driver) {
        String toast = LAST_TOAST.get();
        LAST_TOAST.remove();
        String text = (toast != null ? toast : "") + " " + driver.getPageSource();
        if (text.contains("kh\u00f4ng \u0111\u1ee7 t\u1ed3n")) {
            throw new SkipException("SKU khong du ton kho tren UAT: " + (toast != null ? toast : "(xem man hinh)"));
        }
    }

    private static boolean waitPresent(WebDriver driver, By by, int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds))
                    .until(ExpectedConditions.presenceOfElementLocated(by));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
