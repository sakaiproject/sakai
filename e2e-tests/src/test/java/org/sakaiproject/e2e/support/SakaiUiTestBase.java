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

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;
    protected SakaiHelper sakai;
    private String artifactSlug;

    protected static final Path ARTIFACT_ROOT = Path.of("target", "playwright-artifacts");

    @BeforeAll
    static void launchBrowser() throws Exception {
        Files.createDirectories(ARTIFACT_ROOT);

        if (playwright == null || browser == null) {
            playwright = Playwright.create();
            BrowserType browserType = browserType(playwright, SakaiEnvironment.browserName());
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(SakaiEnvironment.headless());
            browser = browserType.launch(launchOptions);
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
            browser = null;
        }

        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    @BeforeEach
    void createContext(TestInfo testInfo) throws Exception {
        artifactSlug = slug(testInfo);
        Path testDir = ARTIFACT_ROOT.resolve(artifactSlug);
        Files.createDirectories(testDir);

        Browser activeBrowser = browser;
        if (activeBrowser == null) {
            throw new IllegalStateException("Browser is not initialized");
        }

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
            .setIgnoreHTTPSErrors(true)
            .setBaseURL(SakaiEnvironment.baseUrl())
            .setRecordVideoDir(testDir.resolve("video"));

        context = activeBrowser.newContext(contextOptions);
        context.setDefaultTimeout(30_000);
        context.setDefaultNavigationTimeout(120_000);

        context.tracing().start(new Tracing.StartOptions()
            .setScreenshots(true)
            .setSnapshots(true)
            .setSources(true));

        page = context.newPage();
        sakai = new SakaiHelper(page, SakaiEnvironment.baseUrl());
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        if (artifactSlug == null) {
            artifactSlug = slug(testInfo);
        }
        Path testDir = ARTIFACT_ROOT.resolve(artifactSlug);

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
        artifactSlug = null;
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
