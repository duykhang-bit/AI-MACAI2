import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.URL;

public class IOSLaunchTest {

    public static void main(String[] args) throws Exception {
        // Configure XCUITest capabilities for iOS Simulator / Cấu hình capabilities cho iOS Simulator
        XCUITestOptions options = new XCUITestOptions();
        options.setPlatformName("iOS");
        options.setPlatformVersion("18.4");
        options.setDeviceName("iPhone 16 Plus");
        options.setBundleId("com.apple.Preferences");

        // Connect to local Appium server / Kết nối tới Appium server local
        IOSDriver driver = new IOSDriver(
                new URL("http://127.0.0.1:4723"),
                options
        );

        // Keep session open briefly to verify launch / Giữ session mở ngắn để xác nhận app đã mở
        Thread.sleep(5000);

        driver.quit();
    }
}
