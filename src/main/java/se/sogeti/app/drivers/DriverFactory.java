package se.sogeti.app.drivers;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sogeti.app.config.Settings;

public class DriverFactory {

    private DriverFactory() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final Settings settings = Settings.getInstance();

    public static WebDriver createInstance(String browserType) throws MalformedURLException {
        WebDriver driver = null;

        if (browserType.toLowerCase().contains("chrome")) {
            switch (settings.getDriverRunner()) {
                case "local":
                    driver = createLocalChromeDriver();
                    break;
                case "remote":
                    driver = createRemoteChromeDriver();
                    break;
                default:
                    LOGGER.error(
                            "The value for settings.getDriverPath() is not correct; Valid options are: local and remote");
            }
        }
        return driver;
    }

    private static WebDriver createLocalChromeDriver() {
        final String osName = System.getProperty("os.name");

        if (osName.contains("Linux")) {
            System.setProperty("webdriver.chrome.driver", settings.getDriverPath().concat("chromedriver_linux"));
        } else if (osName.contains("Windows")) {
            System.setProperty("webdriver.chrome.driver", settings.getDriverPath().concat("chromedriver_win.exe"));
        } else if (osName.contains("Mac")) {
            System.setProperty("webdriver.chrome.driver", settings.getDriverPath().concat("chromedriver_mac"));
        } else {
            LOGGER.error("Non compatible operative system!");
        }

        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.addArguments("--disable-gpu", "--blink-settings=imagesEnabled=false");

        HashMap<String, Object> images = new HashMap<>();
        images.put("images", 2);
        HashMap<String, Object> prefs = new HashMap<>();
        options.setExperimentalOption("prefs", prefs);
        prefs.put("profile.default_content_setting_values", images);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(settings.getPageLoadTimeout(), TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(settings.getImplicitWaitTimeout(), TimeUnit.SECONDS);
        return driver;
    }

    // If you have remoteDriver, make sure to generalize the URL
    private static WebDriver createRemoteChromeDriver() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);

        WebDriver driver = new RemoteWebDriver(new URL(""), options);
        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(settings.getPageLoadTimeout(), TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(settings.getImplicitWaitTimeout(), TimeUnit.SECONDS);
        return driver;
    }
}