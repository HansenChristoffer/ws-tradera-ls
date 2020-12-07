package se.sogeti.app.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sogeti.app.controllers.Controller;

public class Settings {
    // Misc
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static volatile Settings instance = null;

    // DEFAULTS
    private static final String DEFAULT_BASE_URL = "https://www.tradera.com";
    private static final String DEFAULT_API_URL = "https://webscraperapi-1606300858222.azurewebsites.net";
    private static final String DEFAULT_FILTER_URL = "?sortBy=AddedOn&sellerType=Private";
    private static final String DEFAULT_DRIVER_RUNNER = "local";
    private static final String DEFAULT_DRIVERS_PATH = "/usr/bin/chromedriver";
    private static final String DEFAULT_SCREENSHOT_PATH = "./data/screenshots/";
    private static final String DEFAULT_CONFIG_PATH = "./config/";
    private static final String DEFAULT_INTERNAL_USER_AGENT = "Scraper HttpClient JDK11+";
    private static final String DEFAULT_EXTERNAL_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36";
    private static final long DEFAULT_PAGE_LOAD_TIMEOUT = 45;
    private static final long DEFAULT_IMPLICIT_WAIT_TIMEOUT = 10;
    private static final boolean DEFAULT_IS_SCHEDULED = false; // Will NOT go by specific time of the day, instead uses
                                                               // isActive calls with timeout
    private static final String DEFAULT_ACTIVE_TIME_OF_DAY = "12:00"; // At what time of the day the
                                                                      // isActive call should happened,
                                                                      // ONLY if isScheduled == true
    private static final long DEFAULT_ACTIVE_CALL_TIMEOUT = 120; // 2 seconds

    // Date & time related
    private static final String DEFAULT_TIME_FORMAT_PATTERN = ":01.1+01:00[Europe/Paris]";
    private static final String DEFAULT_TIME_ZONE_ID = "Europe/Paris";

    // Selectors
    public static final String DEFAULT_BUTTON_NEXT = "body > div.site-container.bp > div.site-main > div.search-results-page.site-width > div > div > div.col-md-9.col-sm-12.search-results-wrapper > div.pb-3 > nav > ul > li:nth-child(3) > a";
    public static final String DEFAULT_BUTTON_COOKIE = "//*[@id=\"qc-cmp2-ui\"]/div[2]/div/button[2]";
    public static final String DEFAULT_SELECT_LINK = "a[id*=item_]";
    public static final String DEFAULT_SELECT_ZERO_DATA = "article.data-zero-result";
    public static final String DEFAULT_SELECT_PAGE_LOADED = "body > div.site-container.bp > div.site-main > div.search-results-page.site-width > div > div > div.col-md-9.col-sm-12.search-results-wrapper > header > div.search-results-header__heading.row.justify-content-between.pb-3.py-md-3 > div.col-12.d-flex.align-items-center.justify-content-center.justify-content-md-start > h1";

    // Scheduling related

    // Settings
    private String baseUrl = DEFAULT_BASE_URL;
    private String filterUrl = DEFAULT_FILTER_URL;

    private String apiURL = DEFAULT_API_URL;
    private String apiVersion = "";

    private String driverRunner = DEFAULT_DRIVER_RUNNER;
    private String driverPath = DEFAULT_DRIVERS_PATH;
    private String screenshotPath = DEFAULT_SCREENSHOT_PATH;
    private String configPath = DEFAULT_CONFIG_PATH;

    private String internalUserAgent = DEFAULT_INTERNAL_USER_AGENT;
    private String externalUserAgent = DEFAULT_EXTERNAL_USER_AGENT;

    private long pageLoadTimeout = DEFAULT_PAGE_LOAD_TIMEOUT;
    private long implicitWaitTimeout = DEFAULT_IMPLICIT_WAIT_TIMEOUT;

    private String timeFormatPattern = DEFAULT_TIME_FORMAT_PATTERN;
    private String timeZoneId = DEFAULT_TIME_ZONE_ID;

    private String buttonNext = DEFAULT_BUTTON_NEXT;
    private String buttonCookie = DEFAULT_BUTTON_COOKIE;
    private String selectLink = DEFAULT_SELECT_LINK;
    private String selectZeroData = DEFAULT_SELECT_ZERO_DATA;
    private String selectPageLoaded = DEFAULT_SELECT_PAGE_LOADED;

    private boolean isScheduled = DEFAULT_IS_SCHEDULED;
    private long activeCallTimeout = DEFAULT_ACTIVE_CALL_TIMEOUT;
    private String activeCallTimeOfDay = DEFAULT_ACTIVE_TIME_OF_DAY;

    private ZonedDateTime dateTimeNow;

