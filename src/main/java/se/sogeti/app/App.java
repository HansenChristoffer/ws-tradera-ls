package se.sogeti.app;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sogeti.app.config.Settings;
import se.sogeti.app.drivers.DriverManager;
import se.sogeti.app.scrapers.BaseScraper;
import se.sogeti.app.scrapers.LinkScraper;

public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static Set<BaseScraper> scrapers = new HashSet<>();
	private static Set<Thread> threads = new HashSet<>();

	public static void main(String[] args) {
		app();
	}

	private static void app() {
		try {
			Settings settings = Settings.getInstance();
			settings.updateSettings();
		} catch (Exception e) {
			LOGGER.error("app.Exception == {}", e.getMessage());
		}
	}

	private static void linkScraperTest() {
		try {
			LinkScraper ls1 = new LinkScraper();
			Thread t1 = new Thread(ls1, "T1-LS");

			scrapers.addAll(Arrays.asList(ls1));
			threads.addAll(Arrays.asList(t1));

			t1.start();

		} catch (Exception e) {
			LOGGER.error("linkScraperTest.Exception == {}", e.getMessage());
			kill();
			DriverManager.closeDriver();
		}
	}

	private static void sleep(long seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			LOGGER.error("sleep.InterruptedException == {}", e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

	private static void kill() {
		try {
			if (!scrapers.isEmpty()) {
				scrapers.forEach(s -> s.kill());
				scrapers.clear();
			}
		} catch (Exception e) {
			LOGGER.info("kill().Exception == {}", e.getMessage());
		}
	}

}
