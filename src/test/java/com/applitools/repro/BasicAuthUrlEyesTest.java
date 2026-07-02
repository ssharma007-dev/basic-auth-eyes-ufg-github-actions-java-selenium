package com.applitools.repro;

import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Minimal reproducer: opens a basic-auth-protected URL in headless Chrome
 * and takes a single Applitools Eyes checkpoint. No login flow, no extra
 * steps — just enough to confirm what a real browser renders at that URL.
 *
 * Configure via environment variables (no credentials in source):
 *   APPLITOOLS_API_KEY, URL, BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD
 */
public class BasicAuthUrlEyesTest {

    private static final String URL = System.getenv("URL");
    private static final String BASIC_AUTH_USERNAME = System.getenv().getOrDefault("BASIC_AUTH_USERNAME", "");
    private static final String BASIC_AUTH_PASSWORD = System.getenv().getOrDefault("BASIC_AUTH_PASSWORD", "");

    private WebDriver driver;
    private Eyes eyes;
    private VisualGridRunner runner;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1200,800");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));

        runner = new VisualGridRunner(1);
        eyes = new Eyes(runner);
    }

    @Test
    public void urlMatchesBaseline() {
        if (URL == null || URL.isBlank()) {
            throw new IllegalStateException("URL environment variable is not set.");
        }
        try {
            String targetUrl = BASIC_AUTH_USERNAME.isBlank()
                    ? URL
                    : withEmbeddedCredentials(URL, BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
            driver.get(targetUrl);

            eyes.open(driver, "Basic Auth URL Repro", "URL check");
            eyes.check(Target.window());
            eyes.closeAsync();
        } catch (Throwable t) {
            eyes.abortAsync();
            throw t;
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (runner != null) {
            System.out.println(runner.getAllTestResults(false));
        }
    }

    /** Embeds basic-auth credentials directly in the URL (scheme://user:pass@host/...). */
    private static String withEmbeddedCredentials(String url, String username, String password) {
        URI uri = URI.create(url);
        String userInfo = URLEncoder.encode(username, StandardCharsets.UTF_8)
                + ":" + URLEncoder.encode(password, StandardCharsets.UTF_8);
        String authority = userInfo + "@" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
        String rest = uri.getRawPath()
                + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "")
                + (uri.getRawFragment() != null ? "#" + uri.getRawFragment() : "");
        return uri.getScheme() + "://" + authority + rest;
    }
}
