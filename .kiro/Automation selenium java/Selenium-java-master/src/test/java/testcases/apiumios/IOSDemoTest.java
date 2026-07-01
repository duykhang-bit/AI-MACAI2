package testcases.apiumios;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.testng.annotations.Test;

import java.net.URL;

public class IOSDemoTest {

    @Test
    public void openIOS() throws Exception {

        // cấu hình Appium
        XCUITestOptions options = new XCUITestOptions();

        options.setPlatformName("iOS");

        options.setAutomationName("XCUITest");

        // attach vào simulator đang mở
        options.setUdid(
                "7719B026-36FB-47E3-9B2D-EFE73538C1F5"
        );

        // mở app Settings
        options.setBundleId(
                "com.apple.Preferences"
        );

        // không reset máy
        options.setNoReset(true);

        System.out.println("Start Appium Session...");

        IOSDriver driver =
                new IOSDriver(
                        new URL("http://127.0.0.1:4723"),
                        options
                );

        System.out.println("Connected");

        Thread.sleep(10000);

        driver.quit();

        System.out.println("Done");
    }
}