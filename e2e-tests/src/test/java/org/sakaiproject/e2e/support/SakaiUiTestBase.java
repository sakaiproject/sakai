package org.sakaiproject.e2e.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public abstract class SakaiUiTestBase {

    private static final ThreadLocal<Playwright> PLAYWRIGHT_BY_THREAD = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER_BY_THREAD = new ThreadLocal<>();

    protected BrowserContext context;
    protected Page page;
    protected SakaiHelper sakai;

    protected static final Path ARTIFACT_ROOT = Path.of("target", "playwright-artifacts");

    @BeforeAll
    static void launchBrowser() throws Exception {
        Files.createDirectories(ARTIFACT_ROOT);

        if (PLAYWRIGHT_BY_THREAD.get() == null || BROWSER_BY_THREAD.get() == null) {
            Playwright playwright = Playwright.create();
            BrowserType browserType = browserType(playwright, SakaiEnvironment.browserName());
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(SakaiEnvironment.headless());
            Browser browser = browserType.launch(launchOptions);

            PLAYWRIGHT_BY_THREAD.set(playwright);
            BROWSER_BY_THREAD.set(browser);
        }
    }

    @AfterAll
    static void closeBrowser() {
        Browser browser = BROWSER_BY_THREAD.get();
        if (browser != null) {
            browser.close();
            BROWSER_BY_THREAD.remove();
        }

        Playwright playwright = PLAYWRIGHT_BY_THREAD.get();
        if (playwright != null) {
            playwright.close();
            PLAYWRIGHT_BY_THREAD.remove();
        }
    }

    @BeforeEach
    void createContext(TestInfo testInfo) throws Exception {
        String slug = slug(testInfo);
        Path testDir = ARTIFACT_ROOT.resolve(slug);
        Files.createDirectories(testDir);

        Browser browser = BROWSER_BY_THREAD.get();
        if (browser == null) {
            throw new IllegalStateException("Browser is not initialized for this test thread");
        }

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
            .setIgnoreHTTPSErrors(true)
            .setBaseURL(SakaiEnvironment.baseUrl())
            .setRecordVideoDir(testDir.resolve("video"));

        context = browser.newContext(contextOptions);
        context.setDefaultTimeout(30_000);
        context.setDefaultNavigationTimeout(120_000);

        context.tracing().start(new Tracing.StartOptions()
            .setScreenshots(true)
            .setSnapshots(true)
            .setSources(true));

        page = context.newPage();
        String isolationKey = testInfo.getTestClass().map(Class::getName).orElse("default");
        sakai = new SakaiHelper(page, SakaiEnvironment.baseUrl(), isolationKey);
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        String slug = slug(testInfo);
        Path testDir = ARTIFACT_ROOT.resolve(slug);

        try {
            if (page != null) {
                page.screenshot(new Page.ScreenshotOptions().setPath(testDir.resolve("final.png")).setFullPage(true));
            }
        } catch (RuntimeException ignored) {
            // Ignore teardown screenshot failures.
        }

        try {
            if (context != null) {
                context.tracing().stop(new Tracing.StopOptions().setPath(testDir.resolve("trace.zip")));
            }
        } catch (RuntimeException ignored) {
            // Ignore tracing failures so context close still runs.
        }

        if (context != null) {
            context.close();
        }
    }

    private static BrowserType browserType(Playwright playwrightInstance, String browserName) {
        String normalized = browserName.toLowerCase(Locale.ROOT);
        if ("webkit".equals(normalized)) {
            return playwrightInstance.webkit();
        }
        if ("firefox".equals(normalized)) {
            return playwrightInstance.firefox();
        }
        return playwrightInstance.chromium();
    }

    private static String slug(TestInfo testInfo) {
        String className = testInfo.getTestClass().map(Class::getSimpleName).orElse("UnknownClass");
        String methodName = testInfo.getTestMethod().map(method -> method.getName()).orElse("unknownMethod");
        String base = className + "-" + methodName + "-" + Instant.now().toEpochMilli();
        return base.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
