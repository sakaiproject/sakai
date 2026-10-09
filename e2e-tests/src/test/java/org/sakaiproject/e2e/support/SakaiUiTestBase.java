/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.e2e.support;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class SakaiUiTestBase {

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;
    protected SakaiHelper sakai;
    private String artifactSlug;
    private String migrationStep;
    private final List<String> migrationPageErrors = new ArrayList<>();
    private APIRequestContext fixtureRequest;

    @RegisterExtension
    final AfterEachCallback artifacts = extension -> tearDown(extension.getExecutionException().isPresent());

    protected static final Path ARTIFACT_ROOT = Path.of(System.getProperty("sakai.test.artifacts", "target/playwright-artifacts"));

    @BeforeAll
    static void launchBrowser() throws Exception {
        Files.createDirectories(ARTIFACT_ROOT);
        SakaiHelper.clearSiteCache();

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
        migrationStep = migrationStep(testInfo);
        Path testDir = ARTIFACT_ROOT.resolve(artifactSlug);
        Files.createDirectories(testDir);

        Browser activeBrowser = browser;
        if (activeBrowser == null) {
            throw new IllegalStateException("Browser is not initialized");
        }

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
            .setIgnoreHTTPSErrors(true)
            .setBaseURL(SakaiEnvironment.baseUrl())
            .setLocale("en-US")
            .setTimezoneId("America/New_York");

        context = activeBrowser.newContext(contextOptions);
        // setLocale() only affects navigator.language/Intl in the browser; Sakai negotiates
        // its UI language server-side from the Accept-Language header, so it must be set here too.
        String migrationCapture = System.getProperty("samigo.equivalence.suiteOutput");
        if (migrationCapture == null) {
            context.setExtraHTTPHeaders(Map.of("Accept-Language", "en-US,en;q=0.9"));
        } else {
            context.setExtraHTTPHeaders(Map.of("Accept-Language", "en-US,en;q=0.9",
                "X-Sakai-Equivalence-Step", migrationStep(testInfo)));
        }
        context.setDefaultTimeout(30_000);
        context.setDefaultNavigationTimeout(120_000);

        context.tracing().start(new Tracing.StartOptions()
            .setScreenshots(true)
            .setSnapshots(true)
            .setSources(true));

        migrationPageErrors.clear();
        page = context.newPage();
        if (migrationCapture != null) {
            page.onPageError(migrationPageErrors::add);
        }
        fixtureRequest = playwright.request().newContext(new APIRequest.NewContextOptions()
            .setBaseURL(SakaiEnvironment.baseUrl()).setIgnoreHTTPSErrors(true));
        sakai = new SakaiHelper(page, SakaiEnvironment.baseUrl(), fixtureRequest);
    }

    private void tearDown(boolean failed) throws Exception {
        if (artifactSlug == null) {
            return;
        }
        Path testDir = ARTIFACT_ROOT.resolve(artifactSlug);

        try {
            if (failed && page != null) {
                page.screenshot(new Page.ScreenshotOptions().setPath(testDir.resolve("final.png")).setFullPage(true));
            }
        } catch (RuntimeException ignored) {
            // Ignore teardown screenshot failures.
        }

        try {
            if (context != null) {
                // Export before cleanup: a passing test can still fail while closing resources.
                context.tracing().stop(new Tracing.StopOptions().setPath(testDir.resolve("trace.zip")));
            }
        } catch (RuntimeException ignored) {
            // Ignore tracing failures so context close still runs.
        }

        try {
            captureMigrationSnapshot();
        } finally {
            try {
                if (context != null) {
                    context.close();
                }
            } finally {
                if (fixtureRequest != null) {
                    fixtureRequest.dispose();
                }
            }
        }
        if (!failed) {
            Files.deleteIfExists(testDir.resolve("trace.zip"));
            Files.deleteIfExists(testDir);
        }
        artifactSlug = null;
    }

    private void captureMigrationSnapshot() throws IOException {
        String migrationCapture = System.getProperty("samigo.equivalence.suiteOutput");
        if (migrationCapture != null && page != null && !page.isClosed()) {
            Path destination = Path.of(migrationCapture);
            Files.createDirectories(destination);
            String step = migrationStep;
            String snapshot = (String) page.evaluate("""
                () => {
                  const content = document.getElementById('content') || document.body;
                  return JSON.stringify({path:location.pathname,
                    forms:[...content.querySelectorAll('form')].map(form => ({id:form.id, action:form.getAttribute('action'),
                      controls:[...form.querySelectorAll('input,select,textarea,button')]
                        .filter(e=>!['jakarta.faces.ViewState','sakai_csrf_token'].includes(e.name))
                        .map(e=>({tag:e.tagName,id:e.id,name:e.name,type:e.type,value:e.value,checked:e.checked??null,
                          disabled:e.disabled,options:e.options?[...e.options].map(o=>({value:o.value,text:o.text,selected:o.selected})):null})),
                      labels:[...form.querySelectorAll('label')].map(e=>({for:e.htmlFor,text:e.innerText})),
                      messages:[...form.querySelectorAll('.sak-banner-error,.validate')].map(e=>e.innerText),text:form.innerText})),
                    scripts:[...document.scripts].filter(e=>e.src).map(e=>new URL(e.src).pathname),
                    stylesheets:[...document.querySelectorAll('link[rel=stylesheet]')].map(e=>new URL(e.href).pathname)});
                }
                """);
            Files.writeString(destination.resolve(step + ".json"), snapshot);
            Files.writeString(destination.resolve(step + ".html"), (String) page.evaluate(
                "() => (document.getElementById('content') || document.body).innerHTML"));
            Files.writeString(destination.resolve(step + "-errors.json"), (String) page.evaluate(
                "errors => JSON.stringify(errors)", migrationPageErrors));
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

    private static String migrationStep(TestInfo testInfo) {
        String className = testInfo.getTestClass().map(Class::getSimpleName).orElse("unknown");
        String methodName = testInfo.getTestMethod().map(method -> method.getName()).orElse("unknown");
        return (className + "-" + methodName).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "-");
    }

    private static String slug(TestInfo testInfo) {
        String className = testInfo.getTestClass().map(Class::getSimpleName).orElse("UnknownClass");
        String methodName = testInfo.getTestMethod().map(method -> method.getName()).orElse("unknownMethod");
        String base = className + "-" + methodName + "-" + Instant.now().toEpochMilli();
        return base.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
