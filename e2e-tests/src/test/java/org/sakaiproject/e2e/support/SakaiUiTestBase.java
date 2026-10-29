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
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class SakaiUiTestBase {

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;
    protected SakaiHelper sakai;
    private APIRequestContext fixtureRequest;

    @BeforeAll
    static void launchBrowser() {
        SakaiHelper.clearSiteCache();

        if (playwright == null || browser == null) {
            playwright = Playwright.create();
            BrowserType browserType = browserType(playwright, SakaiEnvironment.browserName());
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(SakaiEnvironment.headless()).setTimeout(15_000);
            browser = browserType.launch(launchOptions);
            // Surefire can create a separate JUnit root for each class within the same fork.
            Runtime.getRuntime().addShutdownHook(new Thread(SakaiUiTestBase::closeBrowser,
                "sakai-e2e-browser-cleanup"));
        }
    }

    private static void closeBrowser() {
        try {
            if (browser != null) {
                browser.close();
            }
        } finally {
            if (playwright != null) {
                playwright.close();
            }
        }
    }

    @BeforeEach
    void createContext() {
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
        context.setExtraHTTPHeaders(Map.of("Accept-Language", "en-US,en;q=0.9"));
        context.setDefaultTimeout(15_000);
        context.setDefaultNavigationTimeout(15_000);

        page = context.newPage();
        fixtureRequest = playwright.request().newContext(new APIRequest.NewContextOptions()
            .setBaseURL(SakaiEnvironment.baseUrl()).setIgnoreHTTPSErrors(true).setTimeout(15_000));
        sakai = new SakaiHelper(page, SakaiEnvironment.baseUrl(), fixtureRequest);
    }

    @AfterEach
    void closeContext() {
        try (BrowserContext closingContext = context) {
            if (page != null) {
                // Route callbacks can still be queued when the test finishes.
                page.unrouteAll();
            }
        } finally {
            if (fixtureRequest != null) {
                fixtureRequest.dispose();
            }
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

}