    private static final String SETTINGS_FILE_PATH = DEFAULT_CONFIG_PATH.concat("linkscraper-settings.xml");

    private Settings() {
        initFileStructure();
    }

    public static Settings getInstance() {
        if (instance == null) {
            synchronized (Settings.class) {
                if (instance == null) {
                    instance = new Settings();
                }
            }
        }

        return instance;
    }

    public void updateSettings() {
        LOGGER.info("Updating...");

        fetchSettingsFile(fetchApiURL());

        try {
            LOGGER.info("Initilizing settings...");
            FileInputStream fis = new FileInputStream(new File(SETTINGS_FILE_PATH));

            Properties prop = getSortedPropertiesInstance();

            prop.loadFromXML(fis);

            baseUrl = prop.getProperty("base_url");
            filterUrl = prop.getProperty("filter_url");

            apiURL = prop.getProperty("api_url");
            apiVersion = prop.getProperty("api_version");

            driverRunner = prop.getProperty("driver_runner");
            driverPath = prop.getProperty("driver_path");
            screenshotPath = prop.getProperty("screenshot_path");

            internalUserAgent = prop.getProperty("internal_user_agent");
            externalUserAgent = prop.getProperty("external_user_agent");

            pageLoadTimeout = prop.getProperty("page_load_timeout") != null
                    ? Long.valueOf(prop.getProperty("page_load_timeout"))
                    : DEFAULT_PAGE_LOAD_TIMEOUT;
            implicitWaitTimeout = prop.getProperty("implicit_wait_timeout") != null
                    ? Long.valueOf(prop.getProperty("implicit_wait_timeout"))
                    : DEFAULT_IMPLICIT_WAIT_TIMEOUT;

            buttonNext = prop.getProperty("button_next");
            buttonCookie = prop.getProperty("button_cookie");
            selectLink = prop.getProperty("select_link");
            selectZeroData = prop.getProperty("select_zero_data");
            selectPageLoaded = prop.getProperty("select_page_loaded");

            isScheduled = Boolean.parseBoolean(prop.getProperty("is_scheduled"));

            activeCallTimeout = prop.getProperty("active_call_timeout") != null
                    ? Long.valueOf(prop.getProperty("active_call_timeout"))
                    : DEFAULT_ACTIVE_CALL_TIMEOUT;
            activeCallTimeOfDay = prop.getProperty("active_call_time_of_day");

            timeFormatPattern = prop.getProperty("time_format_pattern");
            timeZoneId = prop.getProperty("time_zone_id") != null ? prop.getProperty("time_zone_id")
                    : DEFAULT_TIME_ZONE_ID;
            dateTimeNow = ZonedDateTime.now(ZoneId.of(timeZoneId));

            prop.setProperty("lastLoaded", dateTimeNow.toString());

            fis.close();

            FileOutputStream fos = new FileOutputStream(new File(SETTINGS_FILE_PATH));
            prop.storeToXML(fos, "Modified");

            fos.close();

            LOGGER.info("Initilization complete!");
            LOGGER.info("Update complete!");
        } catch (InvalidPropertiesFormatException e) {
            LOGGER.error("Error occured loading the properties file", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Error occured loading the properties file", e);
        } catch (IOException e) {
            LOGGER.error("Error occured loading the properties file", e);
        }

    }

    private Properties getSortedPropertiesInstance() {
        return new Properties() {

            private static final long serialVersionUID = 1L;

            @Override
            public Set<Object> keySet() {
                return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
            }

            @Override
            public Set<Map.Entry<Object, Object>> entrySet() {

                Set<Map.Entry<Object, Object>> set1 = super.entrySet();
                Set<Map.Entry<Object, Object>> set2 = new LinkedHashSet<>(set1.size());

                Iterator<Map.Entry<Object, Object>> iterator = set1.stream()
                        .sorted(new Comparator<Map.Entry<Object, Object>>() {

                            @Override
                            public int compare(java.util.Map.Entry<Object, Object> o1,
                                    java.util.Map.Entry<Object, Object> o2) {
                                return o1.getKey().toString().compareTo(o2.getKey().toString());
                            }
                        }).iterator();

                while (iterator.hasNext())
                    set2.add(iterator.next());

                return set2;
            }

            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<Object>(super.keySet()));
            }
        };
    }

    private void fetchSettingsFile(String settingsURL) {
        LOGGER.info("Fetching newest settings from API");

        Controller controller = new Controller();

        File settingsFile = new File(SETTINGS_FILE_PATH);
        settingsFile.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(settingsFile, false))) {
            bw.write(controller.callGet(settingsURL.concat("/api/settings?value=ls")));
            bw.flush();
            LOGGER.info("Fetching complete!");
        } catch (IOException e) {
            LOGGER.error("fetchSettingsFile().IOException == {}", e.getMessage());
        }
    }

    public String fetchApiURL() {
        Properties prop = new Properties();

        try {
            FileInputStream fis = new FileInputStream(new File(SETTINGS_FILE_PATH));
            prop.loadFromXML(fis);
            String str = prop.getProperty("api_url");
            fis.close();

            return str;
        } catch (InvalidPropertiesFormatException e) {
            LOGGER.error("Error occured loading the properties file", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Error occured loading the properties file", e);
        } catch (IOException e) {
            LOGGER.error("Error occured loading the properties file", e);
        }

        return DEFAULT_API_URL;
    }

    private void initFileStructure() {
        File fScs = new File(getScreenshotPath());
        // File fDrv = new File(getDriverPath());
        File fCnf = new File(getConfigPath());
        File fLss = new File(SETTINGS_FILE_PATH);

        if (!fScs.exists()) {
            LOGGER.info("fScs not exist");
            fScs.mkdirs();
        }

        if (!fCnf.exists()) {
            LOGGER.info("fCnf not exist");
            fCnf.mkdirs();
        }

        if (!fLss.exists()) {
            LOGGER.info("FLss not exist");
            createDefaultSettingsFile();
        }
    }

    private void createDefaultSettingsFile() {
        File f = new File(SETTINGS_FILE_PATH);
        Properties prop = getSortedPropertiesInstance();

        try (FileOutputStream fos = new FileOutputStream(f)) {
            f.setWritable(true);
            f.setReadable(true);
            f.createNewFile();

            dateTimeNow = ZonedDateTime.now(ZoneId.of(DEFAULT_TIME_ZONE_ID));

            prop.setProperty("base_url", DEFAULT_BASE_URL);
            prop.setProperty("api_url", DEFAULT_API_URL);
            prop.setProperty("filter_url", DEFAULT_FILTER_URL);
            prop.setProperty("driver_runner", DEFAULT_DRIVER_RUNNER);
            prop.setProperty("drivers_path", DEFAULT_DRIVERS_PATH);
            prop.setProperty("screenshot_path", DEFAULT_SCREENSHOT_PATH);
            prop.setProperty("internal_user_agent", DEFAULT_INTERNAL_USER_AGENT);
            prop.setProperty("external_user_agent", DEFAULT_EXTERNAL_USER_AGENT);
            prop.setProperty("page_load_timeout", String.valueOf(DEFAULT_PAGE_LOAD_TIMEOUT));
            prop.setProperty("implicit_wait_timeout", String.valueOf(DEFAULT_IMPLICIT_WAIT_TIMEOUT));
            prop.setProperty("button_next", String.valueOf(DEFAULT_BUTTON_NEXT));
            prop.setProperty("button_cookie", String.valueOf(DEFAULT_BUTTON_COOKIE));
            prop.setProperty("select_link", String.valueOf(DEFAULT_SELECT_LINK));
            prop.setProperty("select_zero_data", String.valueOf(DEFAULT_SELECT_ZERO_DATA));
            prop.setProperty("select_page_loaded", String.valueOf(DEFAULT_SELECT_PAGE_LOADED));
            prop.setProperty("is_scheduled", String.valueOf(DEFAULT_IS_SCHEDULED));
            prop.setProperty("active_call_timeout", String.valueOf(DEFAULT_ACTIVE_CALL_TIMEOUT));
            prop.setProperty("active_call_time_of_day", String.valueOf(DEFAULT_ACTIVE_TIME_OF_DAY));
            prop.setProperty("time_format_pattern", String.valueOf(DEFAULT_TIME_FORMAT_PATTERN));
            prop.setProperty("time_zone_id", String.valueOf(DEFAULT_TIME_ZONE_ID));
            prop.setProperty("lastLoaded", dateTimeNow.toString());

            prop.storeToXML(fos, "DEFAULT");
        } catch (InvalidPropertiesFormatException e) {
            LOGGER.error("Error occured loading the properties file", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Error occured loading the properties file", e);
        } catch (IOException e) {
            LOGGER.error("Error occured loading the properties file", e);
        }

    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getFilterUrl() {
        return this.filterUrl;
    }

    public void setFilterUrl(String filterUrl) {
        this.filterUrl = filterUrl;
    }

    public String getApiURL() {
        return this.apiURL;
    }

    public void setApiURL(String apiURL) {
        this.apiURL = apiURL;
    }

    public String getApiVersion() {
        return this.apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getDriverRunner() {
        return this.driverRunner;
    }

    public void setDriverRunner(String driverRunner) {
        this.driverRunner = driverRunner;
    }

    public String getDriverPath() {
        return this.driverPath;
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    public String getScreenshotPath() {
        return this.screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public String getConfigPath() {
        return this.configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public String getInternalUserAgent() {
        return this.internalUserAgent;
    }

    public void setInternalUserAgent(String internalUserAgent) {
        this.internalUserAgent = internalUserAgent;
    }

    public String getExternalUserAgent() {
        return this.externalUserAgent;
    }

    public void setExternalUserAgent(String externalUserAgent) {
        this.externalUserAgent = externalUserAgent;
    }

    public long getPageLoadTimeout() {
        return this.pageLoadTimeout;
    }

    public void setPageLoadTimeout(long pageLoadTimeout) {
        this.pageLoadTimeout = pageLoadTimeout;
    }

    public long getImplicitWaitTimeout() {
        return this.implicitWaitTimeout;
    }

    public void setImplicitWaitTimeout(long implicitWaitTimeout) {
        this.implicitWaitTimeout = implicitWaitTimeout;
    }

    public boolean isIsScheduled() {
        return this.isScheduled;
    }

    public boolean getIsScheduled() {
        return this.isScheduled;
    }

    public void setIsScheduled(boolean isScheduled) {
        this.isScheduled = isScheduled;
    }

    public long getActiveCallTimeout() {
        return this.activeCallTimeout;
    }

    public void setActiveCallTimeout(long activeCallTimeout) {
        this.activeCallTimeout = activeCallTimeout;
    }

    public String getActiveCallTimeOfDay() {
        return this.activeCallTimeOfDay;
    }

    public void setActiveCallTimeOfDay(String activeCallTimeOfDay) {
        this.activeCallTimeOfDay = activeCallTimeOfDay;
    }

    public String getTimeFormatPattern() {
        return this.timeFormatPattern;
    }

    public void setTimeFormatPattern(String timeFormatPattern) {
        this.timeFormatPattern = timeFormatPattern;
    }

    public String getTimeZoneId() {
        return this.timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getButtonNext() {
        return this.buttonNext;
    }

    public void setButtonNext(String buttonNext) {
        this.buttonNext = buttonNext;
    }

    public String getButtonCookie() {
        return this.buttonCookie;
    }

    public void setButtonCookie(String buttonCookie) {
        this.buttonCookie = buttonCookie;
    }

    public String getSelectLink() {
        return this.selectLink;
    }

    public void setSelectLink(String selectLink) {
        this.selectLink = selectLink;
    }

    public String getSelectZeroData() {
        return this.selectZeroData;
    }

    public void setSelectZeroData(String selectZeroData) {
        this.selectZeroData = selectZeroData;
    }

    public String getSelectPageLoaded() {
        return this.selectPageLoaded;
    }

    public void setSelectPageLoaded(String selectPageLoaded) {
        this.selectPageLoaded = selectPageLoaded;
    }

    public ZonedDateTime getDateTimeNow() {
        return this.dateTimeNow;
    }

    public void setDateTimeNow(ZonedDateTime dateTimeNow) {
        this.dateTimeNow = dateTimeNow;
    }

    @Override
    public String toString() {
        return "{" + " baseUrl='" + getBaseUrl() + "'" + ", filterUrl='" + getFilterUrl() + "'" + ", apiURL='"
                + getApiURL() + "'" + ", apiVersion='" + getApiVersion() + "'" + ", driverRunner='" + getDriverRunner()
                + "'" + ", driverPath='" + getDriverPath() + "'" + ", screenshotPath='" + getScreenshotPath() + "'"
                + ", configPath='" + getConfigPath() + "'" + ", internalUserAgent='" + getInternalUserAgent() + "'"
                + ", externalUserAgent='" + getExternalUserAgent() + "'" + ", pageLoadTimeout='" + getPageLoadTimeout()
                + "'" + ", implicitWaitTimeout='" + getImplicitWaitTimeout() + "'" + ", timeFormatPattern='"
                + getTimeFormatPattern() + "'" + ", timeZoneId='" + getTimeZoneId() + "'" + ", buttonNext='"
                + getButtonNext() + "'" + ", buttonCookie='" + getButtonCookie() + "'" + ", selectLink='"
                + getSelectLink() + "'" + ", selectZeroData='" + getSelectZeroData() + "'" + ", selectPageLoaded='"
                + getSelectPageLoaded() + "'" + ", isScheduled='" + isIsScheduled() + "'" + ", activeCallTimeout='"
                + getActiveCallTimeout() + "'" + ", activeCallTimeOfDay='" + getActiveCallTimeOfDay() + "'"
                + ", dateTimeNow='" + getDateTimeNow() + "'" + "}";
    }

}
