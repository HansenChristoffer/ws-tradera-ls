package se.sogeti.app.scrapers;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import se.sogeti.app.config.Constants;
import se.sogeti.app.database.Database;
import se.sogeti.app.drivers.DriverFactory;
import se.sogeti.app.drivers.DriverManager;
import se.sogeti.app.models.dto.LinkDTO;

public class LinkScraper implements Runnable, BaseScraper {

  public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

  private volatile boolean isRunning = true;
  private final Database<LinkDTO> database = new Database<>();

  private Set<LinkDTO> fetchedLinks = new HashSet<>();
  private Set<LinkDTO> formerLinks = new HashSet<>();

  @Override
  public void run() {
    long elapsedTime = System.currentTimeMillis();
    WebDriver driver = startDriver("chrome");

    try {
      fetchAndEnterCategory(driver);

      new WebDriverWait(driver, 5)
          .until(ExpectedConditions.elementToBeClickable(By.xpath(Constants.BUTTON_COOKIES_SELECTOR))).click();
      LOGGER.info("Closed \"accept cookie popup\" window!");

      while (isRunning) {
        LOGGER.info("URL == {}", driver.getCurrentUrl());
        Document doc = Jsoup.parse(driver.getPageSource());

        if (doc.select(Constants.ZERO_DATA_RESULTS).isEmpty() && isNotErrorPage(doc)) {
          Elements links = doc.select(Constants.SELECT_LINK_PATTERN);

          ZonedDateTime zdt = ZonedDateTime.parse(database.getPublished(links.get(0).attr("href").split("/")[3]));

          if (!links.isEmpty() && isValidDate(zdt)) {
            fetchedLinks.clear();
            Thread.sleep(500);

            links.forEach(link -> {
              if (!link.parent().parent().getElementsByAttributeValue("class", "text-danger text-truncate mr-2")
                  .isEmpty()) {
                fetchedLinks.add(new LinkDTO((Constants.BASE_URL + link.attr("href"))));
              }
            });

            LOGGER.info("Links added, FetchedLinks.size == {}", fetchedLinks.size());
            LOGGER.info("FormerLinks.size == {}", fetchedLinks.size());
            fetchedLinks.removeAll(formerLinks);

            LOGGER.info("FormerLinks removed, FetchedLinks.size == {}", fetchedLinks.size());

            formerLinks.clear();
            formerLinks = database.postMultiple(fetchedLinks, "http://".concat(Constants.databaseIp).concat(":")
                .concat(Constants.databasePort).concat("/api/links/all"));
            LOGGER.info("Saved links, FormerLinks.size == {}", fetchedLinks.size());

            String nextHref = "";
            nextHref = doc.select(Constants.NEXT_BUTTON_SELECTOR).attr("href");

            LOGGER.info("nextHref == {}", nextHref);
            if (!nextHref.isBlank()) {
              LOGGER.info(" - GETTING NEW PAGE - ");
              driver.get(Constants.BASE_URL.concat(nextHref));
              waitForPageLoad(Constants.CATEGORY_TITLE_ON_LINK_PAGE, driver, 10);
            } else {
              fetchAndEnterCategory(driver);
            }
          } else {
            fetchAndEnterCategory(driver);
          }
        } else {
          fetchAndEnterCategory(driver);
        }
      }

    } catch (InterruptedException ie) {
      LOGGER.error("run().InterruptedException == {}", ie.getMessage());
      kill();
      DriverManager.closeDriver();
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      LOGGER.error("run().Exception == {}", e.getMessage());
      kill();
      DriverManager.closeDriver();
    } finally {
      LOGGER.info("Elapsed time: {}s", (System.currentTimeMillis() - elapsedTime) / 1000);
      kill();
      DriverManager.closeDriver();
    }
  }

  // Will fetch the links advert by the passed object number, use the published
  // date and check if it is within 24 hours from start of the scraper
  private boolean isValidDate(ZonedDateTime zdt) {
    return (zdt.isAfter(Constants.getDateTimeNow().minusDays(1))
        && zdt.isBefore(Constants.getDateTimeNow().plusMinutes(1)));
  }

  private void fetchAndEnterCategory(WebDriver driver) {
    formerLinks.clear();
    String url = Constants.BASE_URL.concat(database.fetchOpenCategory().getHref())
        .concat(Constants.FILTER_SETTINGS_URL);
    driver.get(url);
    waitForPageLoad(Constants.CATEGORY_TITLE_ON_LINK_PAGE, driver, 10);
  }

  private boolean isNotErrorPage(Document doc) {
    return doc.select("#view > article > div > h2").isEmpty();
  }

  public WebDriver startDriver(String browser) {
    WebDriver webDriver = null;

    try {
      webDriver = DriverFactory.createInstance(browser);
      DriverManager.setWebDriver(webDriver);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    return webDriver;
  }

  public void takeScreenshot(String saveLocation, WebDriver driver) {
    try {
      Random rand = new Random();
      StringBuilder imgName = new StringBuilder();

      Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
          .takeScreenshot(driver);

      for (int i = 0; i < 10; i++) {
        imgName.append(String.valueOf(rand.nextInt(9)));
      }

      ImageIO.write(screenshot.getImage(), "PNG", new File(saveLocation.concat(imgName.toString()).concat(".png")));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void waitForPageLoad(String selector, WebDriver driver, int time) {
    WebDriverWait wait = new WebDriverWait(driver, time);

    try {
      wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
    } catch (TimeoutException te) {
      takeScreenshot(Constants.SCREENSHOT_PATH, driver);
      LOGGER.error("waitForPageLoad().TimeoutException == {} - {}", te.getCause(), te.getMessage());
    }
  }

  // Kills the scraper by telling all the loops to stop. Garbage collector will
  // handle the thread
  @Override
  public void kill() {
    isRunning = false;
  }

}
