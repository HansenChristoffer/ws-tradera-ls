package se.sogeti.app.drivers;

import org.openqa.selenium.WebDriver;

public class DriverManager {
    private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<>();

    public static WebDriver getWebDriver() {
        return webDriver.get();
    }

    public static void setWebDriver(WebDriver driver) {
        webDriver.set(driver);
    }

    public static void closeDriver() {
        if (getWebDriver() != null) {
            try {
                getWebDriver().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                getWebDriver().quit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        webDriver.remove();
    }
}